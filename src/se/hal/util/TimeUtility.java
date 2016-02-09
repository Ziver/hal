package se.hal.util;

import java.util.Calendar;
import java.util.TimeZone;

public class TimeUtility {
	public static final long SECOND_IN_MS = 1000;
	public static final long MINUTES_IN_MS = SECOND_IN_MS * 60;
	public static final long FIVE_MINUTES_IN_MS = MINUTES_IN_MS * 5;
    public static final long HOUR_IN_MS = MINUTES_IN_MS * 60;
    public static final long DAY_IN_MS = HOUR_IN_MS * 24;
    public static final long WEEK_IN_MS = DAY_IN_MS * 7;
	public static final long INFINITY = Long.MAX_VALUE;	//sort of true
    
    public static long getTimestampPeriodStart_UTC(long periodLengthInMs, long timestamp) throws NumberFormatException{
    	//return timestamp - (timestamp % periodLengthInMs);
		return getTimestampPeriodStart(periodLengthInMs, timestamp, Calendar.getInstance(TimeZone.getTimeZone("UTC")));
    }
    
    /**
     * Get the timstamp for the given timestamp floored with the period length. The result should point to the beginning of the timestamps period.
     * @param periodLengthInMs The periods length to floor the timestamp with
     * @param timestamp The timestamp to floor.
     * @return
     */
	public static long getTimestampPeriodStart_LOCAL(long periodLengthInMs, long timestamp) throws NumberFormatException{
		return getTimestampPeriodStart(periodLengthInMs, timestamp, Calendar.getInstance());
	}
	
	private static long getTimestampPeriodStart(long periodLengthInMs, long timestamp, Calendar cal) throws NumberFormatException{
		if(periodLengthInMs < 0 || timestamp < 0)
    		throw new NumberFormatException("argument must be positive");
		
		cal.setTimeInMillis(timestamp);
		boolean clear = false;
		
		int weeks = getWeeksFromTimestamp(periodLengthInMs);
		if(weeks > 0){
			int currentWeek = cal.get(Calendar.WEEK_OF_YEAR);
			cal.set(Calendar.WEEK_OF_YEAR, (currentWeek/weeks)*weeks);
			clear = true;
		}
		
		int days = getDaysFromTimestamp(periodLengthInMs);
		if(days%7 > 0){
			int currentDay = cal.get(Calendar.DAY_OF_YEAR);
			cal.set(Calendar.DAY_OF_YEAR, (currentDay/days)*days);
			clear = true;
		}else if(clear){
			cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
		}
		
		int hours = getHourOfDayFromTimestamp(periodLengthInMs);
		if(hours > 0){
			int currentHour = cal.get(Calendar.HOUR_OF_DAY);
			cal.set(Calendar.HOUR_OF_DAY, (currentHour/hours)*hours);
			clear = true;
		}else if(clear){
			cal.set(Calendar.HOUR_OF_DAY, 0);
		}
		
		int minutes = getMinuteOfHourFromTimestamp(periodLengthInMs);
		if(minutes > 0){
			int currentMinute = cal.get(Calendar.MINUTE);
			cal.set(Calendar.MINUTE, (currentMinute/minutes)*minutes);
			clear = true;
		}else if(clear){
			cal.set(Calendar.MINUTE, 0);
		}
		
		int seconds = getSecondOfMinuteFromTimestamp(periodLengthInMs);
		if(seconds > 0){
			int currentSecond = cal.get(Calendar.SECOND);
			cal.set(Calendar.SECOND, (currentSecond/seconds)*seconds);
			clear = true;
		}else if(clear){
			cal.set(Calendar.SECOND, 0);
		}
		
		int milliseconds = getMillisecondInSecondFromTimestamp(periodLengthInMs);
		if(milliseconds > 0){
			int currentMillisecond = cal.get(Calendar.MILLISECOND);
			cal.set(Calendar.MILLISECOND, (currentMillisecond/milliseconds)*milliseconds);
		}else if(clear){
			cal.set(Calendar.MILLISECOND, 0);
		}
		
		return cal.getTimeInMillis();
	}
	
	public static int getMillisecondInSecondFromTimestamp(long ms) throws NumberFormatException{
		if(ms < 0)
    		throw new NumberFormatException("argument must be positive");
		return (int) (ms % SECOND_IN_MS);
	}
	
	public static int getSecondOfMinuteFromTimestamp(long ms) throws NumberFormatException{
		if(ms < 0)
    		throw new NumberFormatException("argument must be positive");
		return (int) ((ms % MINUTES_IN_MS) / SECOND_IN_MS);
	}
	
	public static int getMinuteOfHourFromTimestamp(long ms) throws NumberFormatException{
		if(ms < 0)
    		throw new NumberFormatException("argument must be positive");
		return (int) ((ms % HOUR_IN_MS) / MINUTES_IN_MS);
	}
	
	public static int getHourOfDayFromTimestamp(long ms) throws NumberFormatException{
		if(ms < 0)
    		throw new NumberFormatException("argument must be positive");
		return (int) ((ms % DAY_IN_MS) / HOUR_IN_MS);
	}
	
	public static int getDaysFromTimestamp(long ms) throws NumberFormatException{
		if(ms < 0)
    		throw new NumberFormatException("argument must be positive");
		return (int) (ms / DAY_IN_MS);
	}
	
	public static int getWeeksFromTimestamp(long ms) throws NumberFormatException{
		if(ms < 0)
    		throw new NumberFormatException("argument must be positive");
		return (int) (ms / WEEK_IN_MS);
	}
	
	public static String msToString(long ms) throws NumberFormatException{
		if(ms < 0)
    		throw new NumberFormatException("argument must be positive");
		String retval = "";
		int weeks = getWeeksFromTimestamp(ms);
		if(weeks > 0){
			retval += weeks + "w+";
		}
		int days = getDaysFromTimestamp(ms) % 7;
		if(days > 0){
			retval += days + "d+";
		}
		int hours = getHourOfDayFromTimestamp(ms);
		retval += (hours<10?"0"+hours:hours);
		int minutes = getMinuteOfHourFromTimestamp(ms);
		retval += ":" + (minutes<10?"0"+minutes:minutes);
		int seconds = getSecondOfMinuteFromTimestamp(ms);
		retval += ":" + (seconds<10?"0"+seconds:seconds);
		int milliseconds = getMillisecondInSecondFromTimestamp(ms);
		retval += "." + (milliseconds<100?"0"+(milliseconds<10?"0"+milliseconds:milliseconds):milliseconds);
		return retval;
	}
	
}
