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

package se.hal.plugin.mqtt;

import se.hal.HalServer;
import se.hal.daemon.HalMulticastDnsDaemon;
import se.hal.intf.*;
import se.hal.plugin.mqtt.detector.HalMqttDetector;
import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;
import zutil.InetUtil;
import zutil.ObjectUtil;
import zutil.log.LogUtil;
import zutil.net.mqtt.MqttBroker;
import zutil.net.mqtt.MqttSubscriptionListener;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HalMqttController implements HalAutostartController, MqttSubscriptionListener, HalEventController {
    private final Logger logger = LogUtil.getLogger();

    private MqttBroker mqttBroker;

    private List<HalMqttDetector> detectors = Collections.emptyList();
    private HashMap<String, List<HalMqttDeviceConfig>> topics = new HashMap<>();
    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();

    // --------------------------
    // Lifecycle methods
    // --------------------------

    @Override
    public void initialize() {
        try {
            if (HalMulticastDnsDaemon.getInstance() != null) {
                logger.info("Register MQTT in mDNS");
                InetAddress serverIp = InetUtil.getLocalInet4Address().get(0);
                HalMulticastDnsDaemon.getInstance().addDnsEntry("_mqtt._tcp.local", serverIp);
            } else {
                logger.info("mDNS not available");
            }

            logger.info("Starting up MQTT Server");
            mqttBroker = new MqttBroker();
            mqttBroker.addGlobalSubscriber(this);
            mqttBroker.start();

            detectors = HalServer.getPluginManager().getEnabledPluginSingletons(HalMqttDetector.class);

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to initialize MQTT plugin.", e);

            close();
        }
    }

    @Override
    public void close(){
        if (mqttBroker != null) {
            logger.info("Shutting down MQTT Server");
            mqttBroker.close();
            mqttBroker = null;
        }
    }

    // --------------------------
    // MQTT Methods
    // --------------------------

    @Override
    public void dataPublished(String topic, byte[] data) {
        logger.finest("MQTT data published(topic: " + topic + "): " + new String(data, StandardCharsets.UTF_8));

        List<HalMqttDeviceConfig> registeredDevices = topics.get(topic);

        // Handle existing devices

        if (!ObjectUtil.isEmpty(registeredDevices)) {
            for (HalMqttDeviceConfig deviceConfig : registeredDevices) {
                HalDeviceData deviceData = deviceConfig.getDeviceData(data);

                if (deviceListeners != null) {
                    for (HalDeviceReportListener deviceListener : deviceListeners) {
                        deviceListener.reportReceived(deviceConfig, deviceData);
                    }
                }
            }
        }

        // Handle new devices

        for (HalMqttDetector detector : detectors) {
            List<HalMqttDeviceConfig> detectedDevices = detector.parseTopic(topic, data);

            // Check if we already know the device
            if (!ObjectUtil.isEmpty(detectedDevices)) {
                for (HalMqttDeviceConfig detectedDeviceConfig : detectedDevices) {
                    // Only handle unknown devices
                    if (!registeredDevices.contains(detectedDeviceConfig)) {
                        HalDeviceData deviceData = detectedDeviceConfig.getDeviceData(data);

                        if (deviceListeners != null) {
                            for (HalDeviceReportListener deviceListener : deviceListeners) {
                                deviceListener.reportReceived(detectedDeviceConfig, deviceData);
                            }
                        }
                    }
                }
            }
        }
    }

    public MqttBroker getBroker() {
        return mqttBroker;
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof HalMqttDeviceConfig) {
            HalMqttDeviceConfig mqttEvent = (HalMqttDeviceConfig) deviceConfig;

            if (!topics.containsKey(mqttEvent.getTopic()))
                topics.put(mqttEvent.getTopic(), new ArrayList<>());
            topics.get(mqttEvent.getTopic()).add(mqttEvent);
        } else {
            throw new IllegalArgumentException(
                    "Device config is not an instance of " + HalMqttDeviceConfig.class + ": " + deviceConfig.getClass());
        }
    }

    @Override
    public void deregister(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof HalMqttDeviceConfig) {
            HalMqttDeviceConfig mqttEvent = (HalMqttDeviceConfig) deviceConfig;
            if (topics.containsKey(mqttEvent.getTopic()))
                topics.get(mqttEvent.getTopic()).remove(deviceConfig);
        }
    }

    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) {
        if (eventConfig instanceof HalMqttDeviceConfig) {
            HalMqttDeviceConfig mqttEvent = (HalMqttDeviceConfig) eventConfig;
            mqttBroker.publish(mqttEvent.getTopic(), Double.toString(eventData.getData()).getBytes());
        } else
            throw new IllegalArgumentException(
                "Device config is not an instance of " + HalMqttDeviceConfig.class + ": " + eventConfig.getClass());
    }

    @Override
    public int size() {
        int size = 0;
        for (List<HalMqttDeviceConfig> devices : topics.values()) {
            size += devices.size();
        }
        return size;
    }

    @Override
    public void addListener(HalDeviceReportListener listener) {
        if (!deviceListeners.contains(listener))
            deviceListeners.add(listener);
    }
}
