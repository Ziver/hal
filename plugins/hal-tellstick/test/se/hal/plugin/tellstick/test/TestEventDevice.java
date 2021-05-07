package se.hal.plugin.tellstick.test;

import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventController;
import se.hal.intf.HalEventData;
import se.hal.plugin.tellstick.TellstickDevice;

public class TestEventDevice implements TellstickDevice, HalEventConfig {
    public int testData;


    @Override
    public String getProtocolName() {
        return "test-prot";
    }

    @Override
    public String getModelName() {
        return "test-model";
    }


    @Override
    public Class<? extends HalEventController> getDeviceControllerClass() {
        return null;
    }

    @Override
    public Class<? extends HalEventData> getDeviceDataClass() {
        return null;
    }

    @Override
    public boolean equals(Object obj) {
        return testData == ((TestEventDevice) obj).testData;
    }
}
