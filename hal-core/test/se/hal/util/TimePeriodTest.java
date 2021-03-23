package se.hal.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import se.hal.daemon.SensorDataAggregatorDaemon.AggregationPeriodLength;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class TimePeriodTest {
	private final long currentTime_UTC;


	/**
	 * Constructor
	 * @param timestamp
	 */
	public TimePeriodTest(long timestamp){
		this.currentTime_UTC = timestamp;
	}

	@Parameters
	public static Collection<Object[]> data(){
		return Arrays.asList(new Object[][] {
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
		});
	}

	@Test
	public void testYearTimePeriod(){
		testSeriesOfPeriods(AggregationPeriodLength.YEAR);
	}

	@Test
	public void testMonthTimePeriod(){
		testSeriesOfPeriods(AggregationPeriodLength.MONTH);
	}

	@Test
	public void testWeekTimePeriod(){
		testSeriesOfPeriods(AggregationPeriodLength.WEEK);
	}

	@Test
	public void testDayTimePeriod(){
		testSeriesOfPeriods(AggregationPeriodLength.DAY);
	}

	@Test
	public void testHourTimePeriod(){
		testSeriesOfPeriods(AggregationPeriodLength.HOUR);
	}

	@Test
	public void test15MiuteTimePeriod(){
		testSeriesOfPeriods(AggregationPeriodLength.FIFTEEN_MINUTES);
	}

	@Test
	public void test5MiuteTimePeriod(){
		testSeriesOfPeriods(AggregationPeriodLength.FIVE_MINUTES);
	}

	@Test
	public void testMiuteTimePeriod(){
		testSeriesOfPeriods(AggregationPeriodLength.MINUTE);
	}

	@Test
	public void testSecondTimePeriod(){
		testSeriesOfPeriods(AggregationPeriodLength.SECOND);
	}

	private void testSeriesOfPeriods(AggregationPeriodLength periodLength){
		UTCTimePeriod tp = new UTCTimePeriod(currentTime_UTC, periodLength);

		//test next 50 periods
		UTCTimePeriod prevTp = tp;
		UTCTimePeriod nextTp = null;
		for (int i = 0; i < 50; ++i){
			nextTp = prevTp.getNextPeriod();
			assertTrue("previos period end must be older than the next period end", prevTp.getEndTimestamp() < nextTp.getStartTimestamp());
			assertTrue("previous period +1ms must be equal to next period start", prevTp.getEndTimestamp()+1 == nextTp.getStartTimestamp());
			assertTrue("previous and next period should not be the same", !prevTp.equals(nextTp));
			testPeriod(nextTp, periodLength);
			prevTp = nextTp;
		}

		//test previous 50 periods
		nextTp = tp;
		for (int i = 0; i < 50; ++i){
			prevTp = nextTp.getPreviosPeriod();
			assertTrue("previos period end must be older than the next period end", prevTp.getEndTimestamp() < nextTp.getStartTimestamp());
			assertTrue("previous period +1ms must be equal to next period start", prevTp.getEndTimestamp()+1 == nextTp.getStartTimestamp());
			assertTrue("previous and next period should not be the same", !prevTp.equals(nextTp));
			testPeriod(nextTp, periodLength);
			nextTp = prevTp;
		}
	}

	private void testPeriod(UTCTimePeriod period, AggregationPeriodLength periodLength){
		//verify that the period start and end is in the same period
		assertEquals("start and/or end timestamp is not in the same period", period.getStartTimestamp(), UTCTimeUtility.getTimestampPeriodStart(periodLength, period.getEndTimestamp()));
		assertEquals("start and/or end timestamp is not in the same period", period.getEndTimestamp(), UTCTimeUtility.getTimestampPeriodEnd(periodLength, period.getStartTimestamp()));
	}

}
