package se.hal;

import se.hal.intf.*;
import se.hal.struct.Event;
import se.hal.util.HalDeviceUtil;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.plugin.PluginManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages all SensorController and EventController objects
 */
public class EventControllerManager extends HalAbstractControllerManager<HalEventController,Event,HalEventConfig>
        implements HalDeviceReportListener<HalEventConfig,HalEventData> {

    private static final Logger logger = LogUtil.getLogger();
    private static EventControllerManager instance;

    /** List of all registered events **/
    private List<Event> registeredEvents = Collections.synchronizedList(new ArrayList<>());
    /** List of auto detected events **/
    private List<Event> detectedEvents = Collections.synchronizedList(new ArrayList<>());


    public void initialize(PluginManager pluginManager) {
        super.initialize(pluginManager);
        instance = this;

        // Read in existing devices

        try {
            DBConnection db = HalContext.getDB();

            logger.info("Reading in existing events.");

            for (Event event : Event.getLocalEvents(db)) {
                EventControllerManager.getInstance().register(event);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to read in existing events.", e);
        }
    }

    // ----------------------------------------------------
    //                     EVENTS
    // ----------------------------------------------------

    /**
     * Register a Event instance on the manager.
     * The manager will start to save reported data for the registered Event.
     */
    @Override
    public void register(Event event) {
        if (event.getDeviceConfig() == null) {
            logger.warning("Event config is null: " + event);
            return;
        }
        if (!getAvailableDeviceConfigs().contains(event.getDeviceConfig().getClass())) {
            logger.warning("Event data plugin not available: " + event.getDeviceConfig().getClass());
            return;
        }

        logger.info("Registering new event(id: " + event.getId() + "): " + event.getDeviceConfig().getClass());
        Class<? extends HalEventController> c = event.getController();
        HalEventController controller = getControllerInstance(c);

        if (controller != null)
            controller.register(event.getDeviceConfig());
        registeredEvents.add(event);
        detectedEvents.remove(HalDeviceUtil.findDevice(event.getDeviceConfig(), detectedEvents)); // Remove if this device was detected
    }

    /**
     * Deregisters a Event from the manager.
     * Data reported on the Event will no longer be saved but already saved data will not be modified.
     * The Controller that owns the Event will be deallocated if it has no more registered devices.
     */
    @Override
    public void deregister(Event event){
        if (event.getDeviceConfig() == null) {
            logger.warning("Event config is null: "+ event);
            return;
        }

        Class<? extends HalEventController> c = event.getController();
        HalEventController controller = controllerMap.get(c);
        if (controller != null) {
            logger.info("Deregistering event(id: " + event.getId() + "): " + event.getDeviceConfig().getClass());
            controller.deregister(event.getDeviceConfig());
            registeredEvents.remove(event);
            removeControllerIfEmpty(controller);
        } else {
            logger.warning("Controller not instantiated: "+ event.getController());
        }
    }

    /**
     * @return a List of Sensor instances that have been registered to this manager
     */
    @Override
    public List<Event> getRegisteredDevices(){
        return registeredEvents;
    }

    /**
     * @return a List of Event instances that have been reported but not registered on the manager
     */
    @Override
    public List<Event> getDetectedDevices(){
        return detectedEvents;
    }

    /**
     * Removes all auto detected events.
     */
    @Override
    public void clearDetectedDevices(){
        detectedEvents.clear();
    }

    /**
     * Called by Controllers to report received Event data
     */
    @Override
    public void reportReceived(HalEventConfig eventConfig, HalEventData eventData) {
        try {
            DBConnection db = HalContext.getDB();
            Event event = HalDeviceUtil.findDevice(eventConfig, registeredEvents);

            if (event != null) {
                logger.finest("Received report from event(" + eventConfig.getClass().getSimpleName() + "): " + eventConfig);
                PreparedStatement stmt =
                        db.getPreparedStatement("INSERT INTO event_data_raw (timestamp, event_id, data) VALUES(?, ?, ?)");
                stmt.setLong(1, eventData.getTimestamp());
                stmt.setLong(2, event.getId());
                stmt.setDouble(3, eventData.getData());
                DBConnection.exec(stmt);
            }
            else { // unknown sensor
                logger.info("Received report from unregistered event" +
                        "(" + eventConfig.getClass().getSimpleName() + "): " + eventConfig);
                event = HalDeviceUtil.findDevice(eventConfig, detectedEvents);
                if (event == null) {
                    event = new Event();
                    detectedEvents.add(event);
                }
                event.setDeviceConfig(eventConfig);
            }
            event.setDeviceData(eventData);
            // call listeners
            for (HalDeviceReportListener<HalEventConfig,HalEventData> listener : event.getReportListeners())
                listener.reportReceived(event.getDeviceConfig(), eventData);

        }catch (SQLException e){
            logger.log(Level.WARNING, "Unable to store event report", e);
        }
    }

    public void send(Event event){
        HalEventController controller = getControllerInstance(event.getController());
        if (controller != null) {
            controller.send(event.getDeviceConfig(), event.getDeviceData());
            reportReceived(event.getDeviceConfig(), event.getDeviceData()); // save action to db
        }
        else
            logger.warning("No controller found for event id: "+ event.getId());
    }

    /**
     * @return all instantiated controllers.
     */
    @Override
    public Collection<HalEventController> getControllers() {
        return controllerMap.values();
    }


    public static EventControllerManager getInstance(){
        return instance;
    }
}
