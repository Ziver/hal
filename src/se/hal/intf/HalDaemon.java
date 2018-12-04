package se.hal.intf;

import java.util.concurrent.ScheduledExecutorService;

public interface HalDaemon extends Runnable{

    void initiate(ScheduledExecutorService executor);

}
