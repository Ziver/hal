package se.hal.plugin.zigbee.deconz.zigbee.deconz.rest;

import zutil.net.ws.WSInterface.WSPath;
import zutil.net.ws.WSInterface.WSRequestType;

import java.util.List;

import static zutil.net.ws.WSInterface.RequestType.*;

/**
 * Schedules provide the ability to trigger timed commands to groups or lights.
 *
 * @link https://dresden-elektronik.github.io/deconz-rest-doc/schedules/
 */
public interface DeConzRestSchedules {

    /**
     * Creates a new schedule.
     *
     * @param name          The name of the new schedule. If the name already exists a number will be appended. (optional)
     * @param description 	The description of the schedule. (optional)
     * @param command 	    The command to execute when the schedule triggers. (required)
     *                      command.address The address of a light or group resource. (required)
     *                      command.method  must be "PUT", (required)
     *                      command.body    The state that the light or group will activate when the schedule triggers, (required)
     * @param status 	    ("enabled"|"disabled") 	Whether the schedule is enabled or disabled. Default is enabled. (optional)
     * @param autoDelete 	If true the schedule will be deleted after triggered. Else it will be disabled. Default is true. (optional)
     * @param time 	        Time when the schedule shall trigger in UTC ISO 8601:2004 format. (required)
     */
    //@WSRequestType(HTTP_POST)
    //@WSPath("/api/{{requestApiKey}}/schedules")
    //int createSchedule(String requestApiKey, String name, String description, List command, String status, boolean autoDelete, String time);

    /**
     * Returns a list of all schedules.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}/schedules")
    List getSchedules(String requestApiKey);

    /**
     * Returns all attributes of a schedule.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}/schedules/{{scheduleId}}")
    List getSchedule(String requestApiKey, int scheduleId);

    /**
     * Creates a new schedule.
     *
     * @param name          The name of the new schedule. If the name already exists a number will be appended. (optional)
     * @param description 	The description of the schedule. (optional)
     * @param command 	    The command to execute when the schedule triggers. (required)
     *                      command.address The address of a light or group resource. (required)
     *                      command.method  must be "PUT", (required)
     *                      command.body    The state that the light or group will activate when the schedule triggers, (required)
     * @param status 	    ("enabled"|"disabled") 	Whether the schedule is enabled or disabled. Default is enabled. (optional)
     * @param autoDelete 	If true the schedule will be deleted after triggered. Else it will be disabled. Default is true. (optional)
     * @param time 	        Time when the schedule shall trigger in UTC ISO 8601:2004 format. (required)
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/schedules/{{scheduleId}}")
    void setSchedule(String requestApiKey, int scheduleId, String name, String description, List command, String status, boolean autoDelete, String time);

    /**
     * Returns all attributes of a schedule.
     */
    @WSRequestType(HTTP_DELETE)
    @WSPath("/api/{{requestApiKey}}/schedules/{{scheduleId}}")
    void deleteSchedule(String requestApiKey, int scheduleId);
}
