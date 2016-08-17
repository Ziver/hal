package se.hal.plugin.tellstick.device;

import se.hal.intf.HalEventController;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.plugin.tellstick.TellstickSerialComm;
import zutil.log.LogUtil;
import zutil.ui.Configurator;

import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-11-19.
 */
public class Oregon0x1A2D implements HalSensorConfig {
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable("Address")
    private int address = 0;
    @Configurator.Configurable("Report Interval(ms)")
    private int interval = 60*1000; // default 1 min




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
    public Class<? extends HalSensorController> getSensorController() {
        return TellstickSerialComm.class;
    }

}
