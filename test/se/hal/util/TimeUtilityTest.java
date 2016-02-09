package se.hal.util;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TimeUtilityTest {
	private long currentTime_UTC;
	private Calendar referenceCalendar_LOCAL;
	
	@Before
	public void setup(){
		currentTime_UTC = System.currentTimeMillis();
		referenceCalendar_LOCAL = Calendar.getInstance();
		referenceCalendar_LOCAL.setTimeInMillis(currentTime_UTC);
	}
	
	// Test flooring LOCAL time to the closes day
	@Test
	public void testWeekStart_LOCAL_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_LOCAL(TimeUtility.WEEK_IN_MS, currentTime_UTC);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisPeriodStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", 0, testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", 0, testCalendar.get(Calendar.HOUR_OF_DAY));
		assertTrue("day is wrong", Math.abs(referenceCalendar_LOCAL.get(Calendar.DAY_OF_YEAR)-testCalendar.get(Calendar.DAY_OF_YEAR)) <= 6);	//day cannot differ more than 6 days
		assertEquals("week is wrong", referenceCalendar_LOCAL.get(Calendar.WEEK_OF_YEAR), testCalendar.get(Calendar.WEEK_OF_YEAR));
	}
	
	// Test flooring LOCAL time to the closes day
	@Test
	public void testDayStart_LOCAL_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_LOCAL(TimeUtility.DAY_IN_MS, currentTime_UTC);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisPeriodStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", 0, testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", 0, testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar_LOCAL.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
		assertEquals("week is wrong", referenceCalendar_LOCAL.get(Calendar.WEEK_OF_YEAR), testCalendar.get(Calendar.WEEK_OF_YEAR));
	}
	
	// Test flooring LOCAL time to the closes hour
	@Test
	public void testHourStart_LOCAL_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_LOCAL(TimeUtility.HOUR_IN_MS, currentTime_UTC);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisPeriodStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", 0, testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", referenceCalendar_LOCAL.get(Calendar.HOUR_OF_DAY), testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar_LOCAL.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
		assertEquals("week is wrong", referenceCalendar_LOCAL.get(Calendar.WEEK_OF_YEAR), testCalendar.get(Calendar.WEEK_OF_YEAR));
	}
	
	// Test flooring LOCAL time to the closes minute
	@Test
	public void testMinuteStart_LOCAL_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_LOCAL(TimeUtility.MINUTES_IN_MS, currentTime_UTC);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisPeriodStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", referenceCalendar_LOCAL.get(Calendar.MINUTE), testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", referenceCalendar_LOCAL.get(Calendar.HOUR_OF_DAY), testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar_LOCAL.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
		assertEquals("week is wrong", referenceCalendar_LOCAL.get(Calendar.WEEK_OF_YEAR), testCalendar.get(Calendar.WEEK_OF_YEAR));
	}
	
	// Test flooring LOCAL time to the closes second
	@Test
	public void testSecondStart_LOCAL_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_LOCAL(TimeUtility.SECOND_IN_MS, currentTime_UTC);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisPeriodStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", referenceCalendar_LOCAL.get(Calendar.SECOND), testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", referenceCalendar_LOCAL.get(Calendar.MINUTE), testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", referenceCalendar_LOCAL.get(Calendar.HOUR_OF_DAY), testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar_LOCAL.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
		assertEquals("week is wrong", referenceCalendar_LOCAL.get(Calendar.WEEK_OF_YEAR), testCalendar.get(Calendar.WEEK_OF_YEAR));
	}
	
	// Test flooring UTC time to the closes week
	@Test
	public void testWeekStart_UTC_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(TimeUtility.WEEK_IN_MS, currentTime_UTC);
		
		assertEquals("millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("minute is wrong", 0, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("hour is wrong", 0, TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertTrue("day is wrong", Math.abs(TimeUtility.getDaysFromTimestamp(currentTime_UTC)-TimeUtility.getDaysFromTimestamp(thisPeriodStartedAt)) <= 6);	//day cannot differ more than 6 days
		assertEquals("week is wrong", TimeUtility.getWeeksFromTimestamp(currentTime_UTC), TimeUtility.getWeeksFromTimestamp(thisPeriodStartedAt));
	}	
	
	// Test flooring UTC time to the closes day
	@Test
	public void testDayStart_UTC_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(TimeUtility.DAY_IN_MS, currentTime_UTC);
		
		assertEquals("millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("minute is wrong", 0, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("hour is wrong", 0, TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("day is wrong", TimeUtility.getDaysFromTimestamp(currentTime_UTC), TimeUtility.getDaysFromTimestamp(thisPeriodStartedAt));
		assertEquals("week is wrong", TimeUtility.getWeeksFromTimestamp(currentTime_UTC), TimeUtility.getWeeksFromTimestamp(thisPeriodStartedAt));
	}
	
	// Test flooring UTC time to the closes hour
	@Test
	public void testHourStart_UTC_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(TimeUtility.HOUR_IN_MS, currentTime_UTC);
		
		assertEquals("millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("minute is wrong", 0, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("day is wrong", TimeUtility.getDaysFromTimestamp(currentTime_UTC), TimeUtility.getDaysFromTimestamp(thisPeriodStartedAt));
		assertEquals("week is wrong", TimeUtility.getWeeksFromTimestamp(currentTime_UTC), TimeUtility.getWeeksFromTimestamp(thisPeriodStartedAt));
	}
	
	// Test flooring UTC time to the closes minute
	@Test
	public void testMinuteStart_UTC_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(TimeUtility.MINUTES_IN_MS, currentTime_UTC);
		
		assertEquals("millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("minute is wrong", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("day is wrong", TimeUtility.getDaysFromTimestamp(currentTime_UTC), TimeUtility.getDaysFromTimestamp(thisPeriodStartedAt));
		assertEquals("week is wrong", TimeUtility.getWeeksFromTimestamp(currentTime_UTC), TimeUtility.getWeeksFromTimestamp(thisPeriodStartedAt));
	}
	
	// Test flooring UTC time to the closes second
	@Test
	public void testSecondStart_UTC_ForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(TimeUtility.SECOND_IN_MS, currentTime_UTC);
		
		assertEquals("millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("second is wrong", TimeUtility.getSecondOfMinuteFromTimestamp(currentTime_UTC), TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("minute is wrong", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("day is wrong", TimeUtility.getDaysFromTimestamp(currentTime_UTC), TimeUtility.getDaysFromTimestamp(thisPeriodStartedAt));
		assertEquals("week is wrong", TimeUtility.getWeeksFromTimestamp(currentTime_UTC), TimeUtility.getWeeksFromTimestamp(thisPeriodStartedAt));
	}
	
	// Test printing converting milliseconds to text
	@Test 
	public void testMsToString(){
		//low values
		assertEquals("00:00:00.000", TimeUtility.msToString(0));
		assertEquals("00:00:00.001", TimeUtility.msToString(1));
		assertEquals("00:00:01.000", TimeUtility.msToString(TimeUtility.SECOND_IN_MS));
		assertEquals("00:01:00.000", TimeUtility.msToString(TimeUtility.MINUTES_IN_MS));
		assertEquals("00:05:00.000", TimeUtility.msToString(TimeUtility.FIVE_MINUTES_IN_MS));
		assertEquals("01:00:00.000", TimeUtility.msToString(TimeUtility.HOUR_IN_MS));
		assertEquals("1d+00:00:00.000", TimeUtility.msToString(TimeUtility.DAY_IN_MS));
		assertEquals("1w+00:00:00.000", TimeUtility.msToString(TimeUtility.WEEK_IN_MS));

		//high values
		assertEquals("00:00:00.999", TimeUtility.msToString(999));
		assertEquals("00:00:59.000", TimeUtility.msToString(TimeUtility.SECOND_IN_MS*59));
		assertEquals("00:59:00.000", TimeUtility.msToString(TimeUtility.MINUTES_IN_MS*59));
		assertEquals("23:00:00.000", TimeUtility.msToString(TimeUtility.HOUR_IN_MS*23));
		assertEquals("52w+5d+00:00:00.000", TimeUtility.msToString(TimeUtility.DAY_IN_MS*369));
		
		//high overflow values
		assertEquals("00:00:01.999", TimeUtility.msToString(1999));
		assertEquals("00:02:39.000", TimeUtility.msToString(TimeUtility.SECOND_IN_MS*159));
		assertEquals("02:39:00.000", TimeUtility.msToString(TimeUtility.MINUTES_IN_MS*159));
		assertEquals("5d+03:00:00.000", TimeUtility.msToString(TimeUtility.HOUR_IN_MS*123));
		
		//combinations
		assertEquals("142w+5d+23:59:59.999", TimeUtility.msToString((TimeUtility.WEEK_IN_MS*142) + (TimeUtility.DAY_IN_MS*5) + (TimeUtility.HOUR_IN_MS*23) + (TimeUtility.MINUTES_IN_MS*59) + (TimeUtility.SECOND_IN_MS*59) + 999));
		assertEquals("6d+23:59:59.999", TimeUtility.msToString(TimeUtility.WEEK_IN_MS-1));
	}
	
	// Test printing converting milliseconds to text for a negative time
	@Test(expected=NumberFormatException.class)
	public void testMsToStringForNegativeArgument(){
		//low values
		TimeUtility.msToString(-1);
	}
	
}
