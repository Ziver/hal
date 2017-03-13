package se.hal.trigger;

import se.hal.HalContext;
import se.hal.TriggerManager;
import se.hal.intf.HalDeviceReportListener;
import se.hal.intf.HalEventData;
import se.hal.intf.HalTrigger;
import se.hal.struct.Event;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.ui.Configurator;
import zutil.ui.Configurator.PostConfigurationActionListener;
import zutil.ui.Configurator.PreConfigurationActionListener;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class EventTrigger implements HalTrigger,
        PreConfigurationActionListener,
        PostConfigurationActionListener, HalDeviceReportListener<Event> {

    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable("Event Device ID")
    private int eventId = -1;
    @Configurator.Configurable("Trigger only on change")
    private boolean triggerOnChange = true;
    @Configurator.Configurable("Data to compare to")
    private double expectedData;

    private transient HalEventData receivedData;


    private Event getEvent(long id){
        try {
            if (eventId >= 0)
                return Event.getEvent(HalContext.getDB(), eventId);
        } catch (SQLException e){ logger.log(Level.SEVERE, null, e);}
        return null;
    }
    @Override
    public void preConfigurationAction(Configurator configurator, Object obj) {
        Event event = getEvent(eventId);
        if (event != null)
            event.removeReportListener(this);
        reset();
    }
    @Override
    public void postConfigurationAction(Configurator configurator, Object obj) {
        Event event = getEvent(eventId);
        if (event != null)
            event.addReportListener(this);
    }

    @Override
    public void receivedReport(Event device) {
        receivedData = device.getDeviceData();
        // Instant trigger evaluation
        if (triggerOnChange)
            TriggerManager.getInstance().evaluateAndExecute();
    }


    @Override
    public boolean evaluate() {
        if (receivedData != null)
            return expectedData == receivedData.getData();
        return false;
    }

    @Override
    public void reset() {
        if (triggerOnChange) // only reset if we want to trigger on change
            receivedData = null;
    }


    @Override
    public String toString(){
        Event event = getEvent(eventId);
        return "Trigger " + (triggerOnChange ? "on" : "when") +
                " event: "+eventId+" ("+(event != null ? event.getName() : null) + ")" +
                " == "+ expectedData;
    }

}
