package se.koc.hal.intf;

import java.util.Timer;

/**
 * Created by Ziver on 2015-12-03.
 */
public interface HalDaemon {
    public void initiate(Timer timer);

    public void run();
}
