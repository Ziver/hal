/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2020 Ziver Koc
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

package se.hal.plugin.zigbee.deconz.zigbee;

import se.hal.HalContext;
import se.hal.intf.*;
import zutil.log.LogUtil;

import java.util.logging.Logger;

/**
 * Class will handle Zigbee devices through the deConz REST API and with devices supporting.
 *
 * <p>
 * Rest documentatiuon for deConz: https://dresden-elektronik.github.io/deconz-rest-doc/
 */
public class DeConzZigbeeController implements HalSensorController, HalEventController, HalAutostartController {
    private static final Logger logger = LogUtil.getLogger();

    public static final String CONFIG_ZIGBEE_REST_URL = "zigbee.rest_url";
    public static final String CONFIG_ZIGBEE_REST_PORT = "zigbee.rest_port";
    public static final String CONFIG_ZIGBEE_REST_USERNAME = "zigbee.rest_username";
    public static final String CONFIG_ZIGBEE_REST_PASSWORD = "zigbee.rest_password";
    public static final String CONFIG_ZIGBEE_COM_PORT = "zigbee.com_port";


    @Override
    public boolean isAvailable() {
        return HalContext.containsProperty(CONFIG_ZIGBEE_REST_URL);
    }

    @Override
    public void initialize() throws Exception {
        // connect to deconz
        // if username is set use that for basic auth
        // else try without username or fail with log message that username should be setup

        // Get API key
    }


    @Override
    public void register(HalDeviceConfig deviceConfig) {

    }

    @Override
    public void deregister(HalDeviceConfig deviceConfig) {

    }

    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) {

    }

    @Override
    public void setListener(HalDeviceReportListener listener) {

    }


    @Override
    public int size() {
        return 0;
    }

    @Override
    public void close() {

    }
}
