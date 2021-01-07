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

import com.zsmartsystems.zigbee.transport.ZigBeePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fazecast.jSerialComm.SerialPort;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * ZigBeePort implementation using jSerialComm library
 *
 * @author Ziver Koc
 */
public class ZigBeeJSerialCommPort implements ZigBeePort {
    private final static Logger logger = LoggerFactory.getLogger(ZigBeeJSerialCommPort.class);

    /**
     * The default baud rate.
     */
    public static final int DEFAULT_BAUD_RATE = 38400;

    /**
     * The default flow control.
     */
    public static final FlowControl DEFAULT_FLOW_CONTROL = FlowControl.FLOWCONTROL_OUT_NONE;


    private final String portName;
    private final int baudRate;
    private final FlowControl flowControl;

    private SerialPort serialPort;
    private InputStream serialInputstream;
    private OutputStream serialOutputstream;


    /**
     * Constructor which sets port name to given value and baud rate to default.
     *
     * @param portName the port name
     */
    public ZigBeeJSerialCommPort(String portName) {
        this(portName, DEFAULT_BAUD_RATE);
    }

    /**
     * Constructor setting port name and baud rate.
     *
     * @param portName the port name
     * @param baudRate the default baud rate
     */
    public ZigBeeJSerialCommPort(String portName, int baudRate) {
        this(portName, baudRate, DEFAULT_FLOW_CONTROL);
    }

    /**
     * Constructor setting port name and baud rate.
     *
     * @param portName the port name
     * @param baudRate the default baud rate
     */
    public ZigBeeJSerialCommPort(String portName, int baudRate, FlowControl flowControl) {
        this.portName = portName;
        this.baudRate = baudRate;
        this.flowControl = flowControl;
    }


    @Override
    public boolean open() {
        return open(baudRate);
    }

    @Override
    public boolean open(int baudRate) {
        return open(baudRate, flowControl);
    }

    @Override
    public boolean open(int baudRate, FlowControl flowControl) {
        try {
            openSerialPort(portName, baudRate, flowControl);

            return true;
        } catch (Exception e) {
            logger.error("Unable to open serial port: " + e.getMessage());
            return false;
        }
    }

    /**
     * Opens serial port.
     *
     * @param portName the port name
     * @param baudRate the baud rate
     * @param flowControl the flow control option
     */
    private void openSerialPort(String portName, int baudRate, FlowControl flowControl) {
        if (serialPort != null) {
            throw new RuntimeException("Serial port already open.");
        }

        logger.debug("Opening port {} at {} baud with {}.", portName, baudRate, flowControl);

        serialPort = SerialPort.getCommPort(portName);
        serialPort.setBaudRate(baudRate);
        serialPort.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        if (!serialPort.openPort()) {
            throw new RuntimeException("Error opening serial port: " + portName);
        }

        serialInputstream = serialPort.getInputStream();
        serialOutputstream = serialPort.getOutputStream();

        switch (flowControl) {
            case FLOWCONTROL_OUT_NONE:
                serialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
                break;
            case FLOWCONTROL_OUT_RTSCTS:
                serialPort.setFlowControl(
                        SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED);
                break;
            case FLOWCONTROL_OUT_XONOFF:
                serialPort.setFlowControl(
                        SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);
                break;
            default:
                break;
        }
    }

    @Override
    public void close() {
        if (serialPort == null)
            return;

        try {
            logger.info("Closing Serial port: '" + portName + "'");
            purgeRxBuffer();
            serialPort.closePort();

            serialInputstream = null;
            serialOutputstream = null;
            serialPort = null;
        } catch (Exception e) {
            logger.warn("Error closing portName portName: '" + portName + "'", e);
        }
    }

    @Override
    public void write(int value) {
        if (serialPort == null)
            throw new RuntimeException("Unable to write, Serial port is not open.");

        try {
            serialOutputstream.write(value);
        } catch (IOException e) {
            logger.error("Was unable to write to serial port.", e);
        }
    }

    @Override
    public int read() {
        return read(0);
    }

    @Override
    public int read(int timeout) {
        if (serialPort == null)
            throw new RuntimeException("Unable to read, Serial port is not open.");

        try {
            if (serialPort.getReadTimeout() != timeout)
                serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, timeout, 0);

            return serialInputstream.read();
        } catch (IOException e) {
            logger.error("Was unable to read from serial port.", e);
        }
        return -1;
    }

    @Override
    public void purgeRxBuffer() {
        if (serialPort == null)
            return;

        try {
            serialOutputstream.flush();
        } catch (IOException e) {
            logger.error("Was unable to flush serial data.", e);
        }
    }
}
