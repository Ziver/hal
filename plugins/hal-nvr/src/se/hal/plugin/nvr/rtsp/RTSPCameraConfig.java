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

import se.hal.intf.HalDeviceData;
import se.hal.plugin.nvr.intf.HalCameraConfig;
import se.hal.plugin.nvr.intf.HalCameraController;
import zutil.ui.conf.Configurator;

public class RTSPCameraConfig implements HalCameraConfig {

    @Configurator.Configurable(value = "RTSP URL", description = "Url to the RTSP stream of the camera. (Should start with rtsp://)")
    private String rtspUrl;


    public RTSPCameraConfig() {}
    public RTSPCameraConfig(String rtspUrl) {
        this.rtspUrl = rtspUrl;
    }


    public String getRtspUrl() {
        return rtspUrl;
    }


    @Override
    public Class<? extends HalCameraController> getDeviceControllerClass() {
        return RTSPController.class;
    }

    @Override
    public Class<? extends HalDeviceData> getDeviceDataClass() {
        return null; // TODO:
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RTSPCameraConfig)) return false;

        RTSPCameraConfig that = (RTSPCameraConfig) o;

        return rtspUrl != null ? rtspUrl.equals(that.rtspUrl) : that.rtspUrl == null;
    }

    @Override
    public int hashCode() {
        return rtspUrl != null ? rtspUrl.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "URL: " + rtspUrl;
    }
}
