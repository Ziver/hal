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
import se.hal.plugin.tellstick.cmd.TellstickCmd;
import zutil.log.LogUtil;
import zutil.struct.TimedHashSet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Tellstick serial port controller, this class handles all tellstick
 * communication and reporting to Hal
 */
public class TellstickSerialComm implements Runnable,
        HalSensorController, HalEventController, HalAutostartController {
    private static final Logger logger = LogUtil.getLogger();

    private static String CONFIG_TELLSTICK_COM_PORT = "tellstick.com_port";
    private static final long TRANSMISSION_UNIQUENESS_TTL = 1000; // milliseconds

    private static TellstickSerialComm instance; // Todo: Don't like this but it is the best I could come up with

    private SerialPort serial;
    private InputStream in;
    private OutputStream out;
    private TimedHashSet<String> receivedTransmissionSet = new TimedHashSet<>(TRANSMISSION_UNIQUENESS_TTL); // To check for duplicate transmissions

    protected TellstickParser parser = new TellstickParser();

    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();
    private List<HalDeviceConfig> registeredDevices = Collections.synchronizedList(new ArrayList<HalDeviceConfig>());


    public TellstickSerialComm() {}

    // --------------------------
    // Lifecycle methods
    // --------------------------

    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_TELLSTICK_COM_PORT);
    }

    @Override
    public void initialize() throws Exception {
         initialize(HalContext.getStringProperty(CONFIG_TELLSTICK_COM_PORT));
    }

    public void initialize(String portName) throws Exception {
        if (instance != null)
            throw new IllegalStateException("There is a previous TellstickSerialComm instance, only one allowed");
        instance = this;

        logger.info("Connecting to com port... ("+ portName +")");
        serial = SerialPort.getCommPort(portName);
        serial.setBaudRate(9600);
        if (!serial.openPort())
            throw new IOException("Could not open port: "+portName);
        serial.setComPortTimeouts(
                SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        in  = serial.getInputStream();
        out = serial.getOutputStream();

        Executors.newSingleThreadExecutor().execute(this);
    }

    public void close() {
        if (serial != null) {
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
        instance = null;
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

    // --------------------------
    // Tellstick methods
    // --------------------------

    /**
     * There seems to be an issue with read(...) methods, only read() is working
     */
    private String readLine() throws IOException {
        StringBuilder str = new StringBuilder(50);
        int c;
        while ((c = in.read()) >= 0) {
            switch(c) {
                case -1:
                    return null;
                case '\n':
                case '\r':
                    if (str.length() > 0)
                        return str.toString();
                    break;
                default:
                    str.append((char)c);
            }
        }
        return str.toString();
    }
    protected void handleLine(String data) {
        List<TellstickDecodedEntry> decodeList = parser.decode(data);
        for (TellstickDecodedEntry entry : decodeList) {
            if (entry.getData().getTimestamp() < 0)
                entry.getData().setTimestamp(System.currentTimeMillis());

            boolean registered = registeredDevices.contains(entry.getDevice());
            if (registered && !receivedTransmissionSet.contains(data) || // check for duplicates transmissions of registered devices
                    !registered && receivedTransmissionSet.contains(data)) { // required duplicate transmissions before reporting unregistered devices

                // Check for registered device that are in the same group
                if (entry.getDevice() instanceof TellstickDeviceGroup) {
                    TellstickDeviceGroup groupProtocol = (TellstickDeviceGroup) entry.getDevice();

                    for (int i = 0; i < registeredDevices.size(); ++i) { // Don't use foreach for concurrency reasons
                        HalDeviceConfig childDevice = registeredDevices.get(i);

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
        }

        receivedTransmissionSet.add(data);
    }
    private void reportEvent(HalDeviceConfig tellstickDevice, HalDeviceData deviceData) {
        for (HalDeviceReportListener deviceListener : deviceListeners) {
            deviceListener.reportReceived(tellstickDevice, deviceData);
        }
    }

    @Override
    public void send(HalEventConfig deviceConfig, HalEventData deviceData) {
        if (deviceConfig instanceof TellstickDevice) {
            TellstickDevice tellstickDevice = (TellstickDevice) deviceConfig;
            TellstickProtocol prot = TellstickParser.getProtocolInstance(
                    tellstickDevice.getProtocolName(),
                    tellstickDevice.getModelName());
            TellstickCmd cmd = prot.encode(deviceConfig, deviceData);
            if (cmd != null)
                write(cmd.getTransmissionString());

            parser.waitSendConformation();
            deviceData.setTimestamp(System.currentTimeMillis());
        }
    }
    private void write(String data) {
        if (data == null)
            return;
        try {
            for (int i=0; i<data.length();i++)
                out.write(0xFF & data.charAt(i));
            out.write('\n');
            out.flush();
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    // --------------------------
    // Hal methods
    // --------------------------

    @Override
    public void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof TellstickDevice)
            registeredDevices.add(deviceConfig);
        else throw new IllegalArgumentException(
                "Device config is not an instance of " + TellstickDevice.class + ": " + deviceConfig.getClass());
    }

    public <T> List<T> getRegisteredDevices(Class<T> clazz) {
        ArrayList<T> list = new ArrayList<>();
        for (HalDeviceConfig device : registeredDevices) {
            if (clazz.isAssignableFrom(device.getClass()))
                list.add((T) device);
        }
        return list;
    }

    @Override
    public void deregister(HalDeviceConfig deviceConfig) {
        registeredDevices.remove(deviceConfig);
    }

    @Override
    public int size() {
        return registeredDevices.size();
    }

    @Override
    public void addListener(HalDeviceReportListener listener) {
        deviceListeners.add(listener);
    }


    public static TellstickSerialComm getInstance() {
        return instance;
    }
}