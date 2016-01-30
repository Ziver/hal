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
	
	public RPiPowerConsumptionSensor(int gpioPin, long timestamp, double data) {
		this.gpioPin = gpioPin;
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
    	if(!(obj instanceof RPiPowerConsumptionSensor))
    		return false;
    	return ((RPiPowerConsumptionSensor)obj).gpioPin == gpioPin;
    }

	public int getGpioPin() {
		return gpioPin;
	}

    public String toString(){
        return "gpioPin:" + gpioPin +", data:" + data;
    }
}
