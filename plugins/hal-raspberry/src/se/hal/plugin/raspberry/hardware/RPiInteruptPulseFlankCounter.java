/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

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

        // Setup a thread pool for executing jobs
        this.executorPool = Executors.newCachedThreadPool();

        //Enable non privileged access to the GPIO pins (no sudo required from now)
        GpioUtil.enableNonPrivilegedAccess();

        // create gpio controller
        GpioController gpio = null;
        try {
            gpio = GpioFactory.getInstance();
        } catch(IllegalArgumentException e) {
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
            //logger.log(Level.INFO, "IR LED turned on");
            synchronized(this) {
                impulseCount++;
            }
        }
    }

    @Override
    public void run() {
        long startTime = System.nanoTime();

        synchronized(this) {
            impulseCount = 0;	//reset the impulse count
        }

        while (!executorPool.isShutdown()) {
            sleepNano(nanoSecondsSleep);	//sleep for some time. This variable will be modified every loop to compensate for the loop time spent.
            int count = -1;

            synchronized(this) {
                count = impulseCount;
                impulseCount = 0;
            }

            report(count, System.currentTimeMillis());	//save the impulse count
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
     *
     * @param ns    nanoseconds to sleep
     */
    private void sleepNano(long ns) {
        try{
            Thread.sleep(ns/1000000L, (int)(ns%1000000L));
        }catch(InterruptedException e) {
            //ignore
        }
    }

    /**
     * Report back some data to Hal Core,
     *
     * @param data      The data to report back to Hal.
     * @param timestamp The timestamp of the received data
     */
    private void report(final int data, final long timestamp) {
        // Offload the timed loop by not doing the db interaction in this thread.
        executorPool.execute(new Runnable() {
            @Override
            public void run() {
                logger.log(Level.INFO, "Reporting data. timestamp_end=" + timestamp + ", data=" + data);
                controller.sendDataReport(
                    new RPiPowerConsumptionSensor(gpioPin),
                    new PowerConsumptionSensorData(data, timestamp)
                );
            }
        });
    }

}
