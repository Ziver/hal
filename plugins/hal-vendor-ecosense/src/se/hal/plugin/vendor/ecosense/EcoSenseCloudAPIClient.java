/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.hal.plugin.vendor.ecosense;

import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthFlowType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InitiateAuthResponse;
import zutil.ObjectUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpURL;
import zutil.net.ws.WSInterface;
import zutil.net.ws.rest.RESTClient;
import zutil.parser.DataNode;
import zutil.parser.json.JSONWriter;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * API Client class that connects to EcoSense cloud API.
 *
 * Reference: https://github.com/rwestergren/hass-ecosense-radon
 */
public class EcoSenseCloudAPIClient {
    private static final Logger logger = LogUtil.getLogger();

    private static final String USER_POOL_ID = "us-west-2_vB73oNa7f";
    private static final String CLIENT_ID = "1dk9ul54cdo42lt6e9u1oa9g1d";
    private static final String USER_POOL_REGION = "us-west-2";
    private static final String API_URL = "https://api.cloud.ecosense.io/api";

    private static SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"); //2025-10-22T14:09:29.691533

    private String username;
    private String password;
    /** The authenticated Amazon Cognito ID token. */
    private String idToken;

    public EcoSenseCloudAPIClient(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void authenticate() {
        CognitoIdentityProviderClient cognitoClient = CognitoIdentityProviderClient.builder()
                .region(Region.of(USER_POOL_REGION))
                .credentialsProvider(AnonymousCredentialsProvider.create())
                .build();

        Map<String, String> authParameters = new HashMap<>();
        authParameters.put("USERNAME", username);
        authParameters.put("PASSWORD", password);

        InitiateAuthRequest authRequest = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .authParameters(authParameters)
                .clientId(CLIENT_ID)
                .build();

        InitiateAuthResponse authResult = cognitoClient.initiateAuth(authRequest);
        this.idToken = authResult.authenticationResult().idToken();
        cognitoClient.close();
    }

    public EcoSenseDevice getDevices() {
        try {
            if (ObjectUtil.isEmpty(idToken)) {
                authenticate();
            }

            RESTClient client = new RESTClient(WSInterface.RequestType.GET, new HttpURL(API_URL + "/v1/device"));
            client.setHeader("Authorization", "Bearer " + idToken);
            client.setParameter("email", username);
            DataNode rootNode = client.send();

            for (DataNode deviceNode : rootNode) {
                EcoSenseDevice ecoSenseDevice = new EcoSenseDevice();
                ecoSenseDevice.pollingPeriod = deviceNode.getInt("polling_period");
                ecoSenseDevice.ledStatus = deviceNode.getString("led_status");
                ecoSenseDevice.subscription = deviceNode.getString("subscription");
                ecoSenseDevice.radonLevel = deviceNode.getInt("radon_level");
                ecoSenseDevice.deactivated = deviceNode.getString("deactivated");
                ecoSenseDevice.ledValue = deviceNode.getInt("led_value");
                ecoSenseDevice.deviceName = deviceNode.getString("device_name");
                ecoSenseDevice.geoCdnt = deviceNode.getString("geo_cdnt");
                ecoSenseDevice.alarmStatus = deviceNode.getString("alarm_status");
                ecoSenseDevice.geoHash = deviceNode.getString("geohash");
                ecoSenseDevice.subsStatus = deviceNode.getString("subs_status");
                ecoSenseDevice.email = deviceNode.getString("email");
                ecoSenseDevice.externalIp = deviceNode.getString("external_ip");
                ecoSenseDevice.dStatus = deviceNode.getInt("d_status");
                ecoSenseDevice.dType = deviceNode.getInt("d_type");
                ecoSenseDevice.radonDou = deviceNode.getInt("radon_dou");
                ecoSenseDevice.alarmValue = deviceNode.getInt("alarm_value");
                ecoSenseDevice.mvMode = deviceNode.getInt("mv_mode");
                ecoSenseDevice.deviceLocationZipcode = deviceNode.get("device_location").getString("zipcode");
                ecoSenseDevice.deviceLocationCountry = deviceNode.get("device_location").getString("country");
                ecoSenseDevice.deviceLocationCity = deviceNode.get("device_location").getString("city");
                ecoSenseDevice.deviceLocationStreet = deviceNode.get("device_location").getString("street");
                ecoSenseDevice.deviceLocationCountryCode = deviceNode.get("device_location").getString("countryCode");
                ecoSenseDevice.deviceLocationCounty = deviceNode.get("device_location").getString("county");
                ecoSenseDevice.deviceLocationDetail = deviceNode.get("device_location").getString("detail");
                ecoSenseDevice.deviceLocationState = deviceNode.get("device_location").getString("state");
                ecoSenseDevice.serialNumber = deviceNode.getString("serial_number");
                ecoSenseDevice.timeZone = deviceNode.getString("time_zone");
                ecoSenseDevice.devicePlacementLocationFloor = deviceNode.get("device_placement").getString("location_floor");
                ecoSenseDevice.devicePlacementLocationMitigationInstalled = deviceNode.get("device_placement").getString("mitigation_installed");
                ecoSenseDevice.devicePlacementLocationLocationType = deviceNode.get("device_placement").getString("location_type");
                ecoSenseDevice.devicePlacementLocationRoomType = deviceNode.get("device_placement").getString("room_type");
                ecoSenseDevice.pushNotification = deviceNode.getString("push_notification");
                ecoSenseDevice.unit = deviceNode.getInt("unit");
                ecoSenseDevice.registrationDate = deviceNode.getString("registration_date");
                ecoSenseDevice.pTime = deviceNode.getInt("p_time");
                ecoSenseDevice.cFactor = deviceNode.getDouble("c_factor");
                ecoSenseDevice.guest = deviceNode.getInt("guest");
                ecoSenseDevice.fwVersion = deviceNode.getString("fw_version");
                ecoSenseDevice.wifiName = deviceNode.getString("wifi_name");

                try {
                    ecoSenseDevice.lastRadonUpdateTime = DATE_PARSER.parse(deviceNode.getString("last_radon_update_time")).getTime();
                    ecoSenseDevice.lastUpdateTime = DATE_PARSER.parse(deviceNode.getString("last_update_time")).getTime();
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Was unable to parse timestamp from EcoSense API.", e);
                }

                return ecoSenseDevice;
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            idToken = null;
            throw new RuntimeException(e);
        }

        return null;
    }

    public class EcoSenseDevice {
        public int pollingPeriod;
        public String ledStatus;
        public String subscription;
        public int radonLevel;
        public String deactivated;
        public int ledValue;
        public String deviceName;
        public String geoCdnt;
        public String alarmStatus;
        public String geoHash;
        public String subsStatus;
        public String email;
        public String externalIp;
        public int dStatus;
        public int dType;
        public int radonDou;
        public int alarmValue;
        public int mvMode;
        public String deviceLocationZipcode;
        public String deviceLocationCountry;
        public String deviceLocationCity;
        public String deviceLocationStreet;
        public String deviceLocationCountryCode;
        public String deviceLocationCounty;
        public String deviceLocationDetail;
        public String deviceLocationState;
        public String serialNumber;
        public String timeZone;
        public String devicePlacementLocationFloor;
        public String devicePlacementLocationMitigationInstalled;
        public String devicePlacementLocationLocationType;
        public String devicePlacementLocationRoomType;
        public String pushNotification;
        public long lastRadonUpdateTime;
        public long lastUpdateTime;
        public int unit;
        public String registrationDate;
        public int pTime;
        public double cFactor;
        public int guest;
        public String fwVersion;
        public String wifiName;
    }
}