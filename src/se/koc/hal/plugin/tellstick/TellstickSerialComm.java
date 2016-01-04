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

package se.koc.hal.plugin.tellstick;

import com.fazecast.jSerialComm.SerialPort;
import se.koc.hal.intf.HalSensor;
import se.koc.hal.intf.HalSensorController;
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
public class TellstickSerialComm implements Runnable, HalSensorController {
    private static final long TRANSMISSION_UNIQUENESS_TTL = 300; // milliseconds
    private static final Logger logger = LogUtil.getLogger();

    private SerialPort serial;
    private BufferedReader in;
    private BufferedWriter out;
    private TimedHashSet set; // To check for retransmissions

    private TellstickParser parser;
    private TellstickChangeListener listener;


    public TellstickSerialComm() throws Exception {
        set = new TimedHashSet(TRANSMISSION_UNIQUENESS_TTL);
        parser = new TellstickParser();
        connect("COM6");
    }

    public void connect(String portName) throws Exception {
        serial = SerialPort.getCommPort(portName);
        serial.setBaudRate(9600);
        if(!serial.openPort())
            throw new IOException("Could not open port: "+portName);
        serial.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);

        in  = new BufferedReader(new InputStreamReader(new InputStreamLogger(serial.getInputStream()),  "UTF-8"));
        out = new BufferedWriter(new OutputStreamWriter(new OutputStreamLogger(serial.getOutputStream()), "UTF-8"));
        Executors.newSingleThreadExecutor().submit(this);
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
            while (in != null && (data = in.readLine()) != null) {
                if ((data.startsWith("+S") || data.startsWith("+T"))) {
                    synchronized (this) {
                        this.notifyAll();
                    }
                }
                else {
                    if(!set.contains(data)) {
                        TellstickProtocol protocol = parser.decode(data);
                        set.add(data);
                        if (listener != null) {
                            listener.stateChange(protocol);
                        }
                    }
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
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

    public void setListener(TellstickChangeListener listener){
        this.listener = listener;
    }



    @Override
    public void register(HalSensor sensor) {}
    @Override
    public void deregister(HalSensor sensor) {}
    @Override
    public int size() {
        return 0;
    }

    @Override
    public void setListener(SensorReportListener listener) {

    }
}