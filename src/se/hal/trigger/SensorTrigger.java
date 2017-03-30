package se.hal.trigger;

import se.hal.HalContext;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.log.LogUtil;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SensorTrigger extends DeviceTrigger{
    private static final Logger logger = LogUtil.getLogger();

    @Override
    protected Sensor getDevice(long id) {
        try {
            if (id >= 0)
                return Sensor.getSensor(HalContext.getDB(), id);
        } catch (SQLException e){ logger.log(Level.SEVERE, null, e);}
        return null;
    }

    @Override
    public String toString(){
        Sensor sensor = getDevice(deviceId);
        return "Trigger " + (triggerOnChange ? "on" : "when") +
                " sensor: "+ deviceId +" ("+(sensor != null ? sensor.getName() : null) + ")" +
                " == "+ expectedData;
    }

}
