/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Ziver Koc
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

package se.hal.plugin.nvr.rtsp;

import se.hal.intf.HalDeviceConfig;
import se.hal.intf.HalDeviceReportListener;
import se.hal.plugin.nvr.intf.HalCameraController;
import zutil.log.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


public class RTSPController implements HalCameraController {
    private static final Logger logger = LogUtil.getLogger();

    private static final String CONFIG_RECORDING_PATH = "nvr.recording_path";

    private List<RTSPCameraConfig> cameras = new ArrayList<>();
    private HalDeviceReportListener listener;


    public RTSPController() {}


    // ----------------------------------------------------
    // Lifecycle methods
    // ----------------------------------------------------

    @Override
    public void initialize() {

    }

    @Override
    public void close() {

    }

    // ----------------------------------------------------
    // Data methods
    // ----------------------------------------------------

    @Override
    public void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof RTSPCameraConfig)
            cameras.add((RTSPCameraConfig) deviceConfig);
    }

    @Override
    public void deregister(HalDeviceConfig deviceConfig) {
        cameras.remove(deviceConfig);
    }

    @Override
    public int size() {
        return cameras.size();
    }

    @Override
    public void setListener(HalDeviceReportListener listener) {
        this.listener = listener;
    }

}
