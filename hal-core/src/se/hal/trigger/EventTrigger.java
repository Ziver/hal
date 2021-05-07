package se.hal.trigger;

import se.hal.struct.Event;
import se.hal.util.ConfigEventValueProvider;
import zutil.log.LogUtil;
import zutil.ui.conf.Configurator;

import java.util.logging.Logger;

public class EventTrigger extends DeviceTrigger{
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable(value = "Event", valueProvider = ConfigEventValueProvider.class)
    protected Event device;


    @Override
    protected Event getDevice() {
        return device;
    }

    @Override
    public String toString(){
        Event event = getDevice();
        return "Trigger " + (triggerOnChange ? "on" : "when") +
                " event: " + device.getId() + " (" + (event != null ? event.getName() : null) + ")" +
                " == " + expectedData;
    }

}
