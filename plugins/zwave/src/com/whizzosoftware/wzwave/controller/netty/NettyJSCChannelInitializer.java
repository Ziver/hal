/*******************************************************************************
 * Copyright (c) 2013 Whizzo Software, LLC.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package com.whizzosoftware.wzwave.controller.netty;

import com.whizzosoftware.wzwave.channel.ZWaveChannelListener;
import io.netty.channel.jsc.JSCChannelConfig;
import io.netty.channel.jsc.JSCDeviceAddress;
import io.netty.channel.jsc.JSerialCommChannel;


/**
 * A jSerialComm serial port initializer
 *
 * @author Ziver Koc
 */
public class NettyJSCChannelInitializer extends AbstractNettyChannelInitializer<JSerialCommChannel> {


    public NettyJSCChannelInitializer(String serialPort, ZWaveChannelListener listener) {
        super(serialPort, listener);

        bootstrap.channel(JSerialCommChannel.class);
    }

    @Override
    protected void doInitChannel(JSerialCommChannel channel) throws Exception {
        this.channel = channel;
        channel.config().setBaudrate(115200);
        channel.config().setDatabits(8);
        channel.config().setParitybit(JSCChannelConfig.Paritybit.NONE);
        channel.config().setStopbits(JSCChannelConfig.Stopbits.STOPBITS_1);
    }


    @Override
    public JSCDeviceAddress getSocketAddress() {
        return new JSCDeviceAddress(serialPort);
    }
}
