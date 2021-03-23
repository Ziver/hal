package se.hal.util;

import se.hal.daemon.SensorDataAggregatorDaemon.AggregationPeriodLength;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class UTCTimeUtility {
    public static final TimeZone TIMEZONE = TimeZone.getTimeZone("UTC");
    public static final Locale LOCALE = new Locale("sv","SE");

    public static final long SECOND_IN_MS = 1000;
    public static final long MINUTE_IN_MS = SECOND_IN_MS * 60;
    public static final long FIVE_MINUTES_IN_MS = MINUTE_IN_MS * 5;
    public static final long FIFTEEN_MINUTES_IN_MS = MINUTE_IN_MS * 15;
    public static final long HOUR_IN_MS = MINUTE_IN_MS * 60;
    public static final long DAY_IN_MS = HOUR_IN_MS * 24;
    public static final long WEEK_IN_MS = DAY_IN_MS * 7;
    public static final long INFINITY = Long.MAX_VALUE;	//sort of true

    public static long getTimestampPeriodStart(AggregationPeriodLength aggrPeriodLength, long timestamp) throws NumberFormatException{
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(timestamp);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        switch(aggrPeriodLength) {
            case YEAR:
                cal.set(Calendar.DAY_OF_YEAR, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case MONTH:
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case WEEK:
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case DAY:
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case HOUR:
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case FIVE_MINUTES:
                cal.set(Calendar.MINUTE, (cal.get(Calendar.MINUTE)/5)*5);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case FIFTEEN_MINUTES:
                cal.set(Calendar.MINUTE, (cal.get(Calendar.MINUTE)/15)*15);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case MINUTE:
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                break;
            case SECOND:
                cal.set(Calendar.MILLISECOND, 0);
                break;
        }
        return cal.getTimeInMillis();
    }

    public static long getTimestampPeriodEnd(AggregationPeriodLength aggrPeriodLength, long timestamp) throws NumberFormatException{
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(timestamp);
        cal.setFirstDayOfWeek(Calendar.MONDAY);
        switch(aggrPeriodLength) {
            case YEAR:
                cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 1000);
                break;
            case MONTH:
                cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 1000);
                break;
            case WEEK:
                cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 1000);
                break;
            case DAY:
                cal.set(Calendar.HOUR_OF_DAY, 23);
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 1000);
                break;
            case HOUR:
                cal.set(Calendar.MINUTE, 59);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 1000);
                break;
            case FIVE_MINUTES:
                cal.set(Calendar.MINUTE, 4+(cal.get(Calendar.MINUTE)/5)*5);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 1000);
                break;
            case FIFTEEN_MINUTES:
                cal.set(Calendar.MINUTE, 14+(cal.get(Calendar.MINUTE)/15)*15);
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 1000);
                break;
            case MINUTE:
                cal.set(Calendar.SECOND, 59);
                cal.set(Calendar.MILLISECOND, 1000);
                break;
            case SECOND:
                cal.set(Calendar.MILLISECOND, 1000);
                break;
        }
        return cal.getTimeInMillis()-1;	//subtract one
    }

    public static int getMillisecondInSecondFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.MILLISECOND);
    }

    public static int getSecondOfMinuteFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.SECOND);
    }

    public static int getMinuteOfHourFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.MINUTE);
    }

    public static int getHourOfDayFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.HOUR_OF_DAY);
    }

    public static int getDayOfWeekFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.DAY_OF_WEEK);
    }

    public static int getDayOfMonthFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.DAY_OF_MONTH);
    }

    public static int getDayOfYearFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.DAY_OF_YEAR);
    }

    public static int getWeekOfYearFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.WEEK_OF_YEAR);
    }

    public static int getMonthOfYearFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.MONTH);
    }

    public static int getYearFromTimestamp(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(ms);
        return cal.get(Calendar.YEAR);
    }

    public static String timeInMsToString(long ms) throws NumberFormatException{
        if (ms < 0)
            throw new NumberFormatException("argument must be positive");
        String retval = "";
        int weeks = (int) (ms / WEEK_IN_MS);
        if (weeks > 0) {
            retval += weeks + "w+";
        }
        int days = ((int) (ms / DAY_IN_MS)) % 7;
        if (days > 0) {
            retval += days + "d+";
        }
        int hours = (int) ((ms % DAY_IN_MS) / HOUR_IN_MS);
        retval += (hours<10?"0"+hours:hours);
        int minutes = (int) ((ms % HOUR_IN_MS) / MINUTE_IN_MS);
        retval += ":" + (minutes<10?"0"+minutes:minutes);
        int seconds = (int) ((ms % MINUTE_IN_MS) / SECOND_IN_MS);
        retval += ":" + (seconds<10?"0"+seconds:seconds);
        int milliseconds = (int) (ms % SECOND_IN_MS);
        retval += "." + (milliseconds<100?"0"+(milliseconds<10?"0"+milliseconds:milliseconds):milliseconds);
        return retval;
    }

    public static String getDateString(long timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        format.setTimeZone(TIMEZONE);
        Calendar cal = Calendar.getInstance(TIMEZONE, LOCALE);
        cal.setTimeInMillis(timestamp);
        return format.format(cal.getTime());
    }

}
