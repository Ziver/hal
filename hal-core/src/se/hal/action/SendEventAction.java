package se.hal.action;

import se.hal.EventControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalAction;
import se.hal.intf.HalEventData;
import se.hal.struct.Event;
import se.hal.util.ConfigEventValueProvider;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.ui.conf.Configurator;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class SendEventAction implements HalAction {
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable(value = "Event Device", valueProvider = ConfigEventValueProvider.class)
    private Event event;
    @Configurator.Configurable("Data to Send")
    private double data;


    @Override
    public void execute() {
        try {
            DBConnection db = HalContext.getDB();
            if (event != null) {
                HalEventData dataObj = (HalEventData) event.getDeviceConfig().getDeviceDataClass().newInstance();
                dataObj.setData(data);
                event.setDeviceData(dataObj);
                // Send
                EventControllerManager.getInstance().send(event);
            }
            else
                logger.warning("Unable to find event with id: "+ event.getId());
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }


    public String toString(){
        return "Send event: " + event.getId() +
                " (" + (event!=null ? event.getName() : null) + ")" +
                " with data: " + data;
    }
}
