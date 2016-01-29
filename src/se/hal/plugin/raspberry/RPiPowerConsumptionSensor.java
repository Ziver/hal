package se.hal.plugin.raspberry;

import com.pi4j.io.gpio.Pin;

import se.hal.intf.HalSensorController;
import se.hal.struct.PowerConsumptionSensorData;
import zutil.ui.Configurator;

public class RPiPowerConsumptionSensor implements PowerConsumptionSensorData {
	
	@Configurator.Configurable("GPIO-Pin")
    private int gpioPin = -1;
	
	private double data;
	private long timestamp;
	
	public RPiPowerConsumptionSensor(){
		//need to be empty for the framework to create an instance
	}
	
	public RPiPowerConsumptionSensor(long timestamp, double data) {
		this.timestamp = timestamp;
		this.data = data;
	}
	
    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public double getData() {
        return data;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.SUM;
    }

    @Override
    public Class<? extends HalSensorController> getSensorController() {
        return RPiController.class;
    }

    public boolean equals(Object obj){
    	if(obj instanceof RPiPowerConsumptionSensor)
    		return obj == this;
    	return false;
    }

	public Pin getGpioPin() {
		return RPiUtility.getPin(gpioPin);
	}
}
