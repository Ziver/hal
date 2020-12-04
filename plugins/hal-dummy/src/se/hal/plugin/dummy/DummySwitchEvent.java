package se.hal.plugin.dummy;

import se.hal.intf.*;
import se.hal.struct.devicedata.SwitchEventData;
import se.hal.struct.devicedata.TemperatureSensorData;

public class DummySwitchEvent implements HalEventConfig {


    public DummySwitchEvent() { }


    @Override
    public Class<? extends HalEventController> getEventControllerClass() {
        return DummyController.class;
    }

    @Override
    public Class<? extends HalEventData> getEventDataClass() {
        return SwitchEventData.class;
    }
}
