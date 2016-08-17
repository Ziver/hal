package se.hal.plugin.raspberry;

import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.struct.devicedata.PowerConsumptionSensorData;
import zutil.ui.Configurator;

public class RPiPowerConsumptionSensor implements HalSensorConfig {
	
	@Configurator.Configurable("GPIO-Pin")
    private int gpioPin = -1;

	
	public RPiPowerConsumptionSensor(){
		//need to be empty for the framework to create an instance
	}
	
	public RPiPowerConsumptionSensor(int gpioPin) {
		this.gpioPin = gpioPin;
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
    public Class<? extends HalSensorController> getSensorController() {
        return RPiController.class;
    }

    @Override
    public boolean equals(Object obj){
    	if(!(obj instanceof RPiPowerConsumptionSensor))
    		return false;
    	return ((RPiPowerConsumptionSensor)obj).gpioPin == gpioPin;
    }

	public int getGpioPin() {
		return gpioPin;
	}

    public String toString(){
        return "gpioPin:" + gpioPin;
    }
}
