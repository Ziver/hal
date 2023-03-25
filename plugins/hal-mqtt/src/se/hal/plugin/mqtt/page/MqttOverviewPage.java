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

    private Map<String, String> topicData = new HashMap<>();


    public MqttOverviewPage() {
        super("mqtt_overview");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "MQTT Overview").setWeight(9_000);

        HalMqttController controller = HalAbstractControllerManager.getController(HalMqttController.class);
        MqttBroker broker = controller.getBroker();
        broker.addGlobalSubscriber(this);
    }


    @Override
    public synchronized void dataPublished(String topic, byte[] data) {
        topicData.put(topic, new String(data, StandardCharsets.UTF_8));
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("topics", topicData.entrySet());
        return tmpl;
    }
}
