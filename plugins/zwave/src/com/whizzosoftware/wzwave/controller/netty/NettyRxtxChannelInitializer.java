/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller.netty;

import com.whizzosoftware.wzwave.channel.ZWaveChannelListener;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.rxtx.RxtxChannel;
import io.netty.channel.rxtx.RxtxChannelConfig;
import io.netty.channel.rxtx.RxtxDeviceAddress;

/**
 * A RxTx serial port initializer
 *
 * @author Ziver Koc
 */
public class NettyRxtxChannelInitializer extends AbstractNettyChannelInitializer<RxtxChannel> {


    public NettyRxtxChannelInitializer(String serialPort, ZWaveChannelListener listener) {
        super(serialPort, listener);

        bootstrap.channel(RxtxChannel.class);
    }

    @Override
    protected void doInitChannel(RxtxChannel channel) throws Exception {
        channel.config().setBaudrate(115200);
        channel.config().setDatabits(RxtxChannelConfig.Databits.DATABITS_8);
        channel.config().setParitybit(RxtxChannelConfig.Paritybit.NONE);
        channel.config().setStopbits(RxtxChannelConfig.Stopbits.STOPBITS_1);
    }


    @Override
    public RxtxDeviceAddress getSocketAddress() {
        return new RxtxDeviceAddress(serialPort);
    }
}
