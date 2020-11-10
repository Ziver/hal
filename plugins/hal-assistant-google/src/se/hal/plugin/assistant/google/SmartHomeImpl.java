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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import com.google.actions.api.smarthome.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.gson.Gson;
import com.google.home.graph.v1.DeviceProto;
import com.google.protobuf.Struct;
import com.google.protobuf.util.JsonFormat;
import se.hal.HalContext;
import se.hal.plugin.assistant.google.endpoint.AuthServlet;
import se.hal.plugin.assistant.google.endpoint.AuthTokenServlet;
import se.hal.plugin.assistant.google.endpoint.SmartHomeServlet;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpServer;


public class SmartHomeImpl extends SmartHomeApp {
    private static final Logger logger = LogUtil.getLogger();
    private static final String PARAM_PORT = "assistant.google.port";
    private static final String PARAM_KEYSTORE_PATH = "assistant.google.keystore";
    private static final String PARAM_KEYSTORE_PASSWORD = "assistant.google.keystore_psw";

    private HttpServer httpServer;


    public SmartHomeImpl() {
        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(getClass().getResourceAsStream("assistant_google.conf"));
            this.setCredentials(credentials);
        } catch (Exception e) {
            logger.severe("couldn't load credentials");
            throw new RuntimeException(e);
        }

        httpServer = new HttpServer(
                HalContext.getIntegerProperty(PARAM_PORT),
                FileUtil.find(HalContext.getStringProperty(PARAM_KEYSTORE_PATH)),
                HalContext.getStringProperty(PARAM_KEYSTORE_PASSWORD));
        httpServer.setPage(AuthServlet.ENDPOINT_URL, new AuthServlet(this));
        httpServer.setPage(AuthTokenServlet.ENDPOINT_URL, new AuthTokenServlet(this));
        httpServer.setPage(SmartHomeServlet.ENDPOINT_URL, new SmartHomeServlet(this));
    }


    @Override
    public SyncResponse onSync(SyncRequest syncRequest, Map<?, ?> headers) {
        SyncResponse res = new SyncResponse();
        res.setRequestId(syncRequest.requestId);
        res.setPayload(new SyncResponse.Payload());

        int numOfDevices = 0;
        res.payload.devices = new SyncResponse.Payload.Device[numOfDevices];
/*        for (int i = 0; i < numOfDevices; i++) {
            SyncResponse.Payload.Device.Builder deviceBuilder =
                    new SyncResponse.Payload.Device.Builder()
                            .setId(device.getId())
                            .setType((String) device.get("type"))
                            .setTraits((List<String>) device.get("traits"))
                            .setName(
                                    DeviceProto.DeviceNames.newBuilder()
                                            .addAllDefaultNames((List<String>) device.get("defaultNames"))
                                            .setName((String) device.get("name"))
                                            .addAllNicknames((List<String>) device.get("nicknames"))
                                            .build())
                            .setWillReportState((Boolean) device.get("willReportState"))
                            .setRoomHint((String) device.get("roomHint"))
                            .setDeviceInfo(
                                    DeviceProto.DeviceInfo.newBuilder()
                                            .setManufacturer((String) device.get("manufacturer"))
                                            .setModel((String) device.get("model"))
                                            .setHwVersion((String) device.get("hwVersion"))
                                            .setSwVersion((String) device.get("swVersion"))
                                            .build());
            if (device.contains("attributes")) {
                Map<String, Object> attributes = new HashMap<>();
                attributes.putAll((Map<String, Object>) device.get("attributes"));
                String attributesJson = new Gson().toJson(attributes);
                Struct.Builder attributeBuilder = Struct.newBuilder();
                try {
                    JsonFormat.parser().ignoringUnknownFields().merge(attributesJson, attributeBuilder);
                } catch (Exception e) {
                    logger.error("FAILED TO BUILD");
                }
                deviceBuilder.setAttributes(attributeBuilder.build());
            }
            if (device.contains("customData")) {
                Map<String, Object> customData = new HashMap<>();
                customData.putAll((Map<String, Object>) device.get("customData"));

                String customDataJson = new Gson().toJson(customData);
                deviceBuilder.setCustomData(customDataJson);
            }
            if (device.contains("otherDeviceIds")) {
                deviceBuilder.setOtherDeviceIds((List) device.get("otherDeviceIds"));
            }
            res.payload.devices[i] = deviceBuilder.build();
        }
*/
        return res;
    }

    @Override
    public QueryResponse onQuery(QueryRequest queryRequest, Map<?, ?> headers) {
        QueryRequest.Inputs.Payload.Device[] devices = ((QueryRequest.Inputs) queryRequest.getInputs()[0]).payload.devices;
        QueryResponse res = new QueryResponse();
        res.setRequestId(queryRequest.requestId);
        res.setPayload(new QueryResponse.Payload());
/*
        Map<String, Map<String, Object>> deviceStates = new HashMap<>();
        for (QueryRequest.Inputs.Payload.Device device : devices) {
            try {
                Map<String, Object> deviceState = database.getState(userId, device.id);
                deviceState.put("status", "SUCCESS");
                deviceStates.put(device.id, deviceState);
            } catch (Exception e) {
                logger.error("QUERY FAILED: {}", e);
                Map<String, Object> failedDevice = new HashMap<>();
                failedDevice.put("status", "ERROR");
                failedDevice.put("errorCode", "deviceOffline");
                deviceStates.put(device.id, failedDevice);
            }
        }
        res.payload.setDevices(deviceStates);*/
        return res;
    }

    @Override
    public ExecuteResponse onExecute(ExecuteRequest executeRequest, Map<?, ?> headers) {
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

    }
}
