package se.hal.plugin.localsensor;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import zutil.db.DBConnection;
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;

import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RPiImpulseCountSensor implements Runnable {
	private static final Logger logger = LogUtil.getLogger();
	
    private static final int REPORT_TIMEOUT = 60_000;   //one minute
    private long nanoSecondsSleep = REPORT_TIMEOUT * 1_000_000L;
    private volatile Integer impulseCount = 0;
    private ExecutorService executorPool;
    private final DBConnection db;
    private final int sensorId;
    
    public static void main(String args[]) throws Exception {
        new RPiImpulseCountSensor(2, RaspiPin.GPIO_02);
    }
    
    /**
     * Constructor
     * @param sensorId	The ID of this sensor. Will be written to the DB
     * @throws Exception
     */
    public RPiImpulseCountSensor(int sensorId, Pin pin) throws Exception{
        
        // init logging
        CompactLogFormatter formatter = new CompactLogFormatter();
        LogUtil.setLevel("se.hal", Level.FINEST);
        LogUtil.setFormatter("se.hal", formatter);
        LogUtil.setLevel("zutil", Level.FINEST);
        LogUtil.setFormatter("zutil", formatter);
        LogUtil.setGlobalFormatter(formatter);

    	this.sensorId = sensorId;
    	
        // create gpio controller
    	GpioController gpio = null;
    	try{
    		gpio = GpioFactory.getInstance();
    	}catch(IllegalArgumentException e){
    		logger.log(Level.SEVERE, "", e);
    		throw e;
    	}catch(UnsatisfiedLinkError e){
    		logger.log(Level.SEVERE, "", e);
    		throw e;
    	}

        // provision gpio pin as an input pin with its internal pull up resistor enabled
        final GpioPinDigitalInput irLightSensor = gpio.provisionDigitalInputPin(pin, PinPullResistance.PULL_UP);

        // create and register gpio pin listener. May require the program to be run as sudo if the GPIO pin has not been exported
        irLightSensor.addListener(new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                if(event.getState() == PinState.LOW){  //low = light went on
                    //System.out.println("IR LED turned ON");
                    //logger.log(Level.INFO, "IR LED turned on");
                    synchronized(impulseCount){
                        impulseCount++;
                    }
                }
            }
            
        });
        
        // setup a thread pool for executing database jobs
        this.executorPool = Executors.newCachedThreadPool();
        
        // Connect to the database
        logger.info("Connecting to db...");
        db = new DBConnection(DBConnection.DBMS.SQLite, "hal.db");
        
        //start a daemon thread to save the impulse count every minute 
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
        
    }
    
    /**
     * This loop will try to save the current time and the number of impulses seen every [IMPULSE_REPORT_TIMEOUT] milliseconds.
     * Every iteration the actual loop time will be evaluated and used to calculate the time for the next loop.
     */
    @Override
	public void run() {
		long startTime = System.nanoTime();
		synchronized(impulseCount){
            impulseCount = 0;	//reset the impulse count
        }
        while(true) {
            sleepNano(nanoSecondsSleep);	//sleep for some time. This variable will be modified every loop to compensate for the loop time spent.
            int count = -1;
            synchronized(impulseCount){
                count = impulseCount;
                impulseCount = 0;
            }
            save(System.currentTimeMillis(), count);	//save the impulse count
            long estimatedNanoTimeSpent = System.nanoTime() - startTime;  //this is where the loop ends
            startTime = System.nanoTime();  //this is where the loop starts from now on
            if(estimatedNanoTimeSpent > 0){  //if no overflow
                long nanoSecondsTooMany = estimatedNanoTimeSpent - (REPORT_TIMEOUT*1000000L);
                //System.out.println("the look took ~" + estimatedNanoTimeSpent + "ns. That is " + nanoSecondsTooMany/1000000L + "ms off");
                nanoSecondsSleep -= nanoSecondsTooMany / 3;  //divide by constant to take into account varaiations im loop time
            }
        }
	}
    
    /**
     * Sleep for [ns] nanoseconds
     * @param ns
     */
    private void sleepNano(long ns){
        //System.out.println("will go to sleep for " + ns + "ns");
    	try{
    		Thread.sleep(ns/1000000L, (int)(ns%1000000L));
    	}catch(InterruptedException e){
    		//ignore
    	}
    }
    
    /**
     * Saves the data to the database.
     * This method should block the caller as short time as possible.
     * Try to make the time spent in the method the same for every call (low variation). 
     * 
     * @param timestamp_end
     * @param data
     */
    private void save(final long timestamp_end, final int data){
    	//offload the timed loop by not doing the db interaction in this thread.
    	executorPool.execute(new Runnable(){
			@Override
			public void run() {
                                logger.log(Level.INFO, "Saving data to DB. timestamp_end="+timestamp_end+", data="+data);
				try {
					db.exec("INSERT INTO sensor_data_raw(timestamp, sensor_id, data) VALUES("+timestamp_end+", "+RPiImpulseCountSensor.this.sensorId+", "+data+")");
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
    }
    
}
