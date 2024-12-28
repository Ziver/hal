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

package se.hal.util;

import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceData;
import se.hal.intf.HalDeviceReportListener;
import zutil.log.LogUtil;

import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Utility class that contains general code for interacting with listeners.
 */
public class ListenerUtil {
    private static final Logger logger = LogUtil.getLogger();

    /**
     * Helper function to call {@link HalDeviceReportListener#reportReceived(HalDeviceConfig, HalDeviceData)} on all provided
     * listeners with the same input parameters. Exceptions will be handled individually per listener and will not prevent
     * other listeners to be called.
     *
     * @param deviceListeners   a list of listeners to be called.
     * @param deviceConfig      the device config that will be provided on each call.
     * @param deviceData        the data config that will be provided on each call.
     */
    public static void callReportReceived(List<HalDeviceReportListener> deviceListeners, HalDeviceConfig deviceConfig, HalDeviceData deviceData) {
        for (HalDeviceReportListener deviceListener : deviceListeners) {
            try {
                deviceListener.reportReceived(deviceConfig, deviceData);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Device listener threw exception during new device report, exception is ignored.", e);
            }
        }
    }
}
