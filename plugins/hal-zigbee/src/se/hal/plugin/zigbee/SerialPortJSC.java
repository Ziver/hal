/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.hal.plugin.zigbee;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;

import com.fazecast.jSerialComm.SerialPort;

/**
 * Wrapper for usage of JSerialComm library with Zigbee4Java
 *
 * @author Ziver Koc
 */
public class SerialPortJSC implements org.bubblecloud.zigbee.v3.SerialPort {
    private final static Logger LOGGER = LoggerFactory.getLogger(SerialPortJSC.class);

    /**
     * The default baud rate.
     */
    public static final int DEFAULT_BAUD_RATE = 38400;

    private final String portName;
    private final int baudRate;

    private SerialPort serialPort;
    private InputStream inputStream;
    private OutputStream outputStream;

    /**
     * Constructor which sets port name to given value and baud rate to default.
     */
    public SerialPortJSC(final String portName) {
        this(portName, DEFAULT_BAUD_RATE);
    }

    /**
     * Constructor setting port name and baud rate.
     * @param portName the port name
     * @param baudRate the baud rate
     */
    public SerialPortJSC(final String portName, final int baudRate) {
        this.portName = portName;
        this.baudRate = baudRate;
    }


    @Override
    public boolean open() {
        if (serialPort != null) {
            throw new RuntimeException("Serial port already open.");
        }

        try {
            LOGGER.info("Connecting to com port... (" + portName + ")");
            serialPort = SerialPort.getCommPort(portName);
            serialPort.setBaudRate(baudRate);
            serialPort.setComPortTimeouts(
                    SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

            if (!serialPort.openPort()) {
                LOGGER.error("Could not open port: " + portName);
                return false;
            }

            outputStream = serialPort.getOutputStream();
            inputStream = serialPort.getInputStream();
            return true;
        } catch (Exception e) {
            LOGGER.warn("Unable to open serial port: " + e.getMessage());
            return false;
        }
    }

    @Override
    public void close() {
        if (serialPort == null)
            return;

        try {
            inputStream.close();
            outputStream.flush();
            outputStream.close();
            serialPort.closePort();

            LOGGER.info("Serial portName '" + portName + "' closed.");

            serialPort = null;
            inputStream = null;
            outputStream = null;
        } catch (Exception e) {
            LOGGER.warn("Error closing portName portName: '" + portName + "'", e);
        }
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }
}
