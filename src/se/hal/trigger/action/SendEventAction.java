package se.hal.trigger.action;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalAction;
import se.hal.intf.HalEventData;
import se.hal.struct.Event;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.ui.Configurator;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SendEventAction implements HalAction {
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable(order = 1, value = "Event Device ID")
    private int eventId;
    @Configurator.Configurable(order = 1, value = "Data to Send")
    private double data;


    @Override
    public void execute() {
        try {
            DBConnection db = HalContext.getDB();
            Event event = Event.getEvent(db, eventId);
            if (event != null) {
                HalEventData dataObj = event.getDeviceConfig().getEventDataClass().newInstance();
                dataObj.setData(data);
                event.setDeviceData(dataObj);
                // Send
                ControllerManager.getInstance().send(event);
            }
            else
                logger.warning("Unable to find event with id: "+ eventId);
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }


    public String toString(){
        return "Send event: "+ eventId +" with data: "+ data;
    }
}
