package se.hal.plugin.dummy;

import se.hal.intf.*;
import se.hal.struct.devicedata.OnOffEventData;

public class DummySwitchEvent implements DummyDevice, HalEventConfig {


    @Override
    public HalDeviceData generateData() {
        return new OnOffEventData(
                (int) (Math.random() * 10) < 5,
                System.currentTimeMillis()
        );
    }


    @Override
    public Class<? extends HalEventController> getDeviceControllerClass() {
        return DummyController.class;
    }

    @Override
    public Class<? extends HalEventData> getDeviceDataClass() {
        return OnOffEventData.class;
    }
}
