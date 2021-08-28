package se.hal.trigger;

import se.hal.intf.HalTrigger;
import zutil.CronTimer;
import zutil.ObjectUtil;
import zutil.ui.conf.Configurator;

import java.text.SimpleDateFormat;


public class DateTimeTrigger implements HalTrigger,Configurator.PostConfigurationActionListener {

    @Configurator.Configurable("Minute (Cron format)")
    private String minute = "00";
    @Configurator.Configurable("Hour (Cron format)")
    private String hour = "12";
    @Configurator.Configurable("Day of the Month (Cron format)")
    private String dayOfMonth = "*";
    @Configurator.Configurable("Month (1-12 or Cron format)")
    private String month = "*";
    @Configurator.Configurable("Day of the Week (1-7 or Cron format)")
    private String dayOfWeek = "*";
    @Configurator.Configurable("Year (Cron format)")
    private String year = "*";

    private transient CronTimer cronTimer;
    private transient long timeOut = -1;


    @Override
    public void postConfigurationAction(Configurator configurator, Object obj) {
        if (ObjectUtil.isEmpty(minute))         minute = "*";
        if (ObjectUtil.isEmpty(hour))             hour = "*";
        if (ObjectUtil.isEmpty(dayOfMonth)) dayOfMonth = "*";
        if (ObjectUtil.isEmpty(month))           month = "*";
        if (ObjectUtil.isEmpty(dayOfWeek))   dayOfWeek = "*";
        if (ObjectUtil.isEmpty(year))             year = "*";

        cronTimer = new CronTimer(minute, hour, dayOfMonth, month, dayOfWeek, year);
        reset();
    }

    @Override
    public boolean evaluate() {
        if (cronTimer == null)
            return false;
        // have we passed the majority of the minute? then get next timeout
        if (System.currentTimeMillis() - timeOut > 50*1000)
            reset();
        return timeOut <= System.currentTimeMillis();
    }

    @Override
    public void reset() {
        if (cronTimer != null)
            timeOut = cronTimer.next();
    }

    public String toString(){
        return //"Cron: \""+minute+" "+hour+" "+dayOfMonth+" "+month+" "+dayOfWeek+" "+year+"\" "+
                "Next timeout: "+
                (timeOut>0 ? new SimpleDateFormat("yyyy-MM-dd HH:mm").format(timeOut) : timeOut);
    }

}
