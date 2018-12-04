package se.hal.trigger;

import se.hal.intf.HalTrigger;
import zutil.Timer;
import zutil.ui.Configurator;

public class TimerTrigger implements HalTrigger {

    @Configurator.Configurable("Countdown time (in seconds)")
    private int timerTime = 10; // default 10s
    private Timer timer;


    @Override
    public boolean evaluate() {
        return timer == null || timer.hasTimedOut();
    }

    @Override
    public void reset() {
        timer = new Timer(timerTime * 1000).start();
    }

    public String toString(){
        return "Timer: "+ timerTime +"s";
    }
}
