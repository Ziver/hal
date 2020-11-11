/*
 * Copyright 2020 Google LLC
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

import java.util.Map;
import java.util.logging.Logger;

import com.google.actions.api.smarthome.SmartHomeApp;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.home.graph.v1.HomeGraphApiServiceProto;
import com.google.home.graph.v1.HomeGraphApiServiceProto.ReportStateAndNotificationDevice;
import com.google.home.graph.v1.HomeGraphApiServiceProto.ReportStateAndNotificationRequest;
import com.google.home.graph.v1.HomeGraphApiServiceProto.StateAndNotificationPayload;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import zutil.log.LogUtil;
import zutil.parser.DataNode;
import zutil.parser.json.JSONParser;

/**
 * A singleton class to encapsulate state reporting behavior with changing ColorSetting state values.
 */
final class ReportState {
    private static final Logger logger = LogUtil.getLogger();


    private ReportState() {
    }


    /**
     * Creates and completes a ReportStateAndNotification request
     *
     * @param actionsApp The SmartHomeApp instance to use to make the gRPC request
     * @param userId     The agent user ID
     * @param deviceId   The device ID
     * @param states     A Map of state keys and their values for the provided device ID
     */
    public static void makeRequest(SmartHomeApp actionsApp, String userId, String deviceId, Map<String, Object> states) {
        Struct.Builder statesStruct = Struct.newBuilder();

        ReportStateAndNotificationDevice.Builder deviceBuilder =
                ReportStateAndNotificationDevice.newBuilder().setStates(
                        Struct.newBuilder().putFields(deviceId,
                                Value.newBuilder().setStructValue(statesStruct).build()
                        ));

        ReportStateAndNotificationRequest request =
                ReportStateAndNotificationRequest.newBuilder()
                        .setRequestId(String.valueOf(Math.random()))
                        .setAgentUserId(userId) // our single user's id
                        .setPayload(StateAndNotificationPayload.newBuilder().setDevices(deviceBuilder))
                        .build();

        actionsApp.reportState(request);
    }
}
