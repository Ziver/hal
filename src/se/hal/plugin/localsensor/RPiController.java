package se.hal.plugin.localsensor;

import se.hal.intf.HalSensor;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorReportListener;

/**
 * Created by ezivkoc on 2016-01-14.
 */
public class RPiController implements HalSensorController {
    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void register(HalSensor sensor) {

    }

    @Override
    public void deregister(HalSensor sensor) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void setListener(HalSensorReportListener listener) {

    }

    @Override
    public void close() {

    }
}
