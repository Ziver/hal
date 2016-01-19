package se.hal.plugin.raspberry.hardware;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import se.hal.plugin.raspberry.RPiTemperatureSensor;
import se.hal.plugin.raspberry.RPiController;
import se.hal.plugin.raspberry.RPiSensor;
import zutil.log.LogUtil;

import com.pi4j.component.temperature.TemperatureSensor;
import com.pi4j.io.w1.W1Master;
import com.pi4j.temperature.TemperatureScale;

public class RPiDS18B20 implements RPiSensor, Runnable {	
	private static final Logger logger = LogUtil.getLogger();
	private final String DEGREE_SIGN  = "\u00b0";
	
	private RPiController controller;
	private String w1Address;
	private ScheduledExecutorService scheduler;
	private W1Master w1Mater;
	
	public RPiDS18B20(String w1Address, RPiController controller){
		this.controller = controller;
		this.w1Address = w1Address;

		scheduler = Executors.newScheduledThreadPool(1);
		
		w1Mater = new W1Master();
		
		//print out all sensors found
		for(TemperatureSensor device : w1Mater.getDevices(TemperatureSensor.class)){
			logger.info(String.format("1-Wire temperature sensor divice found: %-20s: %3.1f"+DEGREE_SIGN+"C\n", device.getName(), device.getTemperature(TemperatureScale.CELSIUS)));
		}
		
		//schedule job
		scheduler.scheduleAtFixedRate(this, 10, 60, TimeUnit.SECONDS);	//wait 10s and run every 60s
		
    }
    
    public void close() {
    	scheduler.shutdown();
    	try {
			scheduler.awaitTermination(5, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			//noop
		}
    }

	@Override
	public void run() {
		for(TemperatureSensor device : w1Mater.getDevices(TemperatureSensor.class)){
			if(device.getName().equals(w1Address)){
				controller.sendDataReport(new RPiTemperatureSensor(System.currentTimeMillis(), device.getTemperature(TemperatureScale.CELSIUS)));
				break;
			}
		}
	}
    
}
