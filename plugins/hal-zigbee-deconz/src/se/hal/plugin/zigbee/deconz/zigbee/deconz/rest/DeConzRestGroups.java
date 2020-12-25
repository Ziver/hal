package se.hal.plugin.zigbee.deconz.zigbee.deconz.rest;

import zutil.net.ws.WSInterface.WSPath;
import zutil.net.ws.WSInterface.WSRequestType;

import java.util.List;

import static zutil.net.ws.WSInterface.RequestType.*;

/**
 * Groups are useful to control many lights at once and provide the base to use scenes.
 *
 * @link https://dresden-elektronik.github.io/deconz-rest-doc/groups/
 */
public interface DeConzRestGroups {

    /**
     * Creates a new empty group.
     *
     * @param name      The name of the new group. (required)
     */
    @WSRequestType(HTTP_POST)
    @WSPath("/api/{{requestApiKey}}/groups")
    void createGroup(String requestApiKey, String name);

    /**
     * Returns a list of all groups.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}/groups")
    void getGroups(String requestApiKey);

    /**
     * Returns the full state of a group.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}/groups/{{groupId}}")
    void getGroup(String requestApiKey, int groupId);

    /**
     * Sets attributes of a group which are not related to its state.
     *
     * @param name 	            The name of the group 	optional
     * @param lights            IDs of the lights which are members of the group. 	optional
     * @param hidden            Indicates the hidden status of the group. Has no effect at the gateway but apps can uses this to hide groups. 	optional
     * @param lightSequence 	Specify a sorted list of light ids that can be used in apps. 	optional
     * @param multiDeviceIds 	Append the subsequential light ids of multidevices like the FLS-PP if the app should handle that light differently.
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/groups/{{groupId}}")
    void setGroup(String requestApiKey, int groupId, String name, List lights, boolean hidden, List lightSequence, List multiDeviceIds);

    /**
     * Sets attributes of a group which are not related to its state.
     *
     * @param on 	        	Set to true to turn the lights on, false to turn them off. 	optional
     * @param toggle 	     	Set to true toggles the lights of that group from on to off or vice versa, false has no effect. **Notice:** This setting supersedes the `on` parameter! 	optional
     * @param bri 	        	Set the brightness of the group. Depending on the lights 0 might not mean visible "off" but minimum brightness. If the lights are off and the value is greater 0 a on=true shall also be provided. 	optional
     * @param hue 	         	Set the color hue of the group. The hue parameter in the HSV color model is between 0°-360° and is mapped to 0..65535 to get 16-bit resolution. 	optional
     * @param sat 	         	Set the color saturation of the group. There 0 means no color at all and 255 is the highest saturation of the color. 	optional
     * @param ct 	         	Set the Mired color temperature of the group. (2000K - 6500K) 	optional
     * @param xy        	 	Set the CIE xy color space coordinates as array [x, y] of real values (0..1). 	optional
     * @param alert             Trigger a temporary alert effect: none (lights are not performing an alert), select (lights are blinking a short time), lselect (lights are blinking a longer time). (optional)
     * @param effect 	     	Trigger an effect of the group: none (no effect), colorloop (the lights of the group will cycle continously through all colors with the speed specified by colorloopspeed). (optional)
     * @param colorLoopSpeed    Specifies the speed of a colorloop. 1 = very fast, 255 = very slow (default: 15). This parameter only has an effect when it is called together with effect colorloop. (optional)
     * @param transitionTime    Transition time in 1/10 seconds between two states. (optional)
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/groups/{{groupId}}/action")
    void setGroupState(String requestApiKey, int groupId, boolean on, boolean toggle, int bri, int hue, int sat, int ct, List xy, String alert, String effect, int colorLoopSpeed, int transitionTime);

    /**
     * Deletes a group.
     */
    @WSRequestType(HTTP_DELETE)
    @WSPath("/api/{{requestApiKey}}/groups/{{groupId}}")
    void deleteGroup(String requestApiKey, int groupId);

}
