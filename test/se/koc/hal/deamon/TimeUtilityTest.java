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
		long thisDayStartedAt = TimeUtility.getTimestampPeriodStart(TimeUtility.DAY_IN_MS, currentTime);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisDayStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", 0, testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", 0, testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
	}
	
	@Test
	public void testHourStartForCurrentTime(){
		long thisDayStartedAt = TimeUtility.getTimestampPeriodStart(TimeUtility.HOUR_IN_MS, currentTime);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisDayStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", 0, testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", referenceCalendar.get(Calendar.HOUR_OF_DAY), testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
	}
	
	@Test
	public void testMinuteStartForCurrentTime(){
		long thisDayStartedAt = TimeUtility.getTimestampPeriodStart(TimeUtility.MINUTES_IN_MS, currentTime);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisDayStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", 0, testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", referenceCalendar.get(Calendar.MINUTE), testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", referenceCalendar.get(Calendar.HOUR_OF_DAY), testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
	}
	
	@Test
	public void testSecondStartForCurrentTime(){
		long thisDayStartedAt = TimeUtility.getTimestampPeriodStart(TimeUtility.SECOND_IN_MS, currentTime);
		Calendar testCalendar = Calendar.getInstance();
		testCalendar.setTimeInMillis(thisDayStartedAt);
		
		assertEquals("millisecond is wrong", 0, testCalendar.get(Calendar.MILLISECOND));
		assertEquals("second is wrong", referenceCalendar.get(Calendar.SECOND), testCalendar.get(Calendar.SECOND));
		assertEquals("minute is wrong", referenceCalendar.get(Calendar.MINUTE), testCalendar.get(Calendar.MINUTE));
		assertEquals("hour is wrong", referenceCalendar.get(Calendar.HOUR_OF_DAY), testCalendar.get(Calendar.HOUR_OF_DAY));
		assertEquals("day is wrong", referenceCalendar.get(Calendar.DAY_OF_YEAR), testCalendar.get(Calendar.DAY_OF_YEAR));
	}
	
}
