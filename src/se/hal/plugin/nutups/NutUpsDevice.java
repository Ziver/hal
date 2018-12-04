package se.hal.plugin.nutups;

import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.struct.devicedata.PowerConsumptionSensorData;
import zutil.osal.linux.app.NutUPSClient;
import zutil.ui.Configurator;

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
        data.setData(ups.getPowerUsage() * 1/60.0); // convert watt min to watt hour
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
    public boolean equals(Object obj){
        if (obj instanceof NutUpsDevice)
            return upsId != null && upsId.equals(((NutUpsDevice)obj).upsId);
        return false;
    }

    public String toString(){
        return "upsId: "+ upsId;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.SUM;
    }
    @Override
    public Class<? extends HalSensorController> getSensorControllerClass() {
        return NutUpsController.class;
    }
    @Override
    public Class<? extends HalSensorData> getSensorDataClass() {
        return PowerConsumptionSensorData.class;
    }
}
