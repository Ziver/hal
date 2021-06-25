package se.hal.plugin.raspberry.hardware;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.wiringpi.GpioUtil;
import se.hal.plugin.raspberry.RPiController;
import se.hal.plugin.raspberry.device.RPiPowerConsumptionSensor;
import se.hal.plugin.raspberry.RPiSensor;
import se.hal.plugin.raspberry.RPiUtility;
import se.hal.struct.devicedata.PowerConsumptionSensorData;
import zutil.log.LogUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RPiInteruptPulseFlankCounter implements Runnable, GpioPinListenerDigital, RPiSensor {
    private static final int REPORT_TIMEOUT = 60_000;   //one minute

    private static final Logger logger = LogUtil.getLogger();

    private RPiController controller;
    private ExecutorService executorPool;
    private long nanoSecondsSleep = REPORT_TIMEOUT * 1_000_000L;
    private volatile Integer impulseCount = 0;
    private GpioPinDigitalInput irLightSensor;
    private final int gpioPin;

    public RPiInteruptPulseFlankCounter(int gpioPin, RPiController controller) {
        this.controller = controller;
        this.gpioPin = gpioPin;

        // setup a thread pool for executing jobs
        this.executorPool = Executors.newCachedThreadPool();

    //Enable non privileged access to the GPIO pins (no sudo required from now)
    GpioUtil.enableNonPrivilegedAccess();

        // create gpio controller
        GpioController gpio = null;
        try{
            gpio = GpioFactory.getInstance();
        }catch(IllegalArgumentException e) {
            logger.log(Level.SEVERE, "", e);
            throw e;
        }catch(UnsatisfiedLinkError e) {
            logger.log(Level.SEVERE, "", e);
            throw e;
        }

        // provision gpio pin as an input pin with its internal pull up resistor enabled
        irLightSensor = gpio.provisionDigitalInputPin(RPiUtility.getPin(gpioPin), PinPullResistance.PULL_UP);

        // create and register gpio pin listener. May require the program to be run as sudo if the GPIO pin has not been exported
        irLightSensor.addListener(this);

        //start a daemon thread to save the impulse count every minute
        Thread thread = new Thread(this);
        thread.setDaemon(false);
        thread.start();
    }

    public void close() {
        irLightSensor.removeListener(this);
        executorPool.shutdown();
    }

    /**
     * GpioPinListenerDigital interface
     */
    @Override
    public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
        if (event.getState() == PinState.LOW) {  //low = light went on
            //System.out.println("IR LED turned ON");
            //logger.log(Level.INFO, "IR LED turned on");
            synchronized(impulseCount) {
                impulseCount++;
            }
        }
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();
        synchronized(impulseCount) {
            impulseCount = 0;	//reset the impulse count
        }
        while (!executorPool.isShutdown()) {
            sleepNano(nanoSecondsSleep);	//sleep for some time. This variable will be modified every loop to compensate for the loop time spent.
            int count = -1;
            synchronized(impulseCount) {
                count = impulseCount;
                impulseCount = 0;
            }
            save(System.currentTimeMillis(), count);	//save the impulse count
            long estimatedNanoTimeSpent = System.nanoTime() - startTime;  //this is where the loop ends
            startTime = System.nanoTime();  //this is where the loop starts from now on
            if (estimatedNanoTimeSpent > 0) {  //if no overflow
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
    private void sleepNano(long ns) {
        //System.out.println("will go to sleep for " + ns + "ns");
        try{
            Thread.sleep(ns/1000000L, (int)(ns%1000000L));
        }catch(InterruptedException e) {
            //ignore
        }
    }

    /**
     * Saves the data to the database.
     * This method should block the caller as short time as possible.
     * This method should try block the same amount of time every time it is called.
     * Try to make the time spent in the method the same for every call (low variation).
     *
     * @param timestamp_end
     * @param data
     */
    private void save(final long timestamp_end, final int data) {
        //offload the timed loop by not doing the db interaction in this thread.
        executorPool.execute(new Runnable() {
            @Override
            public void run() {
                logger.log(Level.INFO, "Reporting data. timestamp_end="+timestamp_end+", data="+data);
                controller.sendDataReport(
                        new RPiPowerConsumptionSensor(gpioPin),
                        new PowerConsumptionSensorData(
                                timestamp_end, data
                        ));
            }
        });
    }

}
