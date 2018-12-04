package se.hal.trigger;

import se.hal.HalContext;
import se.hal.struct.Event;
import zutil.log.LogUtil;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EventTrigger extends DeviceTrigger{
    private static final Logger logger = LogUtil.getLogger();

    @Override
    protected Event getDevice(long id) {
        try {
            if (id >= 0)
                return Event.getEvent(HalContext.getDB(), id);
        } catch (SQLException e){ logger.log(Level.SEVERE, null, e);}
        return null;
    }

    @Override
    public String toString(){
        Event event = getDevice(deviceId);
        return "Trigger " + (triggerOnChange ? "on" : "when") +
                " event: "+ deviceId +" ("+(event != null ? event.getName() : null) + ")" +
                " == "+ expectedData;
    }

}
