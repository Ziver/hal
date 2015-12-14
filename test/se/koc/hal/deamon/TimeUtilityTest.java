package se.koc.hal.deamon;

import static org.junit.Assert.*;

import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

public class TimeUtilityTest {
	private long currentTime;
	private Calendar referenceCalendar;
	
	@Before
	public void setup(){
		currentTime = System.currentTimeMillis();
		referenceCalendar = Calendar.getInstance();
		referenceCalendar.setTimeInMillis(currentTime);
	}
	
	@Test
	public void testDayStartForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart(TimeUtility.DAY_IN_MS, currentTime);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisPeriodStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", 0, testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", 0, testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
	}
	
	@Test
	public void testHourStartForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart(TimeUtility.HOUR_IN_MS, currentTime);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisPeriodStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", 0, testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", referenceCalendar.get(Calendar.HOUR_OF_DAY), testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
	}
	
	@Test
	public void testMinuteStartForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart(TimeUtility.MINUTES_IN_MS, currentTime);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisPeriodStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", referenceCalendar.get(Calendar.MINUTE), testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", referenceCalendar.get(Calendar.HOUR_OF_DAY), testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
	}
	
	@Test
	public void testSecondStartForCurrentTime(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart(TimeUtility.SECOND_IN_MS, currentTime);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisPeriodStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", referenceCalendar.get(Calendar.SECOND), testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", referenceCalendar.get(Calendar.MINUTE), testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", referenceCalendar.get(Calendar.HOUR_OF_DAY), testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
	}
	
	@Test 
	public void testMsToString(){
		//low values
		assertEquals("0days+00:00:00.000", TimeUtility.msToString(0));
		assertEquals("0days+00:00:00.001", TimeUtility.msToString(1));
		assertEquals("0days+00:00:01.000", TimeUtility.msToString(TimeUtility.SECOND_IN_MS));
		assertEquals("0days+00:01:00.000", TimeUtility.msToString(TimeUtility.MINUTES_IN_MS));
		assertEquals("0days+00:05:00.000", TimeUtility.msToString(TimeUtility.FIVE_MINUTES_IN_MS));
		assertEquals("0days+01:00:00.000", TimeUtility.msToString(TimeUtility.HOUR_IN_MS));
		assertEquals("1days+00:00:00.000", TimeUtility.msToString(TimeUtility.DAY_IN_MS));
		assertEquals("7days+00:00:00.000", TimeUtility.msToString(TimeUtility.WEEK_IN_MS));
		
		//high values
		assertEquals("0days+00:00:00.999", TimeUtility.msToString(999));
		assertEquals("0days+00:00:59.000", TimeUtility.msToString(TimeUtility.SECOND_IN_MS*59));
		assertEquals("0days+00:59:00.000", TimeUtility.msToString(TimeUtility.MINUTES_IN_MS*59));
		assertEquals("0days+23:00:00.000", TimeUtility.msToString(TimeUtility.HOUR_IN_MS*23));
		assertEquals("369days+00:00:00.000", TimeUtility.msToString(TimeUtility.DAY_IN_MS*369));
		
		//combinations
		long ms = (TimeUtility.DAY_IN_MS*999) + (TimeUtility.HOUR_IN_MS*23) + (TimeUtility.MINUTES_IN_MS*59) + (TimeUtility.SECOND_IN_MS*59) + 999;
		assertEquals("999days+23:59:59.999", TimeUtility.msToString(ms));
	}
	
}
