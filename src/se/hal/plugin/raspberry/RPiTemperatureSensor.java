package se.hal.plugin.raspberry;

import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.struct.devicedata.TemperatureSensorData;
import zutil.ui.Configurator;

public class RPiTemperatureSensor implements HalSensorConfig {
	
	@Configurator.Configurable("1-Wire Address")
    private String w1Address = null;


	

    @Override
    public long getDataInterval() {
        return 10*60*1000; // 10 min
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.AVERAGE;
    }

    @Override
    public Class<? extends HalSensorController> getSensorController() {
        return RPiController.class;
    }

    @Override
    public boolean equals(Object obj){
    	if(obj instanceof RPiTemperatureSensor)
    		return obj == this;
    	return false;
    }

	public String get1WAddress() {
		return w1Address;
	}

}
