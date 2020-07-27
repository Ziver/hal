package se.hal.plugin.zigbee.deconz.rest;

import zutil.net.ws.WSInterface.WSPath;
import zutil.net.ws.WSInterface.WSRequestType;
import zutil.parser.DataNode;

import static zutil.net.ws.WSInterface.RequestType.*;

/**
 * The configuration endpoint allows to retreive and modify the current configuration of the gateway.
 *
 * @link https://dresden-elektronik.github.io/deconz-rest-doc/configuration/
 */
public interface DeConzRestConfig {

    /**
     * Creates a new API key which provides authorized access to the REST API.
     *
     * @param deviceType    Name of the client application. (required)
     * @param username      Will be used as username. If not specified a random key will be generated. (optional)
     */
    @WSRequestType(HTTP_POST)
    @WSPath("/api")
    DataNode getAPIKey(String deviceType, String username);

    /**
     * Deletes an API key so it can no longer be used.
     */
    @WSRequestType(HTTP_DELETE)
    @WSPath("/api/{{requestApiKey}}/config/whitelist/{{apikey2}}")
    DataNode deleteAPIKey(String requestApiKey, String deleteApiKey);


    /**
     * Returns the current gateway configuration.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}/config")
    void getConfiguration(String requestApiKey);

    /**
     * Modify configuration parameters.
     */
    //@WSRequestType(HTTP_PUT)
    //@WSPath("/api/{{requestApiKey}}/config")
    //void setConfiguration(String requestApiKey);

    /**
     * Returns the full state of the gateway including all its lights, groups, scenes and schedules.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}")
    void getFullState(String requestApiKey);


    /**
     * Returns the newest software version available. Starts the update if available (only on raspberry pi).
     */
    @WSRequestType(HTTP_POST)
    @WSPath("/api/{{requestApiKey}}/config/update")
    void updateSoftware(String requestApiKey);

    /**
     * Starts the update firmware process if newer firmware is available.
     */
    @WSRequestType(HTTP_POST)
    @WSPath("/api/{{requestApiKey}}/config/updatefirmware")
    void updateFirmware(String requestApiKey);

    /**
     * Reset the gateway network settings to factory new and/or delete the deCONZ database (config, lights, scenes, groups, schedules, devices, rules).
     *
     * @param resetGW       Set the network settings of the gateway to factory new. (optional)
     * @param deleteDB      Delete the Database. (optional)
     */
    @WSRequestType(HTTP_POST)
    @WSPath("/api/{{requestApiKey}}/config/reset")
    void resetGateway(String requestApiKey, boolean resetGW, boolean deleteDB);


    /**
     * Change the Password of the Gateway. The parameter must be a Base64 encoded combination of “<username>:<password>”.
     *
     * @param username      The user name (currently only “delight” is supported). (required)
     * @param oldHash   	String 	The Base64 encoded combination of “username:old password”. (required)
     * @param newHash 	    String 	The Base64 encoded combination of “username:new password”. (required)
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/config/password")
    void setPassword(String requestApiKey, String username, String oldHash, String newHash);

    /**
     * Resets the username and password to default (“delight”,”delight”). Only possible within 10 minutes after gateway start.
     */
    @WSRequestType(HTTP_DELETE)
    @WSPath("/api/{{requestApiKey}}/config/password")
    void resetPassword(String requestApiKey);

}
