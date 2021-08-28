package se.hal.plugin.dummy.device;

import se.hal.intf.HalDeviceData;
import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventController;
import se.hal.intf.HalEventData;
import se.hal.plugin.dummy.DummyController;
import se.hal.plugin.dummy.DummyDevice;
import se.hal.struct.devicedata.OnOffEventData;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
}
