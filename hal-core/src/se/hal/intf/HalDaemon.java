package se.hal.intf;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Defines a stand alone process that will run parallel to Hal
 */
public interface HalDaemon extends Runnable {

    /**
     * Setup the execution of the daemon with the provided executor.
     *
     * @param executor the scheduler provided by HAL for the daemon to setup its execution.
     */
    void initiate(ScheduledExecutorService executor);

}
