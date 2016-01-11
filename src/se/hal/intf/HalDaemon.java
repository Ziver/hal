package se.hal.intf;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by Ziver on 2015-12-03.
 */
public interface HalDaemon extends Runnable{
    public void initiate(ScheduledExecutorService executor);

}
