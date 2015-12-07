package se.koc.hal.plugin.localsensor;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import zutil.db.DBConnection;
import zutil.log.LogUtil;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

public class ImpulseTracker implements Runnable {
	private static final Logger logger = LogUtil.getLogger();
	
    private static final int IMPULSE_REPORT_TIMEOUT = 60000;   //one minute
    private long nanoSecondsSleep = IMPULSE_REPORT_TIMEOUT * 1000000L;
    private Integer impulseCount = 0;
    private ExecutorService executorPool;
    private final DBConnection db;
    
    public static void main(String args[]) throws Exception {
        new ImpulseTracker();
    }
    
    public ImpulseTracker() throws Exception{
        
        // create gpio controller
        final GpioController gpio = GpioFactory.getInstance();

        // provision gpio pin #02 as an input pin with its internal pull up resistor enabled
        final GpioPinDigitalInput irLightSensor = gpio.provisionDigitalInputPin(RaspiPin.GPIO_02, PinPullResistance.PULL_UP);

        // create and register gpio pin listener
        irLightSensor.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if(event.getState() == PinState.LOW){  //low = light went on
                    //System.out.println("IR LED turned ON");
                    synchronized(impulseCount){
                        impulseCount++;
                    }
                }
            }
            
        });
        
        this.executorPool = Executors.newCachedThreadPool();
        
        logger.info("Connecting to db...");
        db = new DBConnection(DBConnection.DBMS.SQLite, "hal.db");
        
        //start a daemon thread to save the impulse count every minute 
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
        
    }
    
    @Override
	public void run() {
		long startTime = System.nanoTime();
		synchronized(impulseCount){
            impulseCount = 0;	//reset the impulse count
        }
        while(true) {
            sleepNano(nanoSecondsSleep);
            int count = -1;
            synchronized(impulseCount){
                count = impulseCount;
                impulseCount = 0;
            }
            save(System.currentTimeMillis(), count);	//save the impulse count
            long estimatedNanoTimeSpent = System.nanoTime() - startTime;  //this is where the loop ends
            startTime = System.nanoTime();  //this is where the loop starts from now on
            if(estimatedNanoTimeSpent > 0){  //if no overflow
                long nanoSecondsTooMany = estimatedNanoTimeSpent - (IMPULSE_REPORT_TIMEOUT*1000000L);
                //System.out.println("the look took ~" + estimatedNanoTimeSpent + "ns. That is " + nanoSecondsTooMany/1000000L + "ms off");
                nanoSecondsSleep -= nanoSecondsTooMany / 3;  //divide by constant to take into account varaiations im loop time
            }
        }
	}
    
    private void sleepNano(long ns){
        //System.out.println("will go to sleep for " + ns + "ns");
    	try{
    		Thread.sleep(ns/1000000L, (int)(ns%1000000L));
    	}catch(InterruptedException e){
    		//ignore
    	}
    }
    
    private void save(long timestamp_end, int data){
    	executorPool.execute(new Runnable(){
			@Override
			public void run() {
				try {
					db.exec("INSERT INTO sensor_data_raw(timestamp, sensor_id, data) VALUES("+timestamp_end+", "+2+", "+data+")");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
    }
    
}
