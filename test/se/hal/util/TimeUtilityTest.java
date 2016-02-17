package se.hal.util;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.hal.deamon.SensorDataAggregatorDaemon.AggregationPeriodLength;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TimeUtilityTest {
	private final long currentTime_UTC;
	
	
	/**
	 * Constructor
	 * @param timestamp
	 */
	public TimeUtilityTest(long timestamp){
		this.currentTime_UTC = timestamp;
	}
	
	@Parameters
	public static Collection<Object[]> data(){
		return Arrays.asList(new Object[][] {
			{TimeUtility.WEEK_IN_MS},									//a week after 1970-01-01 00:00:00
			{694223999999L},											//1991-12-31 23:59:59.999 GMT	(last ms of year 1991)
			{694224000000L},											//1992-01-01 00:00:00.000 GMT   (first ms of year 1992)
			{1456703999999L},											//2016-02-28 23:59:59.999 GMT	(last ms before a leap day)
			{1456704000000L},											//2016-02-29 00:00:00.000 GMT	(first ms of a leap day)
			{1456749808000L},											//2016-02-29 12:43:28.000 GMT	(random time during a leap day)
			{1456790399999L},											//2016-02-29 23:59:59.999 GMT	(last ms of a leap day)
			{1456790400000L},											//2016-03-30 00:00:00.000 GMT	(first ms after a leap day)
			{System.currentTimeMillis()},								//current time
			{System.currentTimeMillis()+TimeUtility.MINUTE_IN_MS},		//current time + 1m
			{System.currentTimeMillis()+(2*TimeUtility.MINUTE_IN_MS)},	//current time + 2m
			{System.currentTimeMillis()+(3*TimeUtility.MINUTE_IN_MS)},	//current time + 3m
			{System.currentTimeMillis()+(4*TimeUtility.MINUTE_IN_MS)},	//current time + 4m
			{System.currentTimeMillis()+(5*TimeUtility.MINUTE_IN_MS)},	//current time + 5m
			{System.currentTimeMillis()+(6*TimeUtility.MINUTE_IN_MS)},	//current time + 6m
			{System.currentTimeMillis()+(7*TimeUtility.MINUTE_IN_MS)},	//current time + 7m
			{Long.MAX_VALUE-(60*TimeUtility.WEEK_IN_MS)},				//max time - 60w
		});
	}
	
	@Before
	public void printCurrentTimeStamp(){
		System.out.println("Testing with timestamp: " + currentTime_UTC);
	}
	
	// Test flooring & ceiling UTC time to the closes year
	@Test
	public void testYear_UTC(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(AggregationPeriodLength.YEAR, currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", 0, TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", 1, TimeUtility.getDayOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start month is wrong", Calendar.JANUARY, TimeUtility.getMonthOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodStartedAt));
		
		long thisPeriodEndedAt = TimeUtility.getTimestampPeriodEnd_UTC(AggregationPeriodLength.YEAR, currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", 23, TimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", 31, TimeUtility.getDayOfMonthFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end month is wrong", Calendar.DECEMBER, TimeUtility.getMonthOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodEndedAt));
	}	
	
	// Test flooring & ceiling UTC time to the closes month
	@Test
	public void testMonth_UTC(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(AggregationPeriodLength.MONTH, currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", 0, TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", 1, TimeUtility.getDayOfMonthFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start month is wrong", TimeUtility.getMonthOfYearFromTimestamp(currentTime_UTC), TimeUtility.getMonthOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		long thisPeriodEndedAt = TimeUtility.getTimestampPeriodEnd_UTC(AggregationPeriodLength.MONTH, currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", 23, TimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertTrue("day of month is less than 28", TimeUtility.getDayOfMonthFromTimestamp(thisPeriodEndedAt) >= 28);
		assertEquals("period end month is wrong", TimeUtility.getMonthOfYearFromTimestamp(currentTime_UTC), TimeUtility.getMonthOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodEndedAt));
	}	
		
	// Test flooring & ceiling UTC time to the closes week
	@Test
	public void testWeek_UTC(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(AggregationPeriodLength.WEEK, currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", 0, TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", Calendar.MONDAY, TimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertTrue("period start year is more than one year off", TimeUtility.getYearFromTimestamp(currentTime_UTC)-TimeUtility.getYearFromTimestamp(thisPeriodStartedAt) <= 1);

		long thisPeriodEndedAt = TimeUtility.getTimestampPeriodEnd_UTC(AggregationPeriodLength.WEEK, currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", 23, TimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", Calendar.SUNDAY, TimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertTrue("period end year is more than one year off", TimeUtility.getYearFromTimestamp(thisPeriodEndedAt)-TimeUtility.getYearFromTimestamp(currentTime_UTC) <= 1);
	}	
	
	// Test flooring & ceiling UTC time to the closes day
	@Test
	public void testDay_UTC(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(AggregationPeriodLength.DAY, currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", 0, TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", TimeUtility.getDayOfYearFromTimestamp(currentTime_UTC), TimeUtility.getDayOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodStartedAt));
		
		long thisPeriodEndedAt = TimeUtility.getTimestampPeriodEnd_UTC(AggregationPeriodLength.DAY, currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", 23, TimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", TimeUtility.getDayOfYearFromTimestamp(currentTime_UTC), TimeUtility.getDayOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end month is wrong", TimeUtility.getMonthOfYearFromTimestamp(currentTime_UTC), TimeUtility.getMonthOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodEndedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes hour
	@Test
	public void testHour_UTC(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(AggregationPeriodLength.HOUR, currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", TimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), TimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodStartedAt));
		
		long thisPeriodEndedAt = TimeUtility.getTimestampPeriodEnd_UTC(AggregationPeriodLength.HOUR, currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", TimeUtility.getDayOfYearFromTimestamp(currentTime_UTC), TimeUtility.getDayOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end month is wrong", TimeUtility.getMonthOfYearFromTimestamp(currentTime_UTC), TimeUtility.getMonthOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodEndedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes 15-minute period
	@Test
	public void testFifteenMinute_UTC(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(AggregationPeriodLength.FIFTEEN_MINUTES, currentTime_UTC);		
		assertEquals("period start millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertTrue("the period start minute is in the future of the current time", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) >= TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertTrue("the period starts more than 5 minutes before the current time", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC)-TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt) <= 14);
		assertTrue("the period start minute is not a multiple of five", TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt) % 15 == 0);
		assertEquals("period start hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", TimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), TimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		long thisPeriodEndedAt = TimeUtility.getTimestampPeriodEnd_UTC(AggregationPeriodLength.FIFTEEN_MINUTES, currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertTrue("the period end minute is before of the current time", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) <= TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertTrue("the period ends more than 15 minutes after the current time", TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt)-TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) <= 14);
		assertTrue("the period end minute(+1) is not a multiple of fifteen", TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt+1) % 15 == 0);
		assertEquals("period end hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", TimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), TimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodEndedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes 5-minute period
	@Test
	public void testFiveMinute_UTC(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(AggregationPeriodLength.FIVE_MINUTES, currentTime_UTC);		
		assertEquals("period start millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertTrue("the period start minute is in the future of the current time", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) >= TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertTrue("the period starts more than 5 minutes before the current time", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC)-TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt) <= 4);
		assertTrue("the period start minute is not a multiple of five", TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt) % 5 == 0);
		assertEquals("period start hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", TimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), TimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		long thisPeriodEndedAt = TimeUtility.getTimestampPeriodEnd_UTC(AggregationPeriodLength.FIVE_MINUTES, currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertTrue("the period end minute is before of the current time", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) <= TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertTrue("the period ends more than 5 minutes after the current time", TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt)-TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) <= 4);
		assertTrue("the period end minute(+1) is not a multiple of five", TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt+1) % 5 == 0);
		assertEquals("period end hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", TimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), TimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodEndedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes minute
	@Test
	public void testMinute_UTC(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(AggregationPeriodLength.MINUTE, currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", TimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), TimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		long thisPeriodEndedAt = TimeUtility.getTimestampPeriodEnd_UTC(AggregationPeriodLength.MINUTE, currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", TimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), TimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodEndedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes second
	@Test
	public void testSecond_UTC(){
		long thisPeriodStartedAt = TimeUtility.getTimestampPeriodStart_UTC(AggregationPeriodLength.SECOND, currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", TimeUtility.getSecondOfMinuteFromTimestamp(currentTime_UTC), TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", TimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), TimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		long thisPeriodEndedAt = TimeUtility.getTimestampPeriodEnd_UTC(AggregationPeriodLength.SECOND, currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, TimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", TimeUtility.getSecondOfMinuteFromTimestamp(currentTime_UTC), TimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", TimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), TimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", TimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), TimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", TimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), TimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", TimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), TimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", TimeUtility.getYearFromTimestamp(currentTime_UTC), TimeUtility.getYearFromTimestamp(thisPeriodEndedAt));
	}
	
	// Test printing converting milliseconds to text
	@Test 
	public void testMsToString(){
		//low values
		assertEquals("00:00:00.000", TimeUtility.timeInMsToString(0));
		assertEquals("00:00:00.001", TimeUtility.timeInMsToString(1));
		assertEquals("00:00:01.000", TimeUtility.timeInMsToString(TimeUtility.SECOND_IN_MS));
		assertEquals("00:01:00.000", TimeUtility.timeInMsToString(TimeUtility.MINUTE_IN_MS));
		assertEquals("00:05:00.000", TimeUtility.timeInMsToString(TimeUtility.FIVE_MINUTES_IN_MS));
		assertEquals("01:00:00.000", TimeUtility.timeInMsToString(TimeUtility.HOUR_IN_MS));
		assertEquals("1d+00:00:00.000", TimeUtility.timeInMsToString(TimeUtility.DAY_IN_MS));
		assertEquals("1w+00:00:00.000", TimeUtility.timeInMsToString(TimeUtility.WEEK_IN_MS));

		//high values
		assertEquals("00:00:00.999", TimeUtility.timeInMsToString(999));
		assertEquals("00:00:59.000", TimeUtility.timeInMsToString(TimeUtility.SECOND_IN_MS*59));
		assertEquals("00:59:00.000", TimeUtility.timeInMsToString(TimeUtility.MINUTE_IN_MS*59));
		assertEquals("23:00:00.000", TimeUtility.timeInMsToString(TimeUtility.HOUR_IN_MS*23));
		assertEquals("52w+5d+00:00:00.000", TimeUtility.timeInMsToString(TimeUtility.DAY_IN_MS*369));
		
		//high overflow values
		assertEquals("00:00:01.999", TimeUtility.timeInMsToString(1999));
		assertEquals("00:02:39.000", TimeUtility.timeInMsToString(TimeUtility.SECOND_IN_MS*159));
		assertEquals("02:39:00.000", TimeUtility.timeInMsToString(TimeUtility.MINUTE_IN_MS*159));
		assertEquals("5d+03:00:00.000", TimeUtility.timeInMsToString(TimeUtility.HOUR_IN_MS*123));
		
		//combinations
		assertEquals("142w+5d+23:59:59.999", TimeUtility.timeInMsToString((TimeUtility.WEEK_IN_MS*142) + (TimeUtility.DAY_IN_MS*5) + (TimeUtility.HOUR_IN_MS*23) + (TimeUtility.MINUTE_IN_MS*59) + (TimeUtility.SECOND_IN_MS*59) + 999));
		assertEquals("6d+23:59:59.999", TimeUtility.timeInMsToString(TimeUtility.WEEK_IN_MS-1));
	}
	
	// Test printing converting milliseconds to text for a negative time
	@Test(expected=NumberFormatException.class)
	public void testMsToStringForNegativeArgument(){
		//low values
		TimeUtility.timeInMsToString(-1);
	}
	
}
