package se.hal.intf;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Defines a standalone process that will run parallel to the main application
 */
public interface HalDaemon extends Runnable {

    /**
     * Setup the execution of the daemon with the provided executor.
     *
     * @param executor the scheduler provided for the daemon to setup its execution.
     */
    void initiate(ScheduledExecutorService executor);

}
