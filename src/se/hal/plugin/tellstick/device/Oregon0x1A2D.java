package se.hal.plugin.tellstick.device;

import se.hal.intf.HalEventController;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.plugin.tellstick.TellstickDevice;
import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.plugin.tellstick.TellstickSerialComm;
import se.hal.plugin.tellstick.protocol.Oregon0x1A2DProtocol;
import se.hal.struct.devicedata.PowerConsumptionSensorData;
import zutil.log.LogUtil;
import zutil.ui.Configurator;

import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-11-19.
 */
public class Oregon0x1A2D implements HalSensorConfig,TellstickDevice {
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable("Address")
    private int address = 0;
    @Configurator.Configurable("Report Interval(ms)")
    private int interval = 60*1000; // default 1 min



    public Oregon0x1A2D() { }
    public Oregon0x1A2D(int address) {
        this.address = address;
    }


    @Override
    public boolean equals(Object obj){
        if(! (obj instanceof Oregon0x1A2D))
            return false;
        return ((Oregon0x1A2D)obj).address == this.address;
    }

    public String toString(){
        return "address:"+address;
    }



    @Override
    public long getDataInterval() {
        return interval;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.SUM;
    }

    @Override
    public Class<? extends HalSensorController> getSensorControllerClass() {
        return TellstickSerialComm.class;
    }

    @Override
    public Class<? extends HalSensorData> getSensorDataClass() {
        return PowerConsumptionSensorData.class; // TODO: needs to support all data, add enum?
    }

    @Override
    public String getProtocolName() { return Oregon0x1A2DProtocol.PROTOCOL; }
    @Override
    public String getModelName() { return Oregon0x1A2DProtocol.MODEL; }
}
