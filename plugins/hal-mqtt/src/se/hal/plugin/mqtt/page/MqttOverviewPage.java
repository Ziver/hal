/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Ziver Koc
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

package se.hal.plugin.mqtt.page;

import se.hal.HalContext;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalWebPage;
import se.hal.plugin.mqtt.HalMqttController;
import zutil.io.file.FileUtil;
import zutil.net.mqtt.MqttBroker;
import zutil.net.mqtt.MqttSubscriptionListener;
import zutil.parser.Templator;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;


public class MqttOverviewPage extends HalWebPage implements MqttSubscriptionListener {
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/mqtt_overview.tmpl";

    private MqttBroker broker;
    private Map<String, TopicDTO> topicData = new HashMap<>();


    public MqttOverviewPage() {
        super("mqtt_overview");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "MQTT Overview").setWeight(9_000);

        HalMqttController controller = HalAbstractControllerManager.getController(HalMqttController.class);
        broker = controller.getBroker();
        broker.addGlobalSubscriber(this);
    }


    @Override
    public synchronized void dataPublished(String topic, byte[] data) {
        if (!topicData.containsKey(topic)) {
            topicData.put(topic, new TopicDTO(topic));
        }

        topicData.get(topic).value = new String(data, StandardCharsets.UTF_8);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        if (request.containsKey("topic") && request.containsKey("value")) {
            broker.publish(request.get("topic"), request.get("value"));
        }

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("topics", topicData.values());
        return tmpl;
    }


    public class TopicDTO {
        public String topicName;
        public String value;

        public TopicDTO(String topicName) {
            this.topicName = topicName;
        }

        public int getSubscriberCount() {
            return broker.getSubscriberCount(topicName);
        }
    }
}
