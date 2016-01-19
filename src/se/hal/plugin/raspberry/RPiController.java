package se.hal.plugin.raspberry;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.pi4j.io.gpio.Pin;

import se.hal.intf.HalSensorData;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorReportListener;
import se.hal.plugin.raspberry.hardware.RPiDS18B20;
import se.hal.plugin.raspberry.hardware.RPiInteruptPulseFlankCounter;
import zutil.log.LogUtil;

public class RPiController implements HalSensorController {
	private static final Logger logger = LogUtil.getLogger();
	
	private HashMap<String, RPiSensor> pinToSensorMap = new HashMap<>();
	private HalSensorReportListener sensorListener;
	
	public RPiController(){
        
	}
	
    @Override
    public void initialize() throws Exception {
    	
    }

    @Override
    public void register(HalSensorData sensor) {
    	if(sensor instanceof RPiPowerConsumptionSensor){
    		RPiPowerConsumptionSensor powerConsumprtionSensor = (RPiPowerConsumptionSensor) sensor;
    		Pin gpioPin = powerConsumprtionSensor.getGpioPin();
    		if(!pinToSensorMap.containsKey(gpioPin.getName())){
    			RPiInteruptPulseFlankCounter impulseCounter = new RPiInteruptPulseFlankCounter(gpioPin, this);
            	pinToSensorMap.put(gpioPin.getName(), impulseCounter);
    		}else{
    			logger.warning("Cannot create a RPiPowerConsumptionSensor on GPIO pin " + gpioPin + " since is already is in use by another sensor.");
    		}
    	} else if(sensor instanceof RPiTemperatureSensor){
    		RPiTemperatureSensor temperatureSensor = (RPiTemperatureSensor) sensor;
    		String w1Address = temperatureSensor.get1WAddress();
    		if(!pinToSensorMap.containsKey(w1Address)){
	    		RPiDS18B20 ds12b20 = new RPiDS18B20(w1Address, this);
	        	pinToSensorMap.put(w1Address, ds12b20);
    		}else{
    			logger.warning("Cannot create a RPi1WireTemperatureSensor on 1-Wire address " + w1Address + " since is already is in use by another sensor.");
    		}
    	}else{
    		logger.warning("Cannot register a non-supported sensor");
    	}
    }

    @Override
    public void deregister(HalSensorData sensor) {
    	if(sensor instanceof RPiPowerConsumptionSensor){
    		RPiPowerConsumptionSensor powerConsumprtionSensor = (RPiPowerConsumptionSensor) sensor;
    		RPiSensor sensorToDeregister = pinToSensorMap.remove(powerConsumprtionSensor.getGpioPin().getName());
    		if(sensorToDeregister != null){
    			sensorToDeregister.close();
    		}
    	} else if(sensor instanceof RPiTemperatureSensor){
    		RPiTemperatureSensor temperatureSensor = (RPiTemperatureSensor) sensor;
    		RPiSensor sensorToDeregister = pinToSensorMap.remove(temperatureSensor.get1WAddress());
    		if(sensorToDeregister != null){
    			sensorToDeregister.close();
    		}
    	}else{
    		logger.warning("Cannot deregister a non-supported sensor");
    		return;
    	}
    }

    @Override
    public int size() {
        return pinToSensorMap.size();
    }

    @Override
    public void setListener(HalSensorReportListener listener) {
    	sensorListener = listener;
    }

    @Override
    public void close() {
    	for(String key : this.pinToSensorMap.keySet()){
    		pinToSensorMap.get(key).close();
    		pinToSensorMap.remove(key);
    	}
    }
    
    public void sendDataReport(HalSensorData sensorData){
    	if(sensorListener != null){
    		sensorListener.reportReceived(sensorData);
    	}else{
        	logger.log(Level.WARNING, "Could not report data. No registered listener");
        }
    }
	
}
