package se.hal.plugin.zigbee.deconz.zigbee.deconz.rest;

import zutil.net.ws.WSInterface.WSPath;
import zutil.net.ws.WSInterface.WSRequestType;

import static zutil.net.ws.WSInterface.RequestType.*;

/**
 * Scenes provide an easy and performant way to recall often used states to a group.
 *
 * @link https://dresden-elektronik.github.io/deconz-rest-doc/scenes/
 */
public interface DeConzRestScenes {

    /**
     * Creates a new scene for a group. The actual state of each light will become the lights scene state.
     *
     * @param name      The name of the new scene. (required)
     */
    @WSRequestType(HTTP_POST)
    @WSPath("/api/{{requestApiKey}}/groups/{{group_id}}/scenes")
    void createScene(String requestApiKey, int groupId, String name);

    /**
     * Returns a list of all scenes of a group.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}/groups/{{group_id}}/scenes")
    void getScenes(String requestApiKey, int groupId);

    /**
     * Returns all attributes of a scene.
     */
    @WSRequestType(HTTP_GET)
    @WSPath("/api/{{requestApiKey}}/groups/{{group_id}}/scenes/{{sceneId}}")
    void getScene(String requestApiKey, int groupId, int sceneId);

    /**
     * Sets attributes of a scene.
     *
     * @param name      Name of the scene. (optional)
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/groups/{{group_id}}/scenes/{{sceneId}}")
    void getScene(String requestApiKey, int groupId, int sceneId, String name);

    /**
     * Stores the current group state in the scene. The actual state of each light in the group will become the lights scene state.
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/groups/{{group_id}}/scenes/{{sceneId}}/store")
    String storeScene(String requestApiKey, int groupId, int sceneId);

    /**
     * Recalls a scene. The actual state of each light in the group will become the lights scene state stored in each light.
     * Note: Lights which are not reachable (turned off) won’t be affected!
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/groups/{{group_id}}/scenes/{{sceneId}}/recall")
    void recallScene(String requestApiKey, int groupId, int sceneId);

    /**
     * Recalls a scene. The actual state of each light in the group will become the lights scene state stored in each light.
     * Note: Lights which are not reachable (turned off) won’t be affected!
     *
     * @param on 	        	Set to true to turn the lights on, false to turn them off. 	optional
     * @param bri 	        	Set the brightness of the group. Depending on the lights 0 might not mean visible "off" but minimum brightness. If the lights are off and the value is greater 0 a on=true shall also be provided. 	optional
     * @param xy        	 	Set the CIE xy color space coordinates as array [x, y] of real values (0..1). 	optional
     * @param transitionTime    Transition time in 1/10 seconds between two states. (optional)
     */
    @WSRequestType(HTTP_PUT)
    @WSPath("/api/{{requestApiKey}}/groups/{{group_id}}/scenes/{{sceneId}}/state/lights/{{lightId}}/state")
    void setSceneState(String requestApiKey, int groupId, int sceneId, int lightId, int on, int bri, int xy, int transitionTime);

    /**
     * Deletes a scene.
     */
    @WSRequestType(HTTP_DELETE)
    @WSPath("/api/{{requestApiKey}}/groups/{{group_id}}/scenes/{{sceneId}}")
    void deleteScene(String requestApiKey, int groupId, int sceneId);
}
