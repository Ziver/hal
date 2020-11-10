package se.hal.intf;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Defines a stand alone process that will run parallel to Hal
 */
public interface HalDaemon extends Runnable{

    /**
     * Initialize the daemon.
     *
     * @param executor The sceduler that the daemon should register to.
     */
    void initiate(ScheduledExecutorService executor);

}
