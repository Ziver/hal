package se.hal.plugin.zigbee.deconz.zigbee.deconz.rest;

import zutil.net.ws.WSInterface.WSPath;
import zutil.net.ws.WSInterface.WSRequestType;

import static zutil.net.ws.WSInterface.RequestType.*;

/**
 * Rules provide the ability to trigger actions of lights or groups when a specific sensor condition is met.
 *
 * @link https://dresden-elektronik.github.io/deconz-rest-doc/rules/
 */
public interface DeConzRestRules {

    /**
     * Creates a new rule.
     * <p>Note: To create ZigBee bindings between a sensor and a light or group use the BIND method. The rules condition specifies which ZigBee cluster will be used.
     *
     * @param name          The name of the rule. 	required
     * @param periodic      Specifies if the rule should trigger periodically. 0 = trigger on event; >0 = time in ms the rule will be triggered periodically. Default is 0. 	optional
     * @param status        String ("enabled"|"disabled")
     * @param actions       An array of actions that will happen when the rule triggers. 	required
     *                      action.address 	path to a light, group or scene resource 	required
     *                      action.body 	Parameters that will be send to the resource formated as JSON. 	required
     *                      action.method 	String 	Can be PUT, POST, DELETE (currently only used for green power devices) or BIND which will create a ZigBee binding between a sensor and a light or group. 	required
     * @param conditions 	Array(condition) (1..8) 	The conditions that must be met to trigger a rule. 	required
     *                      condition.address 	String 	path to a sensor resource and the related state 	required
     *                      condition.operator 	String 	eq, gt, lt, dx (equals, greater than, lower than, on change). 	required
     *                      condition.value 	String 	The value the operator is compared with. Will be casted automatically to the corresponding data type. 	required
     */
    //@WSRequestType(HTTP_POST)
    //@WSPath("/api/{{requestApiKey}}/rules")
    //void createRule(String requestApiKey, String name, int periodic, String status, List actions, List conditions);

    /**
     * Returns a list of all rules. If there are no rules in the system then an empty object {} will be returned.
     */
    @WSRequestType(GET)
    @WSPath("/api/{{requestApiKey}}/rules")
    void getRules(String requestApiKey);

    /**
     * Returns the rule with the specified id.
     */
    @WSRequestType(GET)
    @WSPath("/api/{{requestApiKey}}/rules/{{ruleId}}")
    void getRule(String requestApiKey, int ruleId);

    /**
     * Update a rule with the specified parameters.
     *
     * @param name          The name of the rule. 	required
     * @param periodic      Specifies if the rule should trigger periodically. 0 = trigger on event; >0 = time in ms the rule will be triggered periodically. Default is 0. 	optional
     * @param status        String ("enabled"|"disabled")
     * @param actions       An array of actions that will happen when the rule triggers. 	required
     *                      action.address 	path to a light, group or scene resource 	required
     *                      action.body 	Parameters that will be send to the resource formated as JSON. 	required
     *                      action.method 	String 	Can be PUT, POST, DELETE (currently only used for green power devices) or BIND which will create a ZigBee binding between a sensor and a light or group. 	required
     * @param conditions 	Array(condition) (1..8) 	The conditions that must be met to trigger a rule. 	required
     *                      condition.address 	String 	path to a sensor resource and the related state 	required
     *                      condition.operator 	String 	eq, gt, lt, dx (equals, greater than, lower than, on change). 	required
     *                      condition.value 	String 	The value the operator is compared with. Will be casted automatically to the corresponding data type. 	required
     */
    //@WSRequestType(HTTP_PUT)
    //@WSPath("/api/{{requestApiKey}}/rules/{{ruleId}}")
    //void setRule(String requestApiKey, int ruleId, String name, int periodic, String status, List actions, List conditions);

    @WSRequestType(DELETE)
    @WSPath("/api/{{requestApiKey}}/rules/{{ruleId}}")
    void deleteRule(String requestApiKey, int ruleId);
}
