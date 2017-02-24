package se.hal.trigger;

import se.hal.intf.HalTrigger;
import zutil.ui.Configurator;

/**
 *
 */
public class DateTimeTrigger implements HalTrigger {

    @Configurator.Configurable("Minute (Cron format)")
    private String minute = "";
    @Configurator.Configurable("Hour (Cron format)")
    private String hour = "";
    @Configurator.Configurable("Day of the Month (Cron format)")
    private String dayOfMonth = "";
    @Configurator.Configurable("Month of the Year (Cron format)")
    private String monthOfYear = "";
    @Configurator.Configurable("Day of the Week (Cron format)")
    private String dayOfWeek = "";
    @Configurator.Configurable("Year (Cron format)")
    private String year = "";


    @Override
    public boolean evaluate() {
        return false;
    }

    @Override
    public void reset() {

    }
}
