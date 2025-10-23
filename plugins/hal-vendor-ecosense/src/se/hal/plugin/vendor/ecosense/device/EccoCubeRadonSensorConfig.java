/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2024-2025 Ziver Koc
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

package se.hal.plugin.vendor.ecosense.device;

import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.plugin.vendor.ecosense.EcoSenseController;
import se.hal.struct.devicedata.RadonSensorData;
import zutil.ui.conf.Configurator;

import java.util.Objects;

/**
 * A sensor that calculate current radon level
 */
public class EccoCubeRadonSensorConfig implements HalSensorConfig {

    @Configurator.Configurable(value = "Device serial number")
    private String serialNumber;


    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public long getDataInterval() {
        return 10*60*1000; // 10 min
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }

    @Override
    public Class<? extends HalSensorController> getDeviceControllerClass() {
        return EcoSenseController.class;
    }

    @Override
    public Class<? extends HalSensorData> getDeviceDataClass() {
        return RadonSensorData.class;
    }

    @Override
    public final boolean equals(Object o) {
        if (!(o instanceof EccoCubeRadonSensorConfig)) return false;

        EccoCubeRadonSensorConfig that = (EccoCubeRadonSensorConfig) o;
        return Objects.equals(serialNumber, that.serialNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(serialNumber);
    }

    @Override
    public String toString() {
        return "Serial number: " + serialNumber;
    }
}
