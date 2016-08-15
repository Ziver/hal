package se.hal.plugin.raspberry;

import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.struct.TemperatureSensorData;
import zutil.ui.Configurator;

public class RPiTemperatureSensor implements TemperatureSensorData {
	
	@Configurator.Configurable("1-Wire Address")
    private String w1Address = null;
	
	private double data;
	private long timestamp;
	
	public RPiTemperatureSensor(){
		//need to be empty for the framework to create an instance
	}
	
	public RPiTemperatureSensor(long timestamp, double data) {
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

	@Override
	public double getTemperature() {
		return data;
	}
}
