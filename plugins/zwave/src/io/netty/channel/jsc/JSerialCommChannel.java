/*
 * Copyright 2017 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.netty.channel.jsc;

import com.fazecast.jSerialComm.SerialPort;
import io.netty.channel.ChannelPromise;
import io.netty.channel.oio.OioByteStreamChannel;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.TimeUnit;

import static io.netty.channel.jsc.JSCChannelOption.*;


/**
 * A channel to a serial device using the jSerialComm library.
 */
public class JSerialCommChannel extends OioByteStreamChannel {

    private static final JSCDeviceAddress LOCAL_ADDRESS = new JSCDeviceAddress("localhost");

    private final JSCChannelConfig config;

    private boolean open = true;
    private JSCDeviceAddress deviceAddress;
    private SerialPort serialPort;

    public JSerialCommChannel() {
        super(null);

        config = new DefaultJSCChannelConfig(this);
    }

    @Override
    public JSCChannelConfig config() {
        return config;
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    protected AbstractUnsafe newUnsafe() {
        return new JSCUnsafe();
    }

    @Override
    protected void doConnect(SocketAddress remoteAddress, SocketAddress localAddress) throws Exception {
        JSCDeviceAddress remote = (JSCDeviceAddress) remoteAddress;
        SerialPort commPort = SerialPort.getCommPort(remote.value());
        if(!commPort.openPort())
            throw new IOException("Could not open port: "+remote.value());

        commPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING, config().getOption(READ_TIMEOUT), 0);

        deviceAddress = remote;
        serialPort = commPort;
    }

    protected void doInit() throws Exception {
        serialPort.setComPortParameters(
            config().getOption(BAUD_RATE),
            config().getOption(DATA_BITS),
            config().getOption(STOP_BITS).value(),
            config().getOption(PARITY_BIT).value()
        );
        //serialPort.setFlowControl(config().getOption(FLOW_CTRL));

        activate(serialPort.getInputStream(), serialPort.getOutputStream());
    }

    @Override
    public JSCDeviceAddress localAddress() {
        return (JSCDeviceAddress) super.localAddress();
    }

    @Override
    public JSCDeviceAddress remoteAddress() {
        return (JSCDeviceAddress) super.remoteAddress();
    }

    @Override
    protected JSCDeviceAddress localAddress0() {
        return LOCAL_ADDRESS;
    }

    @Override
    protected JSCDeviceAddress remoteAddress0() {
        return deviceAddress;
    }

    @Override
    protected void doBind(SocketAddress localAddress) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doDisconnect() throws Exception {
        doClose();
    }

    @Override
    protected void doClose() throws Exception {
        open = false;
        try {
           super.doClose();
        } finally {
            if (serialPort != null) {
                serialPort.closePort();
                serialPort = null;
            }
        }
    }

    @Override
    protected boolean isInputShutdown() {
        return !open;
    }


    private final class JSCUnsafe extends AbstractUnsafe {
        @Override
        public void connect(
                final SocketAddress remoteAddress,
                final SocketAddress localAddress, final ChannelPromise promise) {
            if (!promise.setUncancellable() || !isOpen()) {
                return;
            }

            try {
                final boolean wasActive = isActive();
                doConnect(remoteAddress, localAddress);

                int waitTime = config().getOption(WAIT_TIME);
                if (waitTime > 0) {
                    eventLoop().schedule(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                doInit();
                                safeSetSuccess(promise);
                                if (!wasActive && isActive()) {
                                    pipeline().fireChannelActive();
                                }
                            } catch (Throwable t) {
                                safeSetFailure(promise, t);
                                closeIfClosed();
                            }
                        }
                   }, waitTime, TimeUnit.MILLISECONDS);
                } else {
                    doInit();
                    safeSetSuccess(promise);
                    if (!wasActive && isActive()) {
                        pipeline().fireChannelActive();
                    }
                }
            } catch (Throwable t) {
                safeSetFailure(promise, t);
                closeIfClosed();
            }
        }
    }
}
