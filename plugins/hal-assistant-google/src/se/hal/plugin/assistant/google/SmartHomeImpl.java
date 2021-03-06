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
import se.hal.plugin.assistant.google.trait.DeviceTrait;
import se.hal.plugin.assistant.google.trait.DeviceTraitFactory;
import se.hal.plugin.assistant.google.type.DeviceType;
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
    private static final String AGENT_USER_ID = "Hal-" + (int)(Math.random()*10000);

    public SmartHomeImpl() { }


    @Override
    public void onTokenRegistration(String clientId, String token, long timeoutMillis) {
        try {
            GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(
                    token,
                    new Date(System.currentTimeMillis() + timeoutMillis)
            ));
            this.setCredentials(credentials);
            logger.fine("New OAuth2 token registered.");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Could not load google credentials", e);
        }
    }

    /**
     * https://developers.google.com/assistant/smarthome/reference/intent/sync
     *
     * TODO: https://developers.google.com/assistant/smarthome/traits/temperaturesetting
     */
    @Override
    public SyncResponse onSync(SyncRequest syncRequest, Map<?, ?> headers) {
        logger.fine("Received sync request.");

        SyncResponse res = new SyncResponse();
        res.setRequestId(syncRequest.requestId);
        res.setPayload(new SyncResponse.Payload());

        List<Sensor> sensors = Collections.EMPTY_LIST;

        try {
            DBConnection db = HalContext.getDB();
            sensors = Sensor.getLocalSensors(db);
        } catch (SQLException e) {
            logger.log(Level.WARNING, "Unable to retrieve sensor list.", e);
        }

        res.payload.agentUserId = AGENT_USER_ID;
        res.payload.devices = new SyncResponse.Payload.Device[sensors.size()];
        for (int i = 0; i < res.payload.devices.length; i++) {
            Sensor sensor = sensors.get(i);
            DeviceType type = DeviceType.getType(sensor);
            DeviceTrait[] traits = DeviceTraitFactory.getTraits(sensor);

            //  Generate payload

            SyncResponse.Payload.Device.Builder deviceBuilder =
                    new SyncResponse.Payload.Device.Builder()
                            .setId("Sensor-" + sensor.getId())
                            .setType(type.toString())
                            .setTraits(DeviceTraitFactory.getTraitIds(traits))
                            .setName(
                                    DeviceProto.DeviceNames.newBuilder()
                                            .setName(sensor.getName())
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
                for (Map.Entry<String,Object> entry : trait.generateSyncResponse(sensor.getDeviceConfig()).entrySet()) {
                    attribJson.put(entry.getKey(), entry.getValue());
                }
            }
            deviceBuilder.setAttributes(attribJson);

            /*if (device.contains("customData")) {
                Map<String, Object> customData = new HashMap<>();

                String customDataJson = new Gson().toJson(customData);
                deviceBuilder.setCustomData(customDataJson);
            }*/
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
        logger.fine("Received query request.");

        DBConnection db = HalContext.getDB();

        QueryResponse res = new QueryResponse();
        res.setRequestId(queryRequest.requestId);
        res.setPayload(new QueryResponse.Payload());
        Map<String, Map<String, Object>> deviceStates = new HashMap<>();

        for (SmartHomeRequest.RequestInputs input : queryRequest.getInputs()) {
            if (!"action.devices.QUERY".equals(input.intent))
                continue;

            for (QueryRequest.Inputs.Payload.Device device : ((QueryRequest.Inputs) input).payload.devices) {
                try {
                    if (!device.getId().startsWith("Sensor-"))
                        throw new IllegalArgumentException("Invalid device ID supplied: " + device.getId());

                    long sensorId = Long.parseLong(device.getId().substring(7)); // Get the number in the id "Sensor-<number>"
                    Sensor sensor = Sensor.getSensor(db, sensorId);
                    DeviceTrait[] traits = DeviceTraitFactory.getTraits(sensor);
                    Map<String, Object> deviceState = new HashMap<>();

                    logger.fine("Generating response for sensor: " + sensor.getName() + " (Id: " + sensor.getId() + ")");

                    for (DeviceTrait trait : traits) {
                        deviceState.putAll(trait.generateQueryResponse(sensor.getDeviceData()));
                    }

                    deviceState.put("status", "SUCCESS");
                    deviceStates.put(device.getId(), deviceState);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Query request failed for sensor: " + device.getId(), e);
                    Map<String, Object> failedDevice = new HashMap<>();
                    failedDevice.put("status", "ERROR");
                    failedDevice.put("errorCode", "deviceOffline");
                    deviceStates.put(device.id, failedDevice);
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
        logger.fine("Received execute request.");

        ExecuteResponse res = new ExecuteResponse();

        List<ExecuteResponse.Payload.Commands> commandsResponse = new ArrayList<>();
        List<String> successfulDevices = new ArrayList<>();
        Map<String, Object> states = new HashMap<>();

        ExecuteRequest.Inputs.Payload.Commands[] commands =
                ((ExecuteRequest.Inputs) executeRequest.inputs[0]).payload.commands;
/*        for (ExecuteRequest.Inputs.Payload.Commands command : commands) {
            for (ExecuteRequest.Inputs.Payload.Commands.Devices device : command.devices) {
                try {
                    states = database.execute(userId, device.id, command.execution[0]);
                    successfulDevices.add(device.id);
                    ReportState.makeRequest(this, userId, device.id, states);
                } catch (Exception e) {
                    if (e.getMessage().equals("PENDING")) {
                        ExecuteResponse.Payload.Commands pendingDevice = new ExecuteResponse.Payload.Commands();
                        pendingDevice.ids = new String[]{device.id};
                        pendingDevice.status = "PENDING";
                        commandsResponse.add(pendingDevice);
                        continue;
                    }
                    if (e.getMessage().equals("pinNeeded")) {
                        ExecuteResponse.Payload.Commands failedDevice = new ExecuteResponse.Payload.Commands();
                        failedDevice.ids = new String[]{device.id};
                        failedDevice.status = "ERROR";
                        failedDevice.setErrorCode("challengeNeeded");
                        failedDevice.setChallengeNeeded(
                                new HashMap<String, String>() {
                                    {
                                        put("type", "pinNeeded");
                                    }
                                });
                        failedDevice.setErrorCode(e.getMessage());
                        commandsResponse.add(failedDevice);
                        continue;
                    }
                    if (e.getMessage().equals("challengeFailedPinNeeded")) {
                        ExecuteResponse.Payload.Commands failedDevice = new ExecuteResponse.Payload.Commands();
                        failedDevice.ids = new String[]{device.id};
                        failedDevice.status = "ERROR";
                        failedDevice.setErrorCode("challengeNeeded");
                        failedDevice.setChallengeNeeded(
                                new HashMap<String, String>() {
                                    {
                                        put("type", "challengeFailedPinNeeded");
                                    }
                                });
                        failedDevice.setErrorCode(e.getMessage());
                        commandsResponse.add(failedDevice);
                        continue;
                    }
                    if (e.getMessage().equals("ackNeeded")) {
                        ExecuteResponse.Payload.Commands failedDevice = new ExecuteResponse.Payload.Commands();
                        failedDevice.ids = new String[]{device.id};
                        failedDevice.status = "ERROR";
                        failedDevice.setErrorCode("challengeNeeded");
                        failedDevice.setChallengeNeeded(
                                new HashMap<String, String>() {
                                    {
                                        put("type", "ackNeeded");
                                    }
                                });
                        failedDevice.setErrorCode(e.getMessage());
                        commandsResponse.add(failedDevice);
                        continue;
                    }

                    ExecuteResponse.Payload.Commands failedDevice = new ExecuteResponse.Payload.Commands();
                    failedDevice.ids = new String[]{device.id};
                    failedDevice.status = "ERROR";
                    failedDevice.setErrorCode(e.getMessage());
                    commandsResponse.add(failedDevice);
                }
            }
        }*/

        ExecuteResponse.Payload.Commands successfulCommands = new ExecuteResponse.Payload.Commands();
        successfulCommands.status = "SUCCESS";
        successfulCommands.setStates(states);
        successfulCommands.ids = successfulDevices.toArray(new String[]{});
        commandsResponse.add(successfulCommands);

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
