package se.hal.trigger;

import se.hal.struct.Sensor;
import se.hal.util.ConfigSensorValueProvider;
import zutil.log.LogUtil;
import zutil.ui.conf.Configurator;

import java.util.logging.Logger;

public class SensorTrigger extends DeviceTrigger {
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable(value = "Sensor", valueProvider = ConfigSensorValueProvider.class)
    protected Sensor device;


    @Override
    protected Sensor getDevice() {
        return device;
    }

    @Override
    public String toString(){
        Sensor sensor = getDevice();
        return "Trigger " + (triggerOnChange ? "on" : "when") +
                " sensor: " + sensor.getId() +" (" + (sensor != null ? sensor.getName() : null) + ")" +
                " == " + expectedData;
    }

}
