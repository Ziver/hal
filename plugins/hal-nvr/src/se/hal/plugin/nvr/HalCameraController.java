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

package se.hal.plugin.nvr;

import se.hal.intf.HalAbstractController;
import se.hal.plugin.nvr.device.HalCameraConfig;
import se.hal.plugin.nvr.device.HalCameraReportListener;

public interface HalCameraController extends HalAbstractController {

    /**
     * Will register a camera to be handled by this controller.
     */
    void register(HalCameraConfig cameraConfig);

    /**
     * Deregisters a camera from this controller, the controller
     * will no longer handle camera device.
     */
    void deregister(HalCameraConfig cameraConfig);

    /**
     * Set a listener that will receive all reports from the the registered camera.
     */
    void setListener(HalCameraReportListener listener);
}
