/*
 *******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************
*/
package com.whizzosoftware.wzwave.controller.netty;

import com.whizzosoftware.wzwave.channel.*;
import com.whizzosoftware.wzwave.channel.event.*;
import com.whizzosoftware.wzwave.commandclass.WakeUpCommandClass;
import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.ZWaveControllerContext;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.frame.*;
import com.whizzosoftware.wzwave.node.*;
import com.whizzosoftware.wzwave.persist.PersistentStore;
import com.whizzosoftware.wzwave.persist.mapdb.MapDbPersistentStore;
import com.whizzosoftware.wzwave.util.ByteUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.oio.OioEventLoopGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;

/**
 * A Netty implementation of a ZWaveController.
 *
 * The pipeline looks like this:
 *
 *                                                      I/O Request via Channel or
 *                                                        ChannelHandlerContext
 *                                                                  |
 * +----------------------------------------------------------------+-------------------+
 * |                                      ChannelPipeline           |                   |
 * |                                                                |                   |
 * |    +--------------------------------+                          |                   |
 * |    |    ZWaveChannelInboundHandler  |                          |                   |
 * |    +---------------+----------------+                          |                   |
 * |                   /|\                                          |                   |
 * |                    |                                           |                   |
 * |    +---------------+----------------+                          |                   |
 * |    |    TransactionInboundHandler   |                          |                   |
 * |    +---------------+----------------+                          |                   |
 * |                   /|\                                          |                   |
 * |                    |                                          \|/                  |
 * |    +---------------+-------------------------------------------+--------------+    |
 * |    |                                FrameQueueHandler                         |    |
 * |    +---------------+-------------------------------------------+--------------+    |
 * |                   /|\                                          |                   |
 * |                    |                                           |                   |
 * |    +---------------+----------------+                          |                   |
 * |    |       ACKInboundHandler        |                          |                   |
 * |    +---------------+----------------+                          |                   |
 * |                   /|\                                          |                   |
 * |                    |                                           |                   |
 * |    +---------------+----------------+            +-------------+--------------+    |
 * |    |       ZWaveFrameDecoder        |            |      ZWaveFrameEncoder     |    |
 * |    +---------------+----------------+            +-------------+--------------+    |
 * |                   /|\                                          |                   |
 * +--------------------+-------------------------------------------+-------------------+
 * |                    |                                          \|/                  |
 * +--------------------+-------------------------------------------+-------------------+
 * |                    |                                           |                   |
 * |            [ Socket.read() ]                           [ Socket.write() ]          |
 * +------------------------------------------------------------------------------------+
 *
 * @author Dan Noguerol
 */
public class NettyZWaveController implements ZWaveController, ZWaveControllerContext, ZWaveControllerListener, ZWaveChannelListener, NodeListener {
    private static final Logger logger = LoggerFactory.getLogger(NettyZWaveController.class);

    private String serialPort;
    private PersistentStore store;
    private AbstractNettyChannelInitializer serial;
    private String libraryVersion;
    private Integer homeId;
    private Byte nodeId;
    private ZWaveControllerListener listener;
    private final List<ZWaveNode> nodes = new ArrayList<>();
    private final Map<Byte,ZWaveNode> nodeMap = new HashMap<>();

    /**
     * Constructor.
     *
     * @param serialPort the serial port the Z-Wave controller is accessible from
     * @param dataDirectory a directory in which to store persistent data
     */
    public NettyZWaveController(String serialPort, File dataDirectory) {
        this(serialPort, new MapDbPersistentStore(dataDirectory));
    }

    /**
     * Constructor.
     *
     * @param serialPort the serial port for Z-Wave controller is accessible from
     * @param store the persistent store to use for storing/retrieving node information
     */
    public NettyZWaveController(String serialPort, PersistentStore store) {
        this.serialPort = serialPort;
        this.store = store;
    }

    /*
     * ZWaveController methods
     */

    @Override
    public void setListener(ZWaveControllerListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (serial == null) {
            // Choose a available library
            try{
                Class.forName("gnu.io.SerialPort"); // check if RxTx is available
                logger.info("RxTx is available, using it as Serial port library");
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

            // set up Netty bootstrap
            Bootstrap bootstrap = serial.getBootstrap();
            bootstrap.connect(serial.getSocketAddress()).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {
                    if (future.isSuccess()) {
                        sendDataFrame(new Version());
                        sendDataFrame(new MemoryGetId());
                        sendDataFrame(new InitData());
                    } else {
                        onZWaveConnectionFailure(future.cause());
                    }
                }
            });
        }
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
    public ZWaveNode getNode(byte nodeId) {
        return nodeMap.get(nodeId);
    }

