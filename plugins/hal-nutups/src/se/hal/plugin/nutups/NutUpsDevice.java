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

package se.hal.plugin.nutups;

import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.struct.devicedata.PowerConsumptionSensorData;
import zutil.osal.linux.app.NutUPSClient;
import zutil.ui.conf.Configurator;

public class NutUpsDevice implements HalSensorConfig{

    @Configurator.Configurable("UPS id")
    private String upsId;


    public NutUpsDevice(){}

    protected NutUpsDevice(NutUPSClient.UPSDevice ups){
        this.upsId = ups.getId();
    }


    protected HalSensorData read(NutUPSClient.UPSDevice ups){
        PowerConsumptionSensorData data = new PowerConsumptionSensorData();
        data.setTimestamp(System.currentTimeMillis());
        data.setData(ups.getPowerUsage() * 1/60.0); // Convert watt minutes to watt hour
        return data;
    }


    public String getUpsId(){
        return upsId;
    }


    @Override
    public long getDataInterval(){
        return 60*1000; // 1 min
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.SUM;
    }
    @Override
    public Class<? extends HalSensorController> getDeviceControllerClass() {
        return NutUpsController.class;
    }
    @Override
    public Class<? extends HalSensorData> getDeviceDataClass() {
        return PowerConsumptionSensorData.class;
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof NutUpsDevice)
            return upsId != null && upsId.equals(((NutUpsDevice)obj).upsId);
        return false;
    }
    @Override
    public String toString(){
        return "upsId: "+ upsId;
    }

}
