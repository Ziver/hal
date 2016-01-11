/*
 * Copyright (c) 2015 Ziver
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

package se.hal.plugin.tellstick;

import com.fazecast.jSerialComm.SerialPort;
import se.hal.HalContext;
import se.hal.intf.*;
import zutil.log.InputStreamLogger;
import zutil.log.LogUtil;
import zutil.log.OutputStreamLogger;
import zutil.struct.TimedHashSet;

import java.io.*;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This version of the TwoWaySerialComm example makes use of the
 * SerialPortEventListener to avoid polling.
 */
public class TellstickSerialComm implements Runnable, HalSensorController, HalEventController {
    private static final long TRANSMISSION_UNIQUENESS_TTL = 300; // milliseconds
    private static final Logger logger = LogUtil.getLogger();

    private SerialPort serial;
    private InputStream in;
    private OutputStream out;
    private TimedHashSet set; // To check for retransmissions

    private TellstickParser parser;
    private HalSensorReportListener sensorListener;
    private HalEventReportListener eventListener;

    private int registeredObjects;



    public TellstickSerialComm() {
        set = new TimedHashSet(TRANSMISSION_UNIQUENESS_TTL);
        parser = new TellstickParser();
        registeredObjects = 0;
    }

    @Override
    public void initialize() throws Exception {
        // Read properties
        String port = HalContext.getStringProperty("tellstick.com_port");
        if (port == null)
            port = "COM1"; // defaults

        connect(port);
    }

    public void connect(String portName) throws Exception {
        logger.info("Connecting to com port... ("+ portName +")");
        serial = SerialPort.getCommPort(portName);
        serial.setBaudRate(9600);
        if(!serial.openPort())
            throw new IOException("Could not open port: "+portName);
        serial.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        in  = new InputStreamLogger(serial.getInputStream());
        out = new OutputStreamLogger(serial.getOutputStream());

        Executors.newSingleThreadExecutor().execute(this);
    }

    public void close() {
        if(serial != null) {
            try {
                serial.closePort();
                in.close();
                out.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, null, e);
            }
        }
        serial = null;
        in = null;
        out = null;
    }


    public void run() {
        try {
            String data;

            while (in != null && (data = readLine()) != null) {
                if ((data.startsWith("+S") || data.startsWith("+T"))) {
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
                else {
                    if(!set.contains(data)) {
                        TellstickProtocol protocol = parser.decode(data);
                        if(protocol != null) {
                            if (protocol.getTimestamp() < 0)
                                protocol.setTimestamp(System.currentTimeMillis());
                            set.add(data);

                            if (sensorListener != null && protocol instanceof HalSensor)
                                sensorListener.reportReceived((HalSensor) protocol);
                            else if (eventListener != null && protocol instanceof HalEvent)
                                eventListener.reportReceived((HalEvent) protocol);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    /**
     * There seems to be an issue with read(...) methods only read() is working
     */
    private String readLine() throws IOException {
        StringBuilder str = new StringBuilder(50);
        char c = 0;
        while((c = (char)in.read()) >= 0){
            switch(c) {
                case '\n':
                case '\r':
                    if(str.length() > 0)
                        return str.toString();
                    break;
                default:
                    str.append(c);
            }
        }
        return str.toString();
    }


    @Override
    public void send(HalEvent event) {
        if(event instanceof TellstickProtocol)
            write((TellstickProtocol) event);
    }
    public synchronized void write(TellstickProtocol prot) {
        write(prot.encode());
        try {
            this.wait();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }
    public void write(String data) {
        try {
            for(int i=0; i<data.length();i++)
                out.write(0xFF & data.charAt(i));
            out.write('\n');
            out.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }


    @Override
    public void register(HalEvent event) {++registeredObjects;}
    @Override
    public void register(HalSensor sensor) {++registeredObjects;}

    @Override
    public void deregister(HalSensor sensor) {--registeredObjects;}
    @Override
    public void deregister(HalEvent event) {--registeredObjects;}

    @Override
    public int size() {
        return registeredObjects;
    }

    @Override
    public void setListener(HalEventReportListener listener) {
        eventListener = listener;
    }
    @Override
    public void setListener(HalSensorReportListener listener) {
        sensorListener = listener;
    }
}