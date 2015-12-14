package se.koc.hal.deamon;

import java.util.Calendar;

public class TimeUtility {
	public static final long SECOND_IN_MS = 1000;
	public static final long MINUTES_IN_MS = SECOND_IN_MS * 60;
	public static final long FIVE_MINUTES_IN_MS = MINUTES_IN_MS * 5;
    public static final long HOUR_IN_MS = MINUTES_IN_MS * 60;
    public static final long DAY_IN_MS = HOUR_IN_MS * 24;
    public static final long WEEK_IN_MS = DAY_IN_MS * 7;
    
	public static long getTimestampPeriodStart(long periodLengthInMs, long timestamp){
		if(periodLengthInMs < DAY_IN_MS){
			long tmp = timestamp % periodLengthInMs;
			return timestamp - tmp;
		}else{
			long tmp = periodLengthInMs;
			int milliseconds = (int) (tmp % SECOND_IN_MS);
			tmp -= milliseconds * SECOND_IN_MS;
			int seconds = (int) ((tmp % MINUTES_IN_MS) / SECOND_IN_MS);
			tmp -= seconds * MINUTES_IN_MS;
			int minutes = (int) ((tmp % HOUR_IN_MS) / MINUTES_IN_MS);
			tmp -= minutes * HOUR_IN_MS;
			int hours = (int) ((tmp % DAY_IN_MS) / HOUR_IN_MS);
			tmp -= hours * DAY_IN_MS;
			int days = (int) (tmp / DAY_IN_MS);
			return getTimestampPeriodStart(days, hours, minutes, seconds, milliseconds, timestamp);
		}
	}

	private static long getTimestampPeriodStart(int days, int hours, int minutes, int seconds, int milliseconds, long timestamp) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		boolean clear = false;
		
		if(days > 0){
			int currentDay = cal.get(Calendar.DAY_OF_YEAR);
			cal.set(Calendar.DAY_OF_YEAR, (currentDay/days)*days);
			clear = true;
		}
		
		if(hours > 0){
			int currentHour = cal.get(Calendar.HOUR_OF_DAY);
			cal.set(Calendar.HOUR_OF_DAY, (currentHour/hours)*hours);
			clear = true;
		}else if(clear){
			cal.set(Calendar.HOUR_OF_DAY, 0);
		}
		
		if(minutes > 0){
			int currentMinute = cal.get(Calendar.MINUTE);
			cal.set(Calendar.MINUTE, (currentMinute/minutes)*minutes);
			clear = true;
		}else if(clear){
			cal.set(Calendar.MINUTE, 0);
		}
		
		if(seconds > 0){
			int currentSecond = cal.get(Calendar.SECOND);
			cal.set(Calendar.SECOND, (currentSecond/seconds)*seconds);
			clear = true;
		}else if(clear){
			cal.set(Calendar.SECOND, 0);
		}
		
		if(milliseconds > 0){
			int currentMillisecond = cal.get(Calendar.MILLISECOND);
			cal.set(Calendar.MILLISECOND, (currentMillisecond/milliseconds)*milliseconds);
		}else if(clear){
			cal.set(Calendar.MILLISECOND, 0);
		}
		
		return cal.getTimeInMillis();
	}
	
}
