/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024 Ziver Koc
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

package se.hal.plugin.tibber;

import se.hal.HalContext;
import se.hal.HalServer;
import se.hal.intf.*;
import zutil.ObjectUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpClient;
import zutil.net.http.HttpHeader;
import zutil.parser.DataNode;
import zutil.parser.json.JSONParser;
import zutil.ui.UserMessageManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


public class TibberController implements HalSensorController, Runnable, HalDaemon, HalAutostartController {
    private static final Logger logger = LogUtil.getLogger();

    private static final String CONFIG_TIBBER_TOKEN = "hal_tibber.token";

    private static final String TIBBER_API_ENDPOINT = "https://api.tibber.com/v1-beta/gql";
    private static final int POLL_TIME = 60*60; // poll every 1h defined in sec

    private List<TibberDevice> registeredDevices = new ArrayList<>();
    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();
    private ScheduledExecutorService executor;


    public TibberController() {}

    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_TIBBER_TOKEN);
    }

    @Override
    public void initialize() {
        HalServer.registerDaemon(this);
    }

    @Override
    public void initiate(ScheduledExecutorService executor) {
        this.executor = executor;
        executor.scheduleAtFixedRate(this, 10, POLL_TIME, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void run() {
        if (!isAvailable()) {
            logger.warning("Controller not available yet.");
            return;
        }

        try {
            logger.fine("Querying Tibber API for new data.");

            HttpClient client = new HttpClient(HttpClient.HttpRequestType.POST);
            client.setURL(TIBBER_API_ENDPOINT);
            client.setHeader("Authorization", "Bearer " + HalContext.getStringProperty(CONFIG_TIBBER_TOKEN));
            client.setContent("application/json");
            String requestQuery = "{ " +
                "\"query\": \"{" +
                    "viewer {" +
                        "homes {" +
                            "consumption(resolution: HOURLY, last: 1) {" +
                                "nodes {" +
                                    "from " +
                                    "to " +
                                    "cost " + // Total cost
                                    "unitPrice " + // Cost per Kwh
                                    "unitPriceVAT " + // Cost per Kwh with taxes
                                    "consumption " + // Total consumption
                                    "consumptionUnit " + // Should be Kwh
                                "}" +
                            "}" +
//                            "currentSubscription {" +
//                                "priceInfo {" +
//                                    "current {" +
//                                        "total " +
//                                        "energy " +
//                                        "tax " +
//                                        "startsAt " +
//                                    "}" +
//                                "}" +
//                            "}" +
                        "}" +
                    "}" +
                "}\"" +
            "}";
            client.setContent(requestQuery);
            logger.finest("Request: " + requestQuery);

            HttpHeader header = client.send();
            JSONParser parser = new JSONParser(header.getInputStream());
            DataNode json = parser.read();
            logger.finest("Response: " + json);

            if (header.getResponseStatusCode() != 200) {
                logger.severe("There was a error fetching data from tibber: StatusCode=" + header.getResponseStatusCode() + ", Response=" + header.getResponseStatusString());
                HalContext.getUserMessageManager().add(new UserMessageManager.UserMessage(
                        UserMessageManager.MessageLevel.ERROR,
                        "Tibber API error.",
                        "There was a error fetching data from tibber: StatusCode=" + header.getResponseStatusCode() + ", Response=" + header.getResponseStatusString(),
                        UserMessageManager.MessageTTL.DISMISSED));
                return;
            }

            if (!ObjectUtil.isEmpty(json.get("error"))) {
                logger.severe("There was a error fetching data from tibber: " + json.getString("error"));
                HalContext.getUserMessageManager().add(new UserMessageManager.UserMessage(
                        UserMessageManager.MessageLevel.ERROR,
                        "Tibber API error.",
                        "There was a error fetching data from tibber: " + json.getString("error"),
                        UserMessageManager.MessageTTL.DISMISSED));
                return;
            }

            DataNode data = json.get("data").get("viewer").get("homes").get(0).get("consumption").get("nodes").get(0);

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Tibber API querying failed.", e);
        }
    }

    @Override
    public synchronized void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof TibberDevice)
            registeredDevices.add((TibberDevice) deviceConfig);
    }

    @Override
    public synchronized void deregister(HalDeviceConfig deviceConfig) {
        registeredDevices.remove(deviceConfig);
    }

    @Override
    public int size() {
        return registeredDevices.size();
    }

    @Override
    public void addListener(HalDeviceReportListener listener) {
        if (!deviceListeners.contains(listener))
            deviceListeners.add(listener);
    }

    @Override
    public synchronized void close() {
        registeredDevices = new ArrayList<>();

    }
}
