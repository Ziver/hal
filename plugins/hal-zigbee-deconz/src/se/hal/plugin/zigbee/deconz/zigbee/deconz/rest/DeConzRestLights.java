package se.hal.plugin.zigbee.deconz.zigbee.deconz.rest;

import zutil.net.ws.WSInterface.WSPath;
import zutil.net.ws.WSInterface.WSRequestType;

import java.util.List;

import static zutil.net.ws.WSInterface.RequestType.*;

/**
 * Monitor and control single lights.
 *
 * @link https://dresden-elektronik.github.io/deconz-rest-doc/lights/
 */
public interface DeConzRestLights {

    /**
     * Returns a list of all lights.
     */
    @WSRequestType(GET)
    @WSPath("/api/{{requestApiKey}}/lights")
    void getLights(String requestApiKey);

    /**
     * Returns the full state of a light.
     */
    @WSRequestType(GET)
    @WSPath("/api/{{requestApiKey}}/lights/{{lightId}")
    void getLight(String requestApiKey, int lightId);

    /**
     * Returns the full state of a light.
     *
     * @param name  Set the name of the light. (required)
     */
    @WSRequestType(PUT)
    @WSPath("/api/{{requestApiKey}}/lights/{{lightId}")
    void setLight(String requestApiKey, int lightId, String name);

    /**
     * Sets the state of a light.
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
    @WSRequestType(GET)
    @WSPath("/api/{{requestApiKey}}/lights/{{lightId}/state")
    void setLightState(String requestApiKey, int lightId, boolean on, boolean toggle, int bri, int hue, int sat, int ct, List xy, String alert, String effect, int colorLoopSpeed, int transitionTime);

    /**
     * Removes the light from the gateway. It will not be shown in any rest api call. Also deletes all groups and scenes on the light device.
     *
     * @param reset     If true sends a network leave command to the light device (may not supported by each manufacturer). (optional)
     */
    @WSRequestType(PUT)
    @WSPath("/api/{{requestApiKey}}/lights/{{lightId}")
    void deleteLight(String requestApiKey, int lightId, boolean reset);

    /**
     * Remove the light from all groups it is a member of.
     */
    @WSRequestType(DELETE)
    @WSPath("/api/{{requestApiKey}}/lights/{{lightId}/groups")
    void deleteGroups(String requestApiKey, int lightId);

    /**
     * Remove the light from all scenes it is a member of.
     */
    @WSRequestType(DELETE)
    @WSPath("/api/{{requestApiKey}}/lights/{{lightId}/scenes")
    void deleteScenes(String requestApiKey, int lightId);
}
