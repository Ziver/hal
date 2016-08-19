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
import se.hal.plugin.tellstick.TellstickProtocol.TellstickDecodedEntry;
import zutil.log.LogUtil;
import zutil.struct.TimedHashSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * This version of the TwoWaySerialComm example makes use of the
 * SerialPortEventListener to avoid polling.
 */
public class TellstickSerialComm implements Runnable,
        HalSensorController, HalEventController, HalAutoScannableController {
    private static final long TRANSMISSION_UNIQUENESS_TTL = 1000; // milliseconds
    private static final Logger logger = LogUtil.getLogger();

    private SerialPort serial;
    private InputStream in;
    private OutputStream out;
    private TimedHashSet<String> set; // To check for duplicate transmissions

    protected TellstickParser parser;
    private HalSensorReportListener sensorListener;
    private HalEventReportListener eventListener;

    private List<TellstickDevice> registeredDevices;



    public TellstickSerialComm() {
        set = new TimedHashSet<>(TRANSMISSION_UNIQUENESS_TTL);
        parser = new TellstickParser();
        registeredDevices = Collections.synchronizedList(new ArrayList<TellstickDevice>());
    }

    @Override
    public boolean isAvailable() {
        return HalContext.getStringProperty("tellstick.com_port") != null;
    }
    @Override
    public void initialize() throws Exception {
        // Read properties
        String port = HalContext.getStringProperty("tellstick.com_port");
        if (port == null)
            port = "COM1"; // defaults

        initialize(port);
    }

    public void initialize(String portName) throws Exception {
        logger.info("Connecting to com port... ("+ portName +")");
        serial = SerialPort.getCommPort(portName);
        serial.setBaudRate(9600);
        if(!serial.openPort())
            throw new IOException("Could not open port: "+portName);
        serial.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        in  = serial.getInputStream();
        out = serial.getOutputStream();

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
                handleLine(data);
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    /**
     * There seems to be an issue with read(...) methods, only read() is working
     */
    private String readLine() throws IOException {
        StringBuilder str = new StringBuilder(50);
        int c;
        while((c = in.read()) >= 0){
            switch(c) {
                case -1:
                    return null;
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
    protected void handleLine(String data){
        List<TellstickDecodedEntry> decodeList = parser.decode(data);
        for (TellstickDecodedEntry entry : decodeList) {
            if (entry.getData().getTimestamp() < 0)
                entry.getData().setTimestamp(System.currentTimeMillis());

            boolean registered = registeredDevices.contains(entry.getDevice());
            if (registered && !set.contains(data) || // check for duplicates transmissions of registered devices
                    !registered && set.contains(data)) { // required duplicate transmissions before reporting unregistered devices

                //Check for registered device that are in the same group
                if (entry.getDevice() instanceof TellstickDeviceGroup) {
                    TellstickDeviceGroup groupProtocol = (TellstickDeviceGroup) entry.getDevice();
                    for (int i = 0; i < registeredDevices.size(); ++i) { // Don't use foreach for concurrency reasons
                        TellstickDevice childDevice = registeredDevices.get(i);
                        if (childDevice instanceof TellstickDeviceGroup &&
                                groupProtocol.equalsGroup((TellstickDeviceGroup)childDevice) &&
                                !entry.getDevice().equals(childDevice)) {
                            reportEvent(childDevice, entry.getData());
                        }
                    }
                }
                // Report source event
                reportEvent(entry.getDevice(), entry.getData());
            }
            set.add(data);
        }
    }
    private void reportEvent(TellstickDevice tellstickDevice, HalDeviceData deviceData){
        if (sensorListener != null && tellstickDevice instanceof HalSensorConfig)
                sensorListener.reportReceived((HalSensorConfig) tellstickDevice, deviceData);
        else if (eventListener != null && tellstickDevice instanceof HalEventConfig)
                eventListener.reportReceived((HalEventConfig) tellstickDevice, deviceData);
    }

    @Override
    public void send(HalEventConfig deviceConfig, HalEventData deviceData) {
        if(deviceConfig instanceof TellstickDevice) {
            TellstickDevice tellstickDevice = (TellstickDevice) deviceConfig;
            TellstickProtocol prot = TellstickParser.getProtocolInstance(
                    tellstickDevice.getProtocolName(),
                    tellstickDevice.getModelName());
            write(prot.encode(deviceConfig, deviceData));

            parser.waitSendConformation();
            deviceData.setTimestamp(System.currentTimeMillis());
        }
    }
    private void write(String data) {
        if (data == null)
            return;
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
    public void register(HalEventConfig event) {
        if(event instanceof TellstickDevice)
            registeredDevices.add((TellstickDevice) event);
        else throw new IllegalArgumentException(
                "Device config is not an instance of "+TellstickDevice.class+": "+event.getClass());
    }
    @Override
    public void register(HalSensorConfig sensor) {
        if(sensor instanceof TellstickDevice)
            registeredDevices.add((TellstickDevice) sensor);
        else throw new IllegalArgumentException(
                "Device config is not an instance of "+TellstickDevice.class+": "+sensor.getClass());
    }

    @Override
    public void deregister(HalEventConfig event) {
        registeredDevices.remove(event);
    }
    @Override
    public void deregister(HalSensorConfig sensor) {
        registeredDevices.remove(sensor);
    }

    @Override
    public int size() {
        return registeredDevices.size();
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