package se.hal.plugin.tellstick.device;

import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.plugin.tellstick.TellstickDevice;
import se.hal.plugin.tellstick.TellstickSerialComm;
import se.hal.plugin.tellstick.protocol.Oregon0x1A2DProtocol;
import se.hal.struct.devicedata.HumiditySensorData;
import se.hal.struct.devicedata.LightSensorData;
import se.hal.struct.devicedata.PowerConsumptionSensorData;
import se.hal.struct.devicedata.TemperatureSensorData;
import zutil.log.LogUtil;
import zutil.ui.Configurator;

import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-11-19.
 */
public class Oregon0x1A2D implements HalSensorConfig,TellstickDevice {
    private static final Logger logger = LogUtil.getLogger();

    public enum OregonSensorType{
        HUMIDITY,LIGHT,POWER,TEMPERATURE
    }

    @Configurator.Configurable("Address")
    private int address = 0;
    @Configurator.Configurable("Report Interval(ms)")
    private int interval = 60*1000; // default 1 min
    @Configurator.Configurable("Sensor Type")
    private OregonSensorType sensorType;



    public Oregon0x1A2D() { }
    public Oregon0x1A2D(int address, OregonSensorType sensorType) {
        this.address = address;
        this.sensorType = sensorType;
    }


    public int getAddress() {
        return address;
    }
    @Override
    public long getDataInterval() {
        return interval;
    }
    public OregonSensorType getSensorType() {
        return sensorType;
    }

    @Override
    public boolean equals(Object obj){
        if(! (obj instanceof Oregon0x1A2D))
            return false;
        return ((Oregon0x1A2D)obj).address == this.address &&
                ((Oregon0x1A2D)obj).sensorType == this.sensorType;
    }

    public String toString(){
        return "address:"+address+",sensorType:"+ sensorType;
    }


    @Override
    public AggregationMethod getAggregationMethod() {
        if (sensorType == OregonSensorType.POWER)
            return AggregationMethod.SUM;
        return AggregationMethod.AVERAGE;
    }

    @Override
    public Class<? extends HalSensorController> getSensorControllerClass() {
        return TellstickSerialComm.class;
    }

    @Override
    public Class<? extends HalSensorData> getSensorDataClass() {
        switch (sensorType){
            case HUMIDITY:      return HumiditySensorData.class;
            case LIGHT:         return LightSensorData.class;
            case POWER:         return PowerConsumptionSensorData.class;
            case TEMPERATURE:   return TemperatureSensorData.class;
        }
        return null;
    }

    @Override
    public String getProtocolName() { return Oregon0x1A2DProtocol.PROTOCOL; }
    @Override
    public String getModelName() { return Oregon0x1A2DProtocol.MODEL; }
}