    public void sendDataFrame(DataFrame frame) {
        serial.getChannel().write(new OutboundDataFrame(frame, true));
    }

    public void sendDataFrame(DataFrame frame, boolean isListeningNode) {
        serial.getChannel().write(new OutboundDataFrame(frame, isListeningNode));
    }

    @Override
    public void sendEvent(Object e) {
        serial.getChannel().write(e);
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
        } else {
            logger.error("Connection failure and no listener was set", t);
        }
    }

    @Override
    public void onZWaveControllerInfo(String libraryVersion, Integer homeId, Byte nodeId) {
        if (listener != null && libraryVersion != null && homeId != null && nodeId != null) {
            listener.onZWaveControllerInfo(libraryVersion, homeId, nodeId);
        }
    }

    @Override
    public void onZWaveInclusionStarted() {
        if (listener != null) {
            listener.onZWaveInclusionStarted();
        }
    }

    @Override
    public void onZWaveInclusion(NodeInfo nodeInfo, boolean success) {
        try {
            logger.trace("Inclusion of new node {}", ByteUtil.createString(nodeInfo.getNodeId()));
            ZWaveNode node = ZWaveNodeFactory.createNode(nodeInfo, !nodeInfo.hasCommandClass(WakeUpCommandClass.ID), this);
            logger.trace("Created new node [{}]: {}", node.getNodeId(), node);
            addNode(node);
            if (listener != null) {
                listener.onZWaveInclusion(nodeInfo, success);
            }
        } catch (NodeCreationException e) {
            logger.error("Unable to create node", e);
        }
    }

    @Override
    public void onZWaveInclusionStopped() {
        if (listener != null) {
            listener.onZWaveInclusionStopped();
        }
    }

    @Override
    public void onZWaveExclusionStarted() {
        if (listener != null) {
            listener.onZWaveExclusionStarted();
        }
    }

    @Override
    public void onZWaveExclusion(NodeInfo nodeInfo, boolean success) {
        if (listener != null) {
            listener.onZWaveExclusion(nodeInfo, success);
        }
    }

    @Override
    public void onZWaveExclusionStopped() {
        if (listener != null) {
            listener.onZWaveExclusionStopped();
        }
    }

    /*
     * ZWaveChannelListener methods
     */

    @Override
    public void onLibraryInfo(String libraryVersion) {
        this.libraryVersion = libraryVersion;
        onZWaveControllerInfo(libraryVersion, homeId, nodeId);
    }

    @Override
    public void onControllerInfo(int homeId, byte nodeId) {
        this.homeId = homeId;
        this.nodeId = nodeId;
        onZWaveControllerInfo(libraryVersion, homeId, nodeId);
    }

    @Override
    public void onNodeProtocolInfo(byte nodeId, NodeProtocolInfo npi) {
        try {
            logger.trace("Received protocol info for node {}", nodeId);
            ZWaveNode node = store.getNode(nodeId, this);
            if (node == null || !node.matchesNodeProtocolInfo(npi)) {
                node = ZWaveNodeFactory.createNode(
                        new NodeInfo(nodeId, npi.getBasicDeviceClass(), npi.getGenericDeviceClass(), npi.getSpecificDeviceClass()),
                        npi.isListening(),
                        this
                );
                logger.trace("Created new node: {}: {}", nodeId, node);
            } else {
                logger.debug("Node[{}] matches persistent node information; no need to interview", nodeId);
            }
            addNode(node);
        } catch (NodeCreationException e) {
            logger.error("Unable to create node", e);
        }
    }

    private void addNode(ZWaveNode node) {
        ZWaveNode n = nodeMap.get(node.getNodeId());
        if (n != null) {
            nodes.remove(n);
            nodeMap.remove(node.getNodeId());
        }
        nodes.add(node);
        nodeMap.put(node.getNodeId(), node);
        node.startInterview(this);
    }

    @Override
    public void onApplicationCommand(ApplicationCommand applicationCommand) {
        ZWaveNode node = nodeMap.get(applicationCommand.getNodeId());
        if (node != null) {
            node.onApplicationCommand(this, applicationCommand);
            if (node.isStarted()) {
                onZWaveNodeUpdated(node);
            }
        } else {
            logger.error("Unable to find node: {}", nodeId);
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
                node.onApplicationUpdate(this, applicationUpdate);
                if (node.isStarted()) {
                    onZWaveNodeUpdated(node);
                }
            } else {
                logger.error("Unable to find node: {}", nodeId);
            }
        } else {
            logger.error("Unable to determine node to route ApplicationUpdate to");
        }
    }

    @Override
    public void onTransactionStarted(TransactionStartedEvent evt) {
        logger.trace("Detected start of new transaction: {}", evt.getId());
        serial.getChannel().write(evt);
    }

    @Override
    public void onTransactionComplete(TransactionCompletedEvent evt) {
        logger.trace("Detected end of transaction: {}", evt.getId());
        if (evt instanceof SendDataTransactionCompletedEvent) {
            ZWaveNode node = nodeMap.get(evt.getNodeId());
            if (node != null) {
                node.onSendDataCallback(this, true);
            } else {
                logger.error("Unable to find node: {}", evt.getNodeId());
            }
        }
        serial.getChannel().write(evt);
    }

    @Override
    public void onTransactionFailed(TransactionFailedEvent evt) {
        logger.trace("Detected transaction failure: {}", evt.getId());
        if (evt instanceof SendDataTransactionFailedEvent) {
            ZWaveNode node = nodeMap.get(evt.getNodeId());
            if (node != null) {
                node.onSendDataCallback(this, ((SendDataTransactionFailedEvent)evt).isTargetNodeACKReceived());
            } else {
                logger.error("Unable to find node: {}", evt.getNodeId());
            }
        }
        serial.getChannel().write(evt);
    }

    @Override
    public void onAddNodeToNetwork(AddNodeToNetwork update) {
        if (listener != null) {
            switch (update.getStatus()) {
                case AddNodeToNetwork.ADD_NODE_STATUS_LEARN_READY:
                    onZWaveInclusionStarted();
                    break;
                case AddNodeToNetwork.ADD_NODE_STATUS_DONE:
                    onZWaveInclusionStopped();
                    break;
                case AddNodeToNetwork.ADD_NODE_STATUS_ADDING_CONTROLLER:
                case AddNodeToNetwork.ADD_NODE_STATUS_ADDING_SLAVE:
                    onZWaveInclusion(update.getNodeInfo(), true);
                    break;
                case AddNodeToNetwork.ADD_NODE_STATUS_FAILED:
                    onZWaveInclusion(update.getNodeInfo(), false);
                    break;
                default:
                    logger.debug("Received unexpected status from AddNodeToNetwork frame: {}", update.getStatus());
            }
        }
    }

    @Override
    public void onRemoveNodeFromNetwork(RemoveNodeFromNetwork update) {
        if (listener != null) {
            switch (update.getStatus()) {
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_LEARN_READY:
                    onZWaveExclusionStarted();
                    break;
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_DONE:
                    onZWaveExclusionStopped();
                    break;
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_NODE_FOUND:
                    logger.debug("A node has been found that wants to be excluded: {}", ByteUtil.createString(update.getSource()));
                    break;
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_REMOVING_CONTROLLER:
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_REMOVING_SLAVE:
                    onZWaveExclusion(update.getNodeInfo(), true);
                    break;
                case RemoveNodeFromNetwork.REMOVE_NODE_STATUS_FAILED:
                    onZWaveExclusion(update.getNodeInfo(), false);
                    break;
                default:
                    logger.debug("Received unexpected status from RemoveNodeFromNetwork frame: {}", update.getStatus());
            }
        }
    }

    @Override
    public void onSetDefault() {
        logger.info("Z-Wave controller has been reset to factory default");
    }

    /*
     * NodeListener methods
     */

    @Override
    public void onNodeStarted(ZWaveNode node) {
        // save the newly started node
        logger.debug("Saving information for node {}", node.getNodeId());
        store.saveNode(node);

        // when a node moves to the "started" state, alert listeners that it's ready to be added
        onZWaveNodeAdded(node);
    }
}
