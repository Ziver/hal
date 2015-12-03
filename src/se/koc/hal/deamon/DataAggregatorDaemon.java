package se.koc.hal.deamon;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Ziver on 2015-12-03.
 */
public class DataAggregatorDaemon extends TimerTask implements HalDaemon {
    private static final int FIVE_MINUTES_IN_MS = 5 * 60 * 1000;


    public void initiate(Timer timer){
        timer.schedule(this, FIVE_MINUTES_IN_MS);
    }


    @Override
    public void run() {
    
    }
}
