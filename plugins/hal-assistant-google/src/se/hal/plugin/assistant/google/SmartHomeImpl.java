/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package se.hal.plugin.assistant.google;

import com.google.actions.api.smarthome.*;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.home.graph.v1.DeviceProto;
import com.google.home.graph.v1.HomeGraphApiServiceProto;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import org.json.JSONObject;
import se.hal.HalContext;
import se.hal.intf.HalAbstractDevice;
import se.hal.plugin.assistant.google.trait.DeviceTrait;
import se.hal.plugin.assistant.google.trait.DeviceTraitFactory;
import se.hal.plugin.assistant.google.trait.OnOffTrait;
import se.hal.plugin.assistant.google.type.DeviceType;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.net.http.page.oauth.OAuth2Registry.TokenRegistrationListener;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


public class SmartHomeImpl extends SmartHomeApp implements TokenRegistrationListener {
    private static final Logger logger = LogUtil.getLogger();

    public static final String CONFIG_USER_AGENT = "hal_assistant_google.user_agent";
    public static final String CONFIG_TOKEN = "hal_assistant_google.token";
    public static final String CONFIG_TOKEN_TIMEOUT = "hal_assistant_google.token_timeout";

    private final String userAgent;


    public SmartHomeImpl() {
        if (!HalContext.containsProperty(CONFIG_USER_AGENT))
            HalContext.setProperty(CONFIG_USER_AGENT, "Hal-" + (int) (Math.random() * 10000));
        userAgent = HalContext.getStringProperty(CONFIG_USER_AGENT);

        if (HalContext.containsProperty(CONFIG_TOKEN)) {
            // Restore previous token
            onTokenRegistration(
                    null,
                    HalContext.getStringProperty(CONFIG_TOKEN),
                    HalContext.getLongProperty(CONFIG_TOKEN_TIMEOUT));
        }
    }


