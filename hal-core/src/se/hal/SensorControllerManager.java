package se.hal;

import se.hal.intf.*;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.plugin.PluginManager;
import zutil.ui.Configurator;
import zutil.ui.Configurator.PostConfigurationActionListener;
import zutil.ui.Configurator.PreConfigurationActionListener;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages all SensorController and EventController objects
 */
public class SensorControllerManager extends HalAbstractControllerManager<HalAbstractController, Sensor, HalSensorConfig> implements
        HalDeviceReportListener<HalSensorConfig, HalSensorData>,
        PreConfigurationActionListener,
        PostConfigurationActionListener {
    private static final Logger logger = LogUtil.getLogger();
    private static SensorControllerManager instance;


    /** All available sensor plugins **/
    private List<Class<? extends HalSensorConfig>> availableSensors = new ArrayList<>();
    /** List of all registered sensors **/
    private List<Sensor> registeredSensors = Collections.synchronizedList(new ArrayList<>());
    /** List of auto detected sensors **/
    private List<Sensor> detectedSensors = Collections.synchronizedList(new ArrayList<>());
    /** List of sensors that are currently being reconfigured **/
    private List<Sensor> limboSensors = Collections.synchronizedList(new LinkedList<>());

    // ----------------------------------------------------
    //                     SENSORS
    // ----------------------------------------------------

    /**
     * Register a Sensor instance on the manager.
     * The manager will start to save reported data for the registered Sensor.
     */
    @Override
    public void register(Sensor sensor) {
        if(sensor.getDeviceConfig() == null) {
            logger.warning("Sensor config is null: " + sensor);
            return;
        }
        if(!availableSensors.contains(sensor.getDeviceConfig().getClass())) {
            logger.warning("Sensor data plugin not available: " + sensor.getDeviceConfig().getClass());
            return;
        }

        logger.info("Registering new sensor(id: " + sensor.getId() + "): " + sensor.getDeviceConfig().getClass());
        Class<? extends HalAbstractController> c = sensor.getController();
        HalAbstractController controller = getControllerInstance(c);

        if(controller != null)
            controller.register(sensor.getDeviceConfig());
        registeredSensors.add(sensor);
        detectedSensors.remove(findSensor(sensor.getDeviceConfig(), detectedSensors)); // Remove if this device was detected
    }

    /**
     * Deregisters a Sensor from the manager.
     * Data reported on the Sensor will no longer be saved but already saved data will not be modified.
     * The Controller that owns the Sensor will be deallocated if it has no more registered devices.
     */
    @Override
    public void deregister(Sensor sensor){
        if(sensor.getDeviceConfig() == null) {
            logger.warning("Sensor config is null: "+ sensor);
            return;
        }

        Class<? extends HalAbstractController> c = sensor.getController();
        HalAbstractController controller = controllerMap.get(c);
        if (controller != null) {
            logger.info("Deregistering sensor(id: " + sensor.getId() + "): " + sensor.getDeviceConfig().getClass());
            controller.deregister(sensor.getDeviceConfig());
            registeredSensors.remove(sensor);
            removeControllerIfEmpty(controller);
        } else {
            logger.warning("Controller not instantiated: " + sensor.getController());
        }
    }

    /**
     * Registers a Sensor class type as usable by the manager
     */
    @Override
    public void addAvailableDevice(Class<? extends HalSensorConfig> sensorConfigClass) {
        if (!availableSensors.contains(sensorConfigClass))
            availableSensors.add(sensorConfigClass);
    }

    /**
     * @return a List of all available Sensors that can be registered to this manager
     */
    @Override
    public List<Class<? extends HalSensorConfig>> getAvailableDeviceConfigs(){
        return availableSensors;
    }

    /**
     * @return a List of Sensor instances that have been registered to this manager
     */
    @Override
    public List<Sensor> getRegisteredDevices(){
        return registeredSensors;
    }


    /**
     * @return a List of Sensor instances that have been reported but not registered on the manager
     */
    @Override
    public List<Sensor> getDetectedDevices(){
        return detectedSensors;
    }

    /**
     * Removes all auto detected sensors.
     */
    @Override
    public void clearDetectedDevices(){
        detectedSensors.clear();
    }

    /**
     * Called by Controllers to report received Sensor data
     */
    @Override
    public void reportReceived(HalSensorConfig sensorConfig, HalSensorData sensorData) {
        try{
            DBConnection db = HalContext.getDB();
            Sensor sensor = findSensor(sensorConfig, registeredSensors);

            if (sensor != null) {
                logger.finest("Received report from sensor(" + sensorConfig.getClass().getSimpleName() + "): " + sensorConfig);
                PreparedStatement stmt =
                        db.getPreparedStatement("INSERT INTO sensor_data_raw (timestamp, sensor_id, data) VALUES(?, ?, ?)");
                stmt.setLong(1, sensorData.getTimestamp());
                stmt.setLong(2, sensor.getId());
                stmt.setDouble(3, sensorData.getData());
                DBConnection.exec(stmt);
            }
            else { // unknown sensor
                logger.finest("Received report from unregistered sensor" +
                        "(" + sensorConfig.getClass().getSimpleName() + "): " + sensorConfig);
                sensor = findSensor(sensorConfig, detectedSensors);
                if(sensor == null) {
                    sensor = new Sensor();
                    detectedSensors.add(sensor);
                }
                sensor.setDeviceConfig(sensorConfig);
            }
            sensor.setDeviceData(sensorData);
            // call listeners
            for(HalDeviceReportListener<HalSensorConfig,HalSensorData> listener : sensor.getReportListeners())
                listener.reportReceived(sensorConfig, sensorData);

        } catch (SQLException e){
            logger.log(Level.WARNING, "Unable to store sensor report", e);
        }
    }

    private static Sensor findSensor(HalSensorConfig sensorData, List<Sensor> list){
        for (int i=0; i<list.size(); ++i) { // Don't use foreach for concurrency reasons
            Sensor s = list.get(i);
            if (sensorData.equals(s.getDeviceConfig())) {
                return s;
            }
        }
        return null;
    }

    @Override
    public void preConfigurationAction(Configurator configurator, Object obj) {
        if (obj instanceof HalSensorConfig) {
            Sensor sensor = findSensor((HalSensorConfig) obj, registeredSensors);
            if (sensor != null){
                deregister(sensor);
                limboSensors.add(sensor);
            }
        }
    }

    @Override
    public void postConfigurationAction(Configurator configurator, Object obj) {
        if (obj instanceof HalSensorConfig) {
            Sensor sensor = findSensor((HalSensorConfig) obj, limboSensors);
            if (sensor != null){
                register(sensor);
                limboSensors.remove(sensor);
            }
        }
    }


    @Override
    public void initialize(PluginManager pluginManager){
        super.initialize(pluginManager);
        instance = this;
    }

    public static SensorControllerManager getInstance(){
        return instance;
    }
}
