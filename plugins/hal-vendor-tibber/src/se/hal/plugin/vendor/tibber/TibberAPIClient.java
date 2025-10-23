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

package se.hal.plugin.vendor.tibber;

import se.hal.HalContext;
import zutil.ObjectUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpClient;
import zutil.net.http.HttpHeader;
import zutil.parser.DataNode;
import zutil.parser.json.JSONParser;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

/**
 * A class that interfaces with the Tibber API.
 */
public class TibberAPIClient {
    private static final Logger logger = LogUtil.getLogger();

    private static final String TIBBER_API_ENDPOINT = "https://api.tibber.com/v1-beta/gql";
    private static SimpleDateFormat DATE_PARSER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX"); // 2024-12-28T23:00:00.000+01:00

    private String tibberToken;


    /**
     * Creates an instance of the TibberAPIClient.
     *
     * @param token     is the authentication token required to connect to the API.
     */
    public TibberAPIClient(String token) {
        this.tibberToken = token;
    }


    /**
     * Retrieves the consumption reported by Tibber.
     *
     * @return
     */
    public TibberConsumption getConsumption() throws IOException {
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
                            "currentSubscription {" +
                                "priceInfo {" +
                                    "current {" +
                                        "total " +
                                        "energy " +
                                        "tax " +
                                        "startsAt " +
                                    "}" +
                                "}" +
                            "}" +
                        "}" +
                    "}" +
                "}\"" +
            "}";
        DataNode responseRoot = sendRequest(requestQuery);
        DataNode data = responseRoot.get("data").get("viewer").get("homes").get(0).get("consumption").get("nodes").get(0);
        // Data example:
        // unitPrice=0.1507875, cost=0.3131856375, from=2024-12-28T21:00:00.000+01:00, consumption=2.077, to=2024-12-28T22:00:00.000+01:00, unitPriceVAT=0.0301575, consumptionUnit=kWh

        TibberConsumption consumption = new TibberConsumption();

        try {
            consumption.timestamp = DATE_PARSER.parse(data.getString("from")).getTime();
        } catch (ParseException e) {
            logger.warning("Was unable to parse timestamp from Tibber API.");
        }

        if (data.get("cost") != null) {
            consumption.cost = data.getDouble("cost");
        }
        if (data.get("unitPrice") != null) {
            consumption.unitPrice = data.getDouble("unitPrice");
        }
        if (data.get("unitPriceVAT") != null) {
            consumption.unitPriceVAT = data.getDouble("unitPriceVAT");
        }
        if (data.get("consumption") != null) {
            consumption.consumption = data.getDouble("consumption");
        }
        if (data.get("consumptionUnit") != null) {
            consumption.consumptionUnit = data.getString("consumptionUnit");
        }

        return consumption;
    }


    private DataNode sendRequest(String query) throws IOException {
        HttpClient client = new HttpClient(HttpClient.HttpRequestType.POST);
        client.setURL(TIBBER_API_ENDPOINT);
        client.setHeader("Authorization", "Bearer " + HalContext.getStringProperty(tibberToken));
        client.setHeader("Content-Type", "application/json");
        client.setContent(query);
        logger.finest("Request: " + query);

        HttpHeader header = client.send();
        JSONParser parser = new JSONParser(header.getInputStream());
        DataNode json = parser.read();
        logger.finest("Received data from Tibber API: " + json);

        if (header.getResponseStatusCode() != 200) {
            throw new IOException("There was a error fetching data from tibber: StatusCode=" + header.getResponseStatusCode() + ", Response=" + header.getResponseStatusString());
        }

        if (!ObjectUtil.isEmpty(json.get("error"))) {
            throw new IOException("There was a error fetching data from tibber: " + json.getString("error"));
        }

        return json;
    }

    public static class TibberConsumption {
        /** The timestamp of when the data was reported */
        public long timestamp;
        /** Total cost */
        public double cost;
        /** Cost per Kwh */
        public double unitPrice;
        /** Cost per Kwh with taxes */
        public double unitPriceVAT;
        /** Total consumption */
        public double consumption;
        /** Consumption unit, should in general be Kwh */
        public String consumptionUnit;
    }
}
