package se.hal.plugin.zigbee.deconz.rest;

import zutil.net.ws.WSInterface.WSPath;
import zutil.net.ws.WSInterface.WSRequestType;

import java.util.List;
import java.util.Map;

import static zutil.net.ws.WSInterface.RequestType.*;

/**
 * Sensors can be used to measure environment parameters like brightness or activation of a switch. With a corresponding rule they can control lights and groups.
 *
 * @link https://dresden-elektronik.github.io/deconz-rest-doc/sensors/
 */
public interface DeConzRestSensors {

    /**
     * Creates a new sensor.
     *
     * @param name              The name of the sensor. 	required
     * @param modelid           The model identifier of the sensor. 	required
     * @param swversion         The software version of the sensor. 	required
     * @param type              The type of the sensor (see: allowed sensor types and its states). 	required
     * @param uniqueid          The unique id of the sensor. Should be the MAC address of the device. 	required
     * @param manufacturername  The manufacturer name of the sensor. 	required
     * @param state             The state of the sensor (see: supported sensor types and its states). 	optional
     * @param config            The config of the sensor. (optional)
     *                          on - Bool - default: true
     *                          reachable - Bool - default: true
     *                          battery - Number (0..100)
     */
    @WSRequestType(HTTP_POST)
    @WSPath("/api/{{requestApiKey}}/sensors")
    void createSensor(String requestApiKey, int groupId, String name, String modelid, String swversion, String type, String uniqueid, String manufacturername, Map state, Map config);

    /**
     * Returns a list of all Sensors. If there are no sensors in the system then an empty object {} will be returned.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}/sensors")
    List getSensor(String requestApiKey);

    /**
     * Returns a list of all Sensors. If there are no sensors in the system then an empty object {} will be returned.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}/sensors/{{sensorId}}")
    void getSensor(String requestApiKey, int sensorId);

    /**
     * Update a sensor with the specified parameters.
     *
     * @param name 	The name of the sensor. (optional)
     * @param mode 	Only available for dresden elektronik Lighting Switch. Set the mode of the switch. (optional)
     *              1 = Scenes mode
     *              2 = Two groups mode
     *              3 = Color temperature mode
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/sensors/{{sensorId}}")
    void setSensor(String requestApiKey, int sensorId, String name, int mode);

    /**
     * Update a sensor with the specified parameters.
     *
     * @param on            The on/off status of the sensor. (optional)
     * @param reachable     The reachable status of the sensor. (optional)
     * @param battery       The current battery state in percent, only for battery powered devices. (optional)
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/sensors/{{sensorId}}/config")
    void setSensorConfig(String requestApiKey, int sensorId, boolean on, boolean reachable, int battery);

    /**
     * Update a sensor state with the specified parameters.
     *
     * @param flag  Sensor type	      | Allowed state | type
     *              CLIPSwitch 	        buttonevent 	Number
     *              CLIPOpenClose 	    open            Bool
     *              CLIPPresence 	    presence        Bool
     *              CLIPTemperature 	temperature     Number
     *              CLIPGenericFlag 	flag            Bool
     *              CLIPGenericStatus 	status          Number
     *              CLIPHumidity 	    humidity        Number
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/sensors/{{sensorId}}/state")
    void setSensorState(String requestApiKey, String flag);

    /**
     * Delete a sensor.
     */
    @WSRequestType(HTTP_DELETE)
    @WSPath("/api/{{requestApiKey}}/sensors/{{sensorId}}/state")
    void deleteSensor(String requestApiKey, int sensorId);
}
