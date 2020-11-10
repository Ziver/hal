package se.hal.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.hal.daemon.SensorDataAggregatorDaemon.AggregationPeriodLength;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.TimeZone;

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
			{60*UTCTimeUtility.WEEK_IN_MS},								//min time + 60w
			{694223999999L},											//1991-12-31 23:59:59.999 GMT	(last ms of year 1991)
			{694224000000L},											//1992-01-01 00:00:00.000 GMT   (first ms of year 1992)
			{1456703999999L},											//2016-02-28 23:59:59.999 GMT	(last ms before a leap day)
			{1456704000000L},											//2016-02-29 00:00:00.000 GMT	(first ms of a leap day)
			{1456749808000L},											//2016-02-29 12:43:28.000 GMT	(random time during a leap day)
			{1456790399999L},											//2016-02-29 23:59:59.999 GMT	(last ms of a leap day)
			{1456790400000L},											//2016-03-30 00:00:00.000 GMT	(first ms after a leap day)
			{System.currentTimeMillis()},								//current time
			{System.currentTimeMillis()+UTCTimeUtility.MINUTE_IN_MS},		//current time + 1m
			{System.currentTimeMillis()+(2*UTCTimeUtility.MINUTE_IN_MS)},	//current time + 2m
			{System.currentTimeMillis()+(3*UTCTimeUtility.MINUTE_IN_MS)},	//current time + 3m
			{System.currentTimeMillis()+(4*UTCTimeUtility.MINUTE_IN_MS)},	//current time + 4m
			{System.currentTimeMillis()+(5*UTCTimeUtility.MINUTE_IN_MS)},	//current time + 5m
			{System.currentTimeMillis()+(6*UTCTimeUtility.MINUTE_IN_MS)},	//current time + 6m
			{System.currentTimeMillis()+(7*UTCTimeUtility.MINUTE_IN_MS)},	//current time + 7m
			{Long.MAX_VALUE-(60*UTCTimeUtility.WEEK_IN_MS)},				//max time - 60w
		});
	}
	
	// Test flooring & ceiling UTC time to the closes year
	@Test
	public void testYear_UTC(){		
		System.out.println("Testing year with timestamp " + currentTime_UTC + "   " + UTCTimeUtility.getDateString(currentTime_UTC));

		long thisPeriodStartedAt = UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.YEAR, currentTime_UTC);
		long thisPeriodEndedAt = UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.YEAR, currentTime_UTC);
		
		//verify period start is correct
		System.out.println("	year start timestamp = " + thisPeriodStartedAt + "   " + UTCTimeUtility.getDateString(thisPeriodStartedAt));
		assertTrue("perdiod start is not before current time", thisPeriodStartedAt <= currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", 0, UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", 1, UTCTimeUtility.getDayOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start month is wrong", Calendar.JANUARY, UTCTimeUtility.getMonthOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		//verify period end is correct
		System.out.println("	year end timestamp = " + thisPeriodEndedAt + "   " + UTCTimeUtility.getDateString(thisPeriodEndedAt));
		assertTrue("perdiod end is not ater current time", thisPeriodEndedAt >= currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", 23, UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", 31, UTCTimeUtility.getDayOfMonthFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end month is wrong", Calendar.DECEMBER, UTCTimeUtility.getMonthOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodEndedAt));
		
		//verify that start and end values are reasonable
		assertTrue("start is zero", thisPeriodStartedAt != 0);
		assertTrue("end is zero", thisPeriodEndedAt != 0);
		assertTrue("start is not before end", thisPeriodStartedAt < thisPeriodEndedAt );
		assertTrue("start and end are equal", thisPeriodStartedAt != thisPeriodEndedAt );
		
		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodStartedAt, UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.YEAR, thisPeriodEndedAt));
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodEndedAt, UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.YEAR, thisPeriodStartedAt));
	}	
	
	// Test flooring & ceiling UTC time to the closes month
	@Test
	public void testMonth_UTC(){
		System.out.println("Testing month with timestamp " + currentTime_UTC + "   " + UTCTimeUtility.getDateString(currentTime_UTC));
		Calendar tmpCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		tmpCal.setTimeInMillis(currentTime_UTC);

		long thisPeriodStartedAt = UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.MONTH, currentTime_UTC);
		long thisPeriodEndedAt = UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.MONTH, currentTime_UTC);
		
		//verify period start is correct
		System.out.println("	month start timestamp = " + thisPeriodStartedAt + "   " + UTCTimeUtility.getDateString(thisPeriodStartedAt));
		assertTrue("perdiod start is not before current time", thisPeriodStartedAt <= currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", 0, UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", 1, UTCTimeUtility.getDayOfMonthFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start month is wrong", UTCTimeUtility.getMonthOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getMonthOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		//verify period end is correct
		System.out.println("	month end timestamp = " + thisPeriodEndedAt + "   " + UTCTimeUtility.getDateString(thisPeriodEndedAt));
		assertTrue("perdiod end is not ater current time", thisPeriodEndedAt >= currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", 23, UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", tmpCal.getActualMaximum(Calendar.DAY_OF_MONTH), UTCTimeUtility.getDayOfMonthFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end month is wrong", UTCTimeUtility.getMonthOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getMonthOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodEndedAt));

		//verify that start and end values are reasonable
		assertTrue("start is zero", thisPeriodStartedAt != 0);
		assertTrue("end is zero", thisPeriodEndedAt != 0);
		assertTrue("start is not before end", thisPeriodStartedAt < thisPeriodEndedAt );
		assertTrue("start and end are equal", thisPeriodStartedAt != thisPeriodEndedAt );
		
		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodStartedAt, UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.MONTH, thisPeriodEndedAt));
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodEndedAt, UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.MONTH, thisPeriodStartedAt));
	}	
		
	// Test flooring & ceiling UTC time to the closes week
	@Test
	public void testWeek_UTC(){
		System.out.println("Testing week with timestamp " + currentTime_UTC + "   " + UTCTimeUtility.getDateString(currentTime_UTC));

		long thisPeriodStartedAt = UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.WEEK, currentTime_UTC);
		long thisPeriodEndedAt = UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.WEEK, currentTime_UTC);
		
		//verify period start is correct
		System.out.println("	week start timestamp = " + thisPeriodStartedAt + "   " + UTCTimeUtility.getDateString(thisPeriodStartedAt));
		assertTrue("period start is not before current time", thisPeriodStartedAt <= currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", 0, UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", Calendar.MONDAY, UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertTrue("period start year is more than one year off", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC)-UTCTimeUtility.getYearFromTimestamp(thisPeriodStartedAt) <= 1);

		//verify period end is correct
		System.out.println("	week end timestamp = " + thisPeriodEndedAt + "   " + UTCTimeUtility.getDateString(thisPeriodEndedAt));
		assertTrue("period end is not after current time", thisPeriodEndedAt >= currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", 23, UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", Calendar.SUNDAY, UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		System.out.println(UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC) +" - " +UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertTrue("period end year is more than one year off", UTCTimeUtility.getYearFromTimestamp(thisPeriodEndedAt)-UTCTimeUtility.getYearFromTimestamp(currentTime_UTC) <= 1);

		//verify that start and end values are reasonable
		assertTrue("start is zero", thisPeriodStartedAt != 0);
		assertTrue("end is zero", thisPeriodEndedAt != 0);
		assertTrue("start is not before end", thisPeriodStartedAt < thisPeriodEndedAt );
		assertTrue("start and end are equal", thisPeriodStartedAt != thisPeriodEndedAt );

		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodStartedAt, UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.WEEK, thisPeriodEndedAt));
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodEndedAt, UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.WEEK, thisPeriodStartedAt));
	}	
	
	// Test flooring & ceiling UTC time to the closes day
	@Test
	public void testDay_UTC(){
		System.out.println("Testing day with timestamp " + currentTime_UTC + "   " + UTCTimeUtility.getDateString(currentTime_UTC));

		long thisPeriodStartedAt = UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.DAY, currentTime_UTC);
		long thisPeriodEndedAt = UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.DAY, currentTime_UTC);
		
		//verify period start is correct
		System.out.println("	day start timestamp = " + thisPeriodStartedAt + "   " + UTCTimeUtility.getDateString(thisPeriodStartedAt));
		assertTrue("period start is not before current time", thisPeriodStartedAt <= currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", 0, UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", UTCTimeUtility.getDayOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		//verify period end is correct
		System.out.println("	day end timestamp = " + thisPeriodEndedAt + "   " + UTCTimeUtility.getDateString(thisPeriodEndedAt));
		assertTrue("period end is not after current time", thisPeriodEndedAt >= currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", 23, UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", UTCTimeUtility.getDayOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end month is wrong", UTCTimeUtility.getMonthOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getMonthOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodEndedAt));

		//verify that start and end values are reasonable
		assertTrue("start is zero", thisPeriodStartedAt != 0);
		assertTrue("end is zero", thisPeriodEndedAt != 0);
		assertTrue("start is not before end", thisPeriodStartedAt < thisPeriodEndedAt );
		assertTrue("start and end are equal", thisPeriodStartedAt != thisPeriodEndedAt );

		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodStartedAt, UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.DAY, thisPeriodEndedAt));
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodEndedAt, UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.DAY, thisPeriodStartedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes hour
	@Test
	public void testHour_UTC(){
		System.out.println("Testing hour with timestamp " + currentTime_UTC + "   " + UTCTimeUtility.getDateString(currentTime_UTC));

		long thisPeriodStartedAt = UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.HOUR, currentTime_UTC);
		long thisPeriodEndedAt = UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.HOUR, currentTime_UTC);
		
		//verify period start is correct
		System.out.println("	hour start timestamp = " + thisPeriodStartedAt + "   " + UTCTimeUtility.getDateString(thisPeriodStartedAt));
		assertTrue("period start is not before current time", thisPeriodStartedAt <= currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", 0, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", UTCTimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		//verify period end is correct
		System.out.println("	hour end timestamp = " + thisPeriodEndedAt + "   " + UTCTimeUtility.getDateString(thisPeriodEndedAt));
		assertTrue("period end is not after current time", thisPeriodEndedAt >= currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", 59, UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", UTCTimeUtility.getDayOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end month is wrong", UTCTimeUtility.getMonthOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getMonthOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodEndedAt));

		//verify that start and end values are reasonable
		assertTrue("start is zero", thisPeriodStartedAt != 0);
		assertTrue("end is zero", thisPeriodEndedAt != 0);
		assertTrue("start is not before end", thisPeriodStartedAt < thisPeriodEndedAt );
		assertTrue("start and end are equal", thisPeriodStartedAt != thisPeriodEndedAt );

		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodStartedAt, UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.HOUR, thisPeriodEndedAt));
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodEndedAt, UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.HOUR, thisPeriodStartedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes 15-minute period
	@Test
	public void testFifteenMinute_UTC(){
		System.out.println("Testing 15-min with timestamp " + currentTime_UTC + "   " + UTCTimeUtility.getDateString(currentTime_UTC));

		long thisPeriodStartedAt = UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.FIFTEEN_MINUTES, currentTime_UTC);
		long thisPeriodEndedAt = UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.FIFTEEN_MINUTES, currentTime_UTC);
		
		//verify period start is correct
		System.out.println("	15-min start timestamp = " + thisPeriodStartedAt + "   " + UTCTimeUtility.getDateString(thisPeriodStartedAt));
		assertTrue("period start is not before current time", thisPeriodStartedAt <= currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertTrue("the period start minute is in the future of the current time", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) >= UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertTrue("the period starts more than 5 minutes before the current time", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC)-UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt) <= 14);
		assertTrue("the period start minute is not a multiple of five", UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt) % 15 == 0);
		assertEquals("period start hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", UTCTimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		//verify period end is correct
		System.out.println("	15-min end timestamp = " + thisPeriodEndedAt + "   " + UTCTimeUtility.getDateString(thisPeriodEndedAt));
		assertTrue("period end is not after current time", thisPeriodEndedAt >= currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertTrue("the period end minute is before of the current time", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) <= UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertTrue("the period ends more than 15 minutes after the current time", UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt)-UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) <= 14);
		assertTrue("the period end minute(+1) is not a multiple of fifteen", UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt+1) % 15 == 0);
		assertEquals("period end hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", UTCTimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodEndedAt));

		//verify that start and end values are reasonable
		assertTrue("start is zero", thisPeriodStartedAt != 0);
		assertTrue("end is zero", thisPeriodEndedAt != 0);
		assertTrue("start is not before end", thisPeriodStartedAt < thisPeriodEndedAt );
		assertTrue("start and end are equal", thisPeriodStartedAt != thisPeriodEndedAt );

		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodStartedAt, UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.FIFTEEN_MINUTES, thisPeriodEndedAt));
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodEndedAt, UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.FIFTEEN_MINUTES, thisPeriodStartedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes 5-minute period
	@Test
	public void testFiveMinute_UTC(){
		System.out.println("Testing 5-min with timestamp " + currentTime_UTC + "   " + UTCTimeUtility.getDateString(currentTime_UTC));

		long thisPeriodStartedAt = UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.FIVE_MINUTES, currentTime_UTC);	
		long thisPeriodEndedAt = UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.FIVE_MINUTES, currentTime_UTC);
		
		//verify period start is correct
		System.out.println("	5-min start timestamp = " + thisPeriodStartedAt + "   " + UTCTimeUtility.getDateString(thisPeriodStartedAt));
		assertTrue("period start is not before current time", thisPeriodStartedAt <= currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertTrue("the period start minute is in the future of the current time", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) >= UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertTrue("the period starts more than 5 minutes before the current time", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC)-UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt) <= 4);
		assertTrue("the period start minute is not a multiple of five", UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt) % 5 == 0);
		assertEquals("period start hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", UTCTimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		//verify period end is correct
		System.out.println("	5-min end timestamp = " + thisPeriodEndedAt + "   " + UTCTimeUtility.getDateString(thisPeriodEndedAt));
		assertTrue("period end is not after current time", thisPeriodEndedAt >= currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertTrue("the period end minute is before of the current time", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) <= UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertTrue("the period ends more than 5 minutes after the current time", UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt)-UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC) <= 4);
		assertTrue("the period end minute(+1) is not a multiple of five", UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt+1) % 5 == 0);
		assertEquals("period end hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", UTCTimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodEndedAt));

		//verify that start and end values are reasonable
		assertTrue("start is zero", thisPeriodStartedAt != 0);
		assertTrue("end is zero", thisPeriodEndedAt != 0);
		assertTrue("start is not before end", thisPeriodStartedAt < thisPeriodEndedAt );
		assertTrue("start and end are equal", thisPeriodStartedAt != thisPeriodEndedAt );

		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodStartedAt, UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.FIVE_MINUTES, thisPeriodEndedAt));
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodEndedAt, UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.FIVE_MINUTES, thisPeriodStartedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes minute
	@Test
	public void testMinute_UTC(){
		System.out.println("Testing minute with timestamp " + currentTime_UTC + "   " + UTCTimeUtility.getDateString(currentTime_UTC));

		long thisPeriodStartedAt = UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.MINUTE, currentTime_UTC);
		long thisPeriodEndedAt = UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.MINUTE, currentTime_UTC);
		
		//verify period start is correct
		System.out.println("	minute start timestamp = " + thisPeriodStartedAt + "   " + UTCTimeUtility.getDateString(thisPeriodStartedAt));
		assertTrue("period start is not before current time", thisPeriodStartedAt <= currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", 0, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", UTCTimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		//verify period end is correct
		System.out.println("	minute end timestamp = " + thisPeriodEndedAt + "   " + UTCTimeUtility.getDateString(thisPeriodEndedAt));
		assertTrue("period end is not after current time", thisPeriodEndedAt >= currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", 59, UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", UTCTimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodEndedAt));

		//verify that start and end values are reasonable
		assertTrue("start is zero", thisPeriodStartedAt != 0);
		assertTrue("end is zero", thisPeriodEndedAt != 0);
		assertTrue("start is not before end", thisPeriodStartedAt < thisPeriodEndedAt );
		assertTrue("start and end are equal", thisPeriodStartedAt != thisPeriodEndedAt );

		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodStartedAt, UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.MINUTE, thisPeriodEndedAt));
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodEndedAt, UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.MINUTE, thisPeriodStartedAt));
	}
	
	// Test flooring & ceiling UTC time to the closes second
	@Test
	public void testSecond_UTC(){
		System.out.println("Testing second with timestamp " + currentTime_UTC + "   " + UTCTimeUtility.getDateString(currentTime_UTC));

		long thisPeriodStartedAt = UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.SECOND, currentTime_UTC);
		long thisPeriodEndedAt = UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.SECOND, currentTime_UTC);
		
		//verify period start is correct
		System.out.println("	second start timestamp = " + thisPeriodStartedAt + "   " + UTCTimeUtility.getDateString(thisPeriodStartedAt));
		assertTrue("period start is not before current time", thisPeriodStartedAt <= currentTime_UTC);
		assertEquals("period start millisecond is wrong", 0, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start second is wrong", UTCTimeUtility.getSecondOfMinuteFromTimestamp(currentTime_UTC), UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start minute is wrong", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start day is wrong", UTCTimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodStartedAt));
		assertEquals("period start year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodStartedAt));

		//verify period end is correct
		System.out.println("	second end timestamp = " + thisPeriodEndedAt + "   " + UTCTimeUtility.getDateString(thisPeriodEndedAt));
		assertTrue("period end is not after current time", thisPeriodEndedAt >= currentTime_UTC);
		assertEquals("period end millisecond is wrong", 999, UTCTimeUtility.getMillisecondInSecondFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end second is wrong", UTCTimeUtility.getSecondOfMinuteFromTimestamp(currentTime_UTC), UTCTimeUtility.getSecondOfMinuteFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end minute is wrong", UTCTimeUtility.getMinuteOfHourFromTimestamp(currentTime_UTC), UTCTimeUtility.getMinuteOfHourFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end hour is wrong", UTCTimeUtility.getHourOfDayFromTimestamp(currentTime_UTC), UTCTimeUtility.getHourOfDayFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end day is wrong", UTCTimeUtility.getDayOfWeekFromTimestamp(currentTime_UTC), UTCTimeUtility.getDayOfWeekFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end week is wrong", UTCTimeUtility.getWeekOfYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getWeekOfYearFromTimestamp(thisPeriodEndedAt));
		assertEquals("period end year is wrong", UTCTimeUtility.getYearFromTimestamp(currentTime_UTC), UTCTimeUtility.getYearFromTimestamp(thisPeriodEndedAt));

		//verify that start and end values are reasonable
		assertTrue("start is zero", thisPeriodStartedAt != 0);
		assertTrue("end is zero", thisPeriodEndedAt != 0);
		assertTrue("start is not before end", thisPeriodStartedAt < thisPeriodEndedAt );
		assertTrue("start and end are equal", thisPeriodStartedAt != thisPeriodEndedAt );

		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodStartedAt, UTCTimeUtility.getTimestampPeriodStart(AggregationPeriodLength.SECOND, thisPeriodEndedAt));
		assertEquals("start and/or end timestamp is not in the same period", thisPeriodEndedAt, UTCTimeUtility.getTimestampPeriodEnd(AggregationPeriodLength.SECOND, thisPeriodStartedAt));
	}
	
	// Test printing converting milliseconds to text
	@Test 
	public void testMsToString(){
		//low values
		assertEquals("00:00:00.000", UTCTimeUtility.timeInMsToString(0));
		assertEquals("00:00:00.001", UTCTimeUtility.timeInMsToString(1));
		assertEquals("00:00:01.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.SECOND_IN_MS));
		assertEquals("00:01:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.MINUTE_IN_MS));
		assertEquals("00:05:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.FIVE_MINUTES_IN_MS));
		assertEquals("01:00:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.HOUR_IN_MS));
		assertEquals("1d+00:00:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.DAY_IN_MS));
		assertEquals("1w+00:00:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.WEEK_IN_MS));

		//high values
		assertEquals("00:00:00.999", UTCTimeUtility.timeInMsToString(999));
		assertEquals("00:00:59.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.SECOND_IN_MS*59));
		assertEquals("00:59:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.MINUTE_IN_MS*59));
		assertEquals("23:00:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.HOUR_IN_MS*23));
		assertEquals("52w+5d+00:00:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.DAY_IN_MS*369));
		
		//high overflow values
		assertEquals("00:00:01.999", UTCTimeUtility.timeInMsToString(1999));
		assertEquals("00:02:39.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.SECOND_IN_MS*159));
		assertEquals("02:39:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.MINUTE_IN_MS*159));
		assertEquals("5d+03:00:00.000", UTCTimeUtility.timeInMsToString(UTCTimeUtility.HOUR_IN_MS*123));
		
		//combinations
		assertEquals("142w+5d+23:59:59.999", UTCTimeUtility.timeInMsToString((UTCTimeUtility.WEEK_IN_MS*142) + (UTCTimeUtility.DAY_IN_MS*5) + (UTCTimeUtility.HOUR_IN_MS*23) + (UTCTimeUtility.MINUTE_IN_MS*59) + (UTCTimeUtility.SECOND_IN_MS*59) + 999));
		assertEquals("6d+23:59:59.999", UTCTimeUtility.timeInMsToString(UTCTimeUtility.WEEK_IN_MS-1));
	}
	
	// Test printing converting milliseconds to text for a negative time
	@Test(expected=NumberFormatException.class)
	public void testMsToStringForNegativeArgument(){
		//low values
		UTCTimeUtility.timeInMsToString(-1);
	}
	
}
