package se.hal.plugin.raspberry;

import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorReportListener;
import se.hal.plugin.raspberry.hardware.RPiDS18B20;
import se.hal.plugin.raspberry.hardware.RPiInteruptPulseFlankCounter;
import zutil.log.LogUtil;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    public void register(HalSensorConfig sensor) {
    	if(sensor instanceof RPiPowerConsumptionSensor){
    		RPiPowerConsumptionSensor powerConsumptionSensor = (RPiPowerConsumptionSensor) sensor;
    		int gpioPin = powerConsumptionSensor.getGpioPin();
    		if(!pinToSensorMap.containsKey("GPIO_"+gpioPin)){
    			RPiInteruptPulseFlankCounter impulseCounter = new RPiInteruptPulseFlankCounter(gpioPin, this);
            	pinToSensorMap.put("GPIO_"+gpioPin, impulseCounter);
    		}else{
    			logger.warning("Cannot create a RPiPowerConsumptionSensor on GPIO pin " + gpioPin + " since is already is in use by another sensor.");
    		}
    	} else if(sensor instanceof RPiTemperatureSensor){
    		RPiTemperatureSensor temperatureSensor = (RPiTemperatureSensor) sensor;
    		String w1Address = temperatureSensor.get1WAddress();
    		if(!pinToSensorMap.containsKey("W1_"+w1Address)){
	    		RPiDS18B20 ds12b20 = new RPiDS18B20(w1Address, this);
	        	pinToSensorMap.put("W1_"+w1Address, ds12b20);
    		}else{
    			logger.warning("Cannot create a RPi1WireTemperatureSensor on 1-Wire address " + w1Address + " since is already is in use by another sensor.");
    		}
    	}else{
    		logger.warning("Cannot register a non-supported sensor");
    	}
    }

    @Override
    public void deregister(HalSensorConfig sensor) {
    	if(sensor instanceof RPiPowerConsumptionSensor){
    		RPiPowerConsumptionSensor powerConsumprtionSensor = (RPiPowerConsumptionSensor) sensor;
    		RPiSensor sensorToDeregister = pinToSensorMap.remove("GPIO_"+powerConsumprtionSensor.getGpioPin());
    		if(sensorToDeregister != null){
    			sensorToDeregister.close();
    		}
    	} else if(sensor instanceof RPiTemperatureSensor){
    		RPiTemperatureSensor temperatureSensor = (RPiTemperatureSensor) sensor;
    		RPiSensor sensorToDeregister = pinToSensorMap.remove("W1_"+temperatureSensor.get1WAddress());
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
    
    public void sendDataReport(HalSensorConfig sensorConfig){
    	if(sensorListener != null){
    		sensorListener.reportReceived(sensorConfig);
    	}else{
        	logger.log(Level.WARNING, "Could not report data. No registered listener");
        }
    }
	
}
