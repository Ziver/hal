package se.hal.plugin.nutups;

import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.struct.PowerConsumptionSensorData;
import zutil.osal.app.linux.NutUPSClient;
import zutil.ui.Configurator;

/**
 * Created by Ziver on 2016-05-25.
 */
public class NutUpsDevice implements PowerConsumptionSensorData{

    @Configurator.Configurable("UPS id")
    private String deviceId;
    private long timestamp;
    private double consumption;


    public NutUpsDevice(){}

    protected NutUpsDevice(NutUPSClient.UPSDevice ups){
        this.deviceId = ups.getId();
        this.timestamp = System.currentTimeMillis();
        this.consumption = ups.getPowerUsage() * 1/60.0; // convert watt min to watt hour
    }


    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public double getData() {
        return consumption;
    }

    @Override
    public long getDataInterval(){
        return 60*1000; // 1 min
    }

    @Override
    public boolean equals(Object obj){
        if (obj instanceof NutUpsDevice)
            return deviceId != null && deviceId.equals(((NutUpsDevice)obj).deviceId);
        return false;
    }

    public String toString(){
        return "id: "+deviceId +
                ", consumption: "+consumption;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.SUM;
    }
    @Override
    public Class<? extends HalSensorController> getSensorController() {
        return NutUpsController.class;
    }
}
