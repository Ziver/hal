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

import se.hal.intf.*;
import se.hal.plugin.mqtt.device.HalMqttDeviceConfig;
import se.hal.plugin.mqtt.device.HalMqttDeviceData;
import zutil.InetUtil;
import zutil.log.LogUtil;
import zutil.net.dns.MulticastDnsServer;
import zutil.net.mqtt.MqttBroker;
import zutil.net.mqtt.MqttSubscriptionListener;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HalMqttController implements HalAutoScannableController, MqttSubscriptionListener, HalEventController {
    private final Logger logger = LogUtil.getLogger();

    private MulticastDnsServer mDns;
    private MqttBroker mqttBroker;

    private HashMap<String, HalMqttDeviceConfig> topics = new HashMap<>();
    private HalEventReportListener eventListener;

    // --------------------------
    // Lifecycle methods
    // --------------------------

    @Override
    public void initialize() {
        try {
            InetAddress serverIp = InetUtil.getLocalInet4Address().get(0);

            mDns = new MulticastDnsServer();
            mDns.addEntry("_mqtt.tcp", serverIp);
            mDns.addEntry("hal.local", serverIp);

            mqttBroker = new MqttBroker();

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Unable to initialize MQTT plugin.", e);

            close();
        }
    }

    @Override
    public boolean isAvailable() {
        return mDns != null && mqttBroker != null;
    }

    @Override
    public void close(){
        if (mDns != null) {
            mDns.close();
            mDns = null;
        }

        if (mqttBroker != null) {
            mqttBroker.close();
            mqttBroker = null;
        }
    }

    // --------------------------
    // MQTT Methods
    // --------------------------

    @Override
    public void dataPublished(String topic, byte[] data) {
        HalMqttDeviceConfig eventConfig = topics.get(topic);

        if (eventConfig != null && data.length > 0) {
            HalMqttDeviceData eventData = new HalMqttDeviceData(data);

            if (eventListener != null) {
                eventListener.reportReceived(eventConfig, eventData);
            }
        }
    }

    // --------------------------
    // Hal Methods
    // --------------------------

    @Override
    public void register(HalEventConfig eventConfig) {
        if(eventConfig instanceof HalMqttDeviceConfig) {
            HalMqttDeviceConfig mqttEvent = (HalMqttDeviceConfig) eventConfig;
            topics.put(mqttEvent.getTopic(), mqttEvent);
        } else throw new IllegalArgumentException(
                "Device config is not an instance of " + HalMqttDeviceConfig.class + ": " + eventConfig.getClass());
    }

    @Override
    public void deregister(HalEventConfig eventConfig) {
        if(eventConfig instanceof HalMqttDeviceConfig) {
            HalMqttDeviceConfig mqttEvent = (HalMqttDeviceConfig) eventConfig;
            topics.remove(mqttEvent.getTopic());
        }
    }

    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) {
        if(eventConfig instanceof HalMqttDeviceConfig) {
            HalMqttDeviceConfig mqttEvent = (HalMqttDeviceConfig) eventConfig;
            mqttBroker.publish(mqttEvent.getTopic(), Double.toString(eventData.getData()).getBytes());
        } else throw new IllegalArgumentException(
                "Device config is not an instance of " + HalMqttDeviceConfig.class + ": " + eventConfig.getClass());
    }

    @Override
    public int size() {
        return topics.size();
    }

    @Override
    public void setListener(HalEventReportListener listener) {
        eventListener = listener;
    }
}