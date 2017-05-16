/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller.netty;

import com.whizzosoftware.wzwave.channel.*;
import com.whizzosoftware.wzwave.codec.ZWaveFrameDecoder;
import com.whizzosoftware.wzwave.codec.ZWaveFrameEncoder;
import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.node.*;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * A Netty implementation of a ZWaveController.
 *
 * @author Dan Noguerol
 */
public class NettyZWaveController implements ZWaveController, ZWaveControllerContext, ZWaveControllerListener, ZWaveChannelListener, NodeListener {
    private static final Logger logger = LoggerFactory.getLogger(NettyZWaveController.class);

    private AbstractNettyChannelInitializer serial;
    private String libraryVersion;
    private int homeId;
    private byte nodeId;
    private ZWaveControllerListener listener;
    private final List<ZWaveNode> nodes = new ArrayList<ZWaveNode>();
    private final Map<Byte,ZWaveNode> nodeMap = new HashMap<Byte,ZWaveNode>();

    public NettyZWaveController(String serialPort) {
        // Choose a available library
        try{
            Class.forName("gnu.io.SerialPort"); // check if RxTx is available
            logger.info("RxTx is available, using it as Serial port Com port library");
            serial = new NettyRxtxChannelInitializer(serialPort, this);
        } catch (ClassNotFoundException e) {
            try {
                Class.forName("com.fazecast.jSerialComm.SerialPort"); // check if jSerialComm is available
                logger.info("jSerialComm is available, using it as Serial port library");
                serial = new NettyJSCChannelInitializer(serialPort, this);
            } catch (ClassNotFoundException e1) {
                throw new RuntimeException("Unable to find Rxtx or jSerialComm lib in classpath", e);
            }
        }
    }

    /*
     * ZWaveController methods
     */

    @Override
    public void setListener(ZWaveControllerListener listener) {
        this.listener = listener;
    }

    public void start() {
        serial.getBootstrap().connect(serial.getSocketAddress()).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (future.isSuccess()) {
                    serial.getChannel().write(new Version());
                    serial.getChannel().write(new MemoryGetId());
                    serial.getChannel().write(new InitData());
                } else {
                    listener.onZWaveConnectionFailure(future.cause());
                }
            }
        });
    }

    @Override
    public void stop() {

    }

    @Override
    public int getHomeId() {
        return homeId;
    }

    @Override
    public byte getNodeId() {
        return nodeId;
    }

    @Override
    public String getLibraryVersion() {
        return libraryVersion;
    }

    @Override
    public Collection<ZWaveNode> getNodes() {
        return nodes;
    }

    @Override
    public void sendDataFrame(DataFrame dataFrame) {
        serial.getChannel().write(dataFrame);
    }

    /*
     * ZWaveControllerListener methods
     */

    @Override
    public void onZWaveNodeAdded(ZWaveEndpoint node) {
        if (listener != null) {
            listener.onZWaveNodeAdded(node);
        }
    }

    @Override
    public void onZWaveNodeUpdated(ZWaveEndpoint node) {
        if (listener != null) {
            listener.onZWaveNodeUpdated(node);
        }
    }

    @Override
    public void onZWaveConnectionFailure(Throwable t) {
        if (listener != null) {
            listener.onZWaveConnectionFailure(t);
        }
    }

    /*
     * ZWaveChannelListener methods
     */

    @Override
    public void onLibraryInfo(String libraryVersion) {
        this.libraryVersion = libraryVersion;
    }

    @Override
    public void onControllerInfo(int homeId, byte nodeId) {
        this.homeId = homeId;
        this.nodeId = nodeId;
    }

    @Override
    public void onNodeProtocolInfo(byte nodeId, NodeProtocolInfo nodeProtocolInfo) {
        try {
            logger.trace("Received protocol info for node " + nodeId);
            ZWaveNode node = ZWaveNodeFactory.createNode(this, nodeId, nodeProtocolInfo, this);
            logger.trace("Created node [" + node.getNodeId() + "]: " + node);
            nodes.add(node);
            nodeMap.put(node.getNodeId(), node);
        } catch (NodeCreationException e) {
            logger.error("Unable to create node", e);
        }
    }

    @Override
    public void onSendData(SendData sendData) {
        byte nodeId = sendData.getNodeId();
        ZWaveNode node = nodeMap.get(nodeId);
        if (node != null) {
            node.onDataFrameReceived(this, sendData);
        } else {
            logger.error("Unable to find node " + nodeId);
        }
    }

    @Override
    public void onApplicationCommand(ApplicationCommand applicationCommand) {
        ZWaveNode node = nodeMap.get(applicationCommand.getNodeId());
        if (node != null) {
            node.onDataFrameReceived(this, applicationCommand);
            if (listener != null && node.isStarted()) {
                listener.onZWaveNodeUpdated(node);
            }
        } else {
            logger.error("Unable to find node " + applicationCommand.getNodeId());
        }
    }

    @Override
    public void onApplicationUpdate(ApplicationUpdate applicationUpdate) {
        Byte nodeId = applicationUpdate.getNodeId();

        if (applicationUpdate.didInfoRequestFail()) {
            logger.trace("UPDATE_STATE_NODE_INFO_REQ_FAILED received");
        }

        if (nodeId != null) {
            ZWaveNode node = nodeMap.get(nodeId);
            if (node != null) {
                node.onDataFrameReceived(this, applicationUpdate);
                if (listener != null && node.isStarted()) {
                    listener.onZWaveNodeUpdated(node);
                }
            } else {
                logger.error("Unable to find node " + applicationUpdate.getNodeId());
            }
        } else {
            logger.error("Unable to determine node to route ApplicationUpdate to");
        }
    }

    /*
     * NodeListener methods
     */

    @Override
    public void onNodeStarted(ZWaveNode node) {
        // when a node moves to the "started" state, alert listeners that it's ready to be added
        if (listener != null) {
            listener.onZWaveNodeAdded(node);
        }
    }

    /*
     * Test main
     */

    public static void main(String[] args) throws Exception {
        NettyZWaveController c = new NettyZWaveController("/dev/cu.SLAB_USBtoUART");
        ZWaveControllerListener listener = new ZWaveControllerListener() {
            @Override
            public void onZWaveNodeAdded(ZWaveEndpoint node) {
                System.out.println("onZWaveNodeAdded: " + node);
            }

            @Override
            public void onZWaveNodeUpdated(ZWaveEndpoint node) {
                System.out.println("onZWaveNodeUpdated: " + node);
            }

            @Override
            public void onZWaveConnectionFailure(Throwable t) {
                System.out.println("A connection error occurred: " + t.getLocalizedMessage());
            }
        };
        c.setListener(listener);
        c.start();

        Thread.sleep(10000);
    }
}