    @Override
    public void onTokenRegistration(String clientId, String token, long timeoutMillis) {
        try {
            GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(
                    token,
                    new Date(System.currentTimeMillis() + timeoutMillis)
            ));
            this.setCredentials(credentials);
            logger.fine("New OAuth2 token registered.");

            HalContext.setProperty(CONFIG_TOKEN, token);
            HalContext.setProperty(CONFIG_TOKEN_TIMEOUT, String.valueOf(timeoutMillis));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load google credentials", e);
        }
    }

    /**
     * https://developers.google.com/assistant/smarthome/reference/intent/sync
     */
    @SuppressWarnings("unchecked")
    @Override
    public SyncResponse onSync(SyncRequest syncRequest, Map<?, ?> headers) {
        logger.fine("Received sync request.");

        SyncResponse res = new SyncResponse();
        res.setRequestId(syncRequest.requestId);
        res.setPayload(new SyncResponse.Payload());

        List<HalAbstractDevice> deviceList = new LinkedList<>();

        try {
            DBConnection db = HalContext.getDB();
            deviceList.addAll(Sensor.getLocalSensors(db));
            deviceList.addAll(Event.getLocalEvents(db));
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Unable to retrieve devices.", e);
        }

        res.payload.agentUserId = userAgent;
        res.payload.devices = new SyncResponse.Payload.Device[deviceList.size()];
        for (int i = 0; i < res.payload.devices.length; i++) {
            HalAbstractDevice device = deviceList.get(i);
            DeviceType type = DeviceType.getType(device);
            DeviceTrait[] traits = DeviceTraitFactory.getTraits(device);

            //  Generate payload

            SyncResponse.Payload.Device.Builder deviceBuilder =
                    new SyncResponse.Payload.Device.Builder()
                            .setId(device.getClass().getSimpleName() + "-" + device.getId())
                            .setType("" + type)
                            .setTraits(DeviceTraitFactory.getTraitIds(traits))
                            .setName(
                                    DeviceProto.DeviceNames.newBuilder()
                                            .setName(device.getName())
                                            .build())
                            .setWillReportState(true)
                            //.setRoomHint(sensor.getRoom().getName())
                            .setDeviceInfo(
                                    DeviceProto.DeviceInfo.newBuilder()
                                            //.setManufacturer((String) device.get("manufacturer"))
                                            //.setModel((String) device.get("model"))
                                            //.setHwVersion((String) device.get("hwVersion"))
                                            //.setSwVersion((String) device.get("swVersion"))
                                            .build());

            JSONObject attribJson = new JSONObject();
            for (DeviceTrait trait : traits) {
                for (Map.Entry<String,Object> entry : trait.generateSyncResponse(device.getDeviceConfig()).entrySet()) {
                    attribJson.put(entry.getKey(), entry.getValue());
                }
            }
            deviceBuilder.setAttributes(attribJson);

            // Set custom data

            JSONObject customDataJson = new JSONObject();
            customDataJson.put("type", device.getClass().getSimpleName());
            customDataJson.put("id", device.getId());
            deviceBuilder.setCustomData(customDataJson);

            res.payload.devices[i] = deviceBuilder.build();
        }

        return res;
    }

    /**
     * Creates a and sends a request for a sync from Google
     *
     * @param userId     The agent user ID
     * @param deviceId   The device ID
     * @param states     A Map of state keys and their values for the provided device ID
     */
    public void syncRequest(String userId, String deviceId, Map<String, Object> states) {
        Struct.Builder statesStruct = Struct.newBuilder();

        HomeGraphApiServiceProto.ReportStateAndNotificationDevice.Builder deviceBuilder =
                HomeGraphApiServiceProto.ReportStateAndNotificationDevice.newBuilder().setStates(
                        Struct.newBuilder().putFields(deviceId,
                                Value.newBuilder().setStructValue(statesStruct).build()
                        ));

        HomeGraphApiServiceProto.ReportStateAndNotificationRequest request =
                HomeGraphApiServiceProto.ReportStateAndNotificationRequest.newBuilder()
                        .setRequestId(String.valueOf(Math.random()))
                        .setAgentUserId(userId) // our single user's id
                        .setPayload(HomeGraphApiServiceProto.StateAndNotificationPayload.newBuilder().setDevices(deviceBuilder))
                        .build();

        this.reportState(request);
    }

    /**
     * https://developers.google.com/assistant/smarthome/reference/intent/query
     */
    @Override
    public QueryResponse onQuery(QueryRequest queryRequest, Map<?, ?> headers) {
        DBConnection db = HalContext.getDB();

        QueryResponse res = new QueryResponse();
        res.setRequestId(queryRequest.requestId);
        res.setPayload(new QueryResponse.Payload());
        Map<String, Map<String, Object>> deviceStates = new HashMap<>();

        for (SmartHomeRequest.RequestInputs input : queryRequest.getInputs()) {
            if (!"action.devices.QUERY".equals(input.intent))
                continue;

            for (QueryRequest.Inputs.Payload.Device deviceRequest : ((QueryRequest.Inputs) input).payload.devices) {
                try {
                    logger.fine("Received query request for: type=" + deviceRequest.getId());

                    if (deviceRequest.getCustomData() == null || !deviceRequest.getCustomData().containsKey("type") || !deviceRequest.getCustomData().containsKey("id"))
                        throw new IllegalArgumentException("Device Type and ID was no supplied in customData: " + deviceRequest.getId());

                    String deviceTypeStr = (String) deviceRequest.getCustomData().get("type");
                    int deviceId = (Integer) deviceRequest.getCustomData().get("id");

                    HalAbstractDevice device;
                    switch (deviceTypeStr) {
                        case "Sensor": device = Sensor.getSensor(db, deviceId); break;
                        case "Event":  device = Event.getEvent(db, deviceId); break;
                        default: throw new IllegalArgumentException("Unknown device type: " + deviceTypeStr);
                    }

                    logger.fine("Generating response for sensor: " + device.getName() + " (Id: " + device.getId() + ")");

                    DeviceTrait[] traits = DeviceTraitFactory.getTraits(device);
                    Map<String, Object> deviceState = new HashMap<>();

                    if (traits.length > 0) {
                        for (DeviceTrait trait : traits) {
                            deviceState.putAll(trait.generateQueryResponse(device.getDeviceData()));
                        }

                        deviceState.put("status", "SUCCESS");
                    } else {
                        deviceState.put("status", "UNKNOWN");
                    }
                    deviceStates.put(deviceRequest.getId(), deviceState);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Query request failed for sensor: " + deviceRequest.getId(), e);
                    Map<String, Object> failedDevice = new HashMap<>();
                    failedDevice.put("status", "ERROR");
                    failedDevice.put("errorCode", "deviceOffline");
                    deviceStates.put(deviceRequest.id, failedDevice);
                }
            }
        }

        res.payload.setDevices(deviceStates);
        return res;
    }

    /**
     * TODO:
     */
    @Override
    public ExecuteResponse onExecute(ExecuteRequest executeRequest, Map<?, ?> headers) {
        DBConnection db = HalContext.getDB();

        ExecuteResponse res = new ExecuteResponse();
        List<ExecuteResponse.Payload.Commands> commandsResponse = new ArrayList<>();

        for (ExecuteRequest.Inputs.Payload.Commands command : ((ExecuteRequest.Inputs) executeRequest.inputs[0]).payload.commands) {
            for (ExecuteRequest.Inputs.Payload.Commands.Devices deviceRequest : command.devices) {
                try {
                    if (deviceRequest.getCustomData() == null || !deviceRequest.getCustomData().containsKey("type") || !deviceRequest.getCustomData().containsKey("id"))
                        throw new IllegalArgumentException("Device Type and ID was no supplied in customData: " + deviceRequest.getId());

                    String deviceTypeStr = (String) deviceRequest.getCustomData().get("type");
                    int deviceId = (Integer) deviceRequest.getCustomData().get("id");

                    HalAbstractDevice device;
                    switch (deviceTypeStr) {
                        case "Sensor": device = Sensor.getSensor(db, deviceId); break;
                        case "Event":  device = Event.getEvent(db, deviceId); break;
                        default: throw new IllegalArgumentException("Unknown device type: " + deviceTypeStr);
                    }

                    for (ExecuteRequest.Inputs.Payload.Commands.Execution execution : command.execution) {
                        if ("action.devices.commands.OnOff".equals(execution.command)) { // TODO: This looks ugly!
                            new OnOffTrait().execute(device, execution);
                        } else
                            throw new UnsupportedOperationException("Unsupported command requested: " + execution.command);
                    }

                    ExecuteResponse.Payload.Commands successfulCommands = new ExecuteResponse.Payload.Commands();
                    successfulCommands.status = "SUCCESS";
                    successfulCommands.ids = new String[]{deviceRequest.id};
                    commandsResponse.add(successfulCommands);
                } catch (Exception e) {
                    ExecuteResponse.Payload.Commands failedDevice = new ExecuteResponse.Payload.Commands();
                    failedDevice.ids = new String[]{deviceRequest.id};
                    failedDevice.status = "ERROR";
                    failedDevice.setErrorCode(e.getMessage());
                    commandsResponse.add(failedDevice);
                }
            }
        }

        res.requestId = executeRequest.requestId;
        ExecuteResponse.Payload payload = new ExecuteResponse.Payload(
                        commandsResponse.toArray(new ExecuteResponse.Payload.Commands[]{}));
        res.setPayload(payload);

        return res;
    }

    @Override
    public void onDisconnect(DisconnectRequest disconnectRequest, Map<?, ?> headers) {
        logger.fine("Received disconnect request.");
    }
}
