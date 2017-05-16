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
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.jsc.JSerialCommChannel;
import io.netty.channel.oio.OioEventLoopGroup;

import java.net.SocketAddress;

/**
 * A abstract initializer
 *
 * @author Ziver Koc
 */
abstract class AbstractNettyChannelInitializer<T extends Channel> extends ChannelInitializer<T> {

    protected String serialPort;
    private ZWaveChannelInboundHandler inboundHandler;

    protected Bootstrap bootstrap;
    protected Channel channel;


    public AbstractNettyChannelInitializer(String serialPort, ZWaveChannelListener listener) {
        this.serialPort = serialPort;
        this.inboundHandler = new ZWaveChannelInboundHandler(listener);

        bootstrap = new Bootstrap();
        bootstrap.group(new OioEventLoopGroup());
        bootstrap.handler(this);
    }



    @Override
    protected void initChannel(T channel) throws Exception {
        this.channel = channel;
        doInitChannel(channel);

        // Setup general channel handlers and coders
        channel.pipeline().addLast("decoder", new ZWaveFrameDecoder());
        channel.pipeline().addLast("ack", new AcknowledgementInboundHandler());
        channel.pipeline().addLast("transaction", new ZWaveDataFrameTransactionInboundHandler());
        channel.pipeline().addLast("handler", inboundHandler);
        channel.pipeline().addLast("encoder", new ZWaveFrameEncoder());
        channel.pipeline().addLast("writeQueue", new ZWaveQueuedOutboundHandler());
    }


    public Bootstrap getBootstrap() {
        return bootstrap;
    }
    public Channel getChannel() {
        return channel;
    }


    public abstract SocketAddress getSocketAddress();

    protected abstract void doInitChannel(T channel) throws Exception;
}
