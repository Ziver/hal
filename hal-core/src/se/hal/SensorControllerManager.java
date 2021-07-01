package se.hal;

import se.hal.intf.*;
import se.hal.struct.Sensor;
import se.hal.util.HalDeviceUtil;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.plugin.PluginManager;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class manages all SensorController and EventController objects
 */
public class SensorControllerManager extends HalAbstractControllerManager<HalAbstractController, Sensor, HalSensorConfig>
        implements HalDeviceReportListener {

    private static final Logger logger = LogUtil.getLogger();
    private static SensorControllerManager instance;

    /** List of all registered sensors **/
    private List<Sensor> registeredSensors = Collections.synchronizedList(new ArrayList<>());
    /** List of auto detected sensors **/
    private List<Sensor> detectedSensors = Collections.synchronizedList(new ArrayList<>());


    @Override
    public void initialize(PluginManager pluginManager){
        super.initialize(pluginManager);
        instance = this;

        // Read in existing devices

        try {
            DBConnection db = HalContext.getDB();

            logger.info("Reading in existing sensors.");

            for (Sensor sensor : Sensor.getLocalSensors(db)) {
                SensorControllerManager.getInstance().register(sensor);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Unable to read in existing sensors.", e);
        }
    }

    // ----------------------------------------------------
    //                     SENSORS
    // ----------------------------------------------------

    /**
     * Register a Sensor instance on the manager.
     * The manager will start to save reported data for the registered Sensor.
     */
    @Override
    public void register(Sensor sensor) {
        if (sensor.getDeviceConfig() == null) {
            logger.warning("Sensor config is null: " + sensor);
            return;
        }
        if (!getAvailableDeviceConfigs().contains(sensor.getDeviceConfig().getClass())) {
            logger.warning("Sensor data plugin not available: " + sensor.getDeviceConfig().getClass());
            return;
        }

        logger.info("Registering new sensor(id: " + sensor.getId() + "): " + sensor.getDeviceConfig().getClass());
        Class<? extends HalAbstractController> c = sensor.getController();
        HalAbstractController controller = getControllerInstance(c);

        if (controller != null)
            controller.register(sensor.getDeviceConfig());
        registeredSensors.add(sensor);
        detectedSensors.remove(
                HalDeviceUtil.findDevice(sensor.getDeviceConfig(), detectedSensors)); // Remove if this device was detected
    }

    /**
     * Deregisters a Sensor from the manager.
     * Data reported on the Sensor will no longer be saved but already saved data will not be modified.
     * The Controller that owns the Sensor will be deallocated if it has no more registered devices.
     */
    @Override
    public void deregister(Sensor sensor){
        if (sensor.getDeviceConfig() == null) {
            logger.warning("Sensor config is null: " + sensor);
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
    public void reportReceived(HalDeviceConfig sensorConfig, HalDeviceData sensorData) {
        if (!(sensorConfig instanceof HalSensorConfig && sensorData instanceof HalSensorData))
            return;

        try{
            DBConnection db = HalContext.getDB();
            Sensor sensor = HalDeviceUtil.findDevice(sensorConfig, registeredSensors);

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
                sensor = HalDeviceUtil.findDevice(sensorConfig, detectedSensors);
                if (sensor == null) {
                    sensor = new Sensor();
                    detectedSensors.add(sensor);
                }
                sensor.setDeviceConfig((HalSensorConfig) sensorConfig);
            }
            sensor.setDeviceData((HalSensorData) sensorData);
            // call listeners
            for (HalDeviceReportListener listener : sensor.getReportListeners())
                listener.reportReceived(sensorConfig, sensorData);

        } catch (SQLException e){
            logger.log(Level.WARNING, "Unable to store sensor report", e);
        }
    }


    public static SensorControllerManager getInstance(){
        return instance;
    }
}
