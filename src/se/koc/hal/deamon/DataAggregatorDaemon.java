package se.koc.hal.deamon;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import se.koc.hal.PowerChallenge;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.db.handler.SimpleSQLHandler;
import zutil.log.LogUtil;

/**
 * Created by Ziver on 2015-12-03.
 */
public class DataAggregatorDaemon extends TimerTask implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();
	public static final long FIVE_MINUTES_IN_MS = 5 * 60 * 1000;
    public static final long HOUR_IN_MS = FIVE_MINUTES_IN_MS * 12;
    public static final long DAY_IN_MS = HOUR_IN_MS * 24;
    
    private static final long HOUR_AGGREGATION_OFFSET = DAY_IN_MS;    
    private static final long DAY_AGGREGATION_OFFSET = DAY_IN_MS * 3;


    public void initiate(Timer timer){
        timer.schedule(this, FIVE_MINUTES_IN_MS);
        run();
    }


    @Override
    public void run() {
    	DBConnection db = PowerChallenge.db;
    	try {
    		Long maxDBTimestamp = db.exec("SELECT MAX(timestamp_end) FROM sensor_data_aggr", new SimpleSQLHandler<Long>());
    		if(maxDBTimestamp == null)
    			maxDBTimestamp = 0l;
    		// 5 minute aggregation
    		long minPeriodTimestamp = getTimestampMinutePeriodStart(5, System.currentTimeMillis());
    		logger.fine("Calculating 5 min periods... (from:"+ maxDBTimestamp +", to:"+ minPeriodTimestamp +")");
    		db.exec("SELECT * FROM sensor_data_raw "
    				+ "WHERE timestamp > " + maxDBTimestamp + " AND timestamp < " + minPeriodTimestamp
    				+ " ORDER BY timestamp ASC", 
    				new FiveMinuteAggregator());
    		
    		// hour aggregation
    		maxDBTimestamp = db.exec("SELECT MAX(timestamp_end) FROM sensor_data_aggr WHERE timestamp_end-timestamp_start == " + (HOUR_IN_MS-1), new SimpleSQLHandler<Long>());
    		if(maxDBTimestamp == null)
    			maxDBTimestamp = 0l;
    		long hourPeriodTimestamp = getTimestampMinutePeriodStart(60, System.currentTimeMillis()-HOUR_AGGREGATION_OFFSET);
    		logger.fine("Calculating hour periods... (from:"+ maxDBTimestamp +", to:"+ hourPeriodTimestamp +")");
    		db.exec("SELECT * FROM sensor_data_aggr "
    				+ "WHERE " + maxDBTimestamp + " < timestamp_start AND timestamp_start < " + hourPeriodTimestamp + " AND timestamp_end-timestamp_start == " + (FIVE_MINUTES_IN_MS-1) 
    				+" ORDER BY timestamp_start ASC", 
    				new HourAggregator());
    		
    		// day aggregation
    		maxDBTimestamp = db.exec("SELECT MAX(timestamp_end) FROM sensor_data_aggr WHERE timestamp_end-timestamp_start == " + (DAY_IN_MS-1), new SimpleSQLHandler<Long>());
    		if(maxDBTimestamp == null)
    			maxDBTimestamp = 0l;
    		long dayPeriodTimestamp = getTimestampHourPeriodStart(24, System.currentTimeMillis()-DAY_AGGREGATION_OFFSET);
    		logger.fine("Calculating day periods... (from:"+ maxDBTimestamp +", to:"+ dayPeriodTimestamp +")");
    		db.exec("SELECT * FROM sensor_data_aggr "
    				+ "WHERE " + maxDBTimestamp + " < timestamp_start AND timestamp_start < " + dayPeriodTimestamp + " AND timestamp_end-timestamp_start == " + (HOUR_IN_MS-1)
    				+" ORDER BY timestamp_start ASC", 
    				new DayAggregator());
    		
    		
    		logger.fine("Done aggregation");
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
    private static long getTimestampHourPeriodStart(int hour, long timestamp){
    	Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int currentMinute = cal.get(Calendar.HOUR_OF_DAY);
		cal.set(Calendar.HOUR_OF_DAY, (currentMinute/hour) * hour);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
    }
    
    private static long getTimestampMinutePeriodStart(int min, long timestamp){
    	Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int currentMinute = cal.get(Calendar.MINUTE);
		cal.set(Calendar.MINUTE, (currentMinute/min) * min);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTimeInMillis();
    }
    
    public static Integer getNextSequenceId(long sensorId) throws SQLException{
    	 Integer id = PowerChallenge.db.exec("SELECT MAX(sequence_id) FROM sensor_data_aggr WHERE sensor_id == "+ sensorId, new SimpleSQLHandler<Integer>());
    	 return (id != null ? id+1 : 1);
    }
    
    private class FiveMinuteAggregator implements SQLResultHandler<Object>{
		@Override
		public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			long currentPeriodTimestamp = 0;
			int sum = 0;
			int count = 0;
			while(result.next()){
				long timestamp = result.getLong("timestamp");
				long periodTimestamp = getTimestampMinutePeriodStart(5, timestamp);
				if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
					float confidence = Math.min(count / 5f, 1.0f);
					logger.finer("Calculated minute period: "+ currentPeriodTimestamp +" sum: "+ sum +" confidence: "+ confidence);
					PowerChallenge.db.exec(String.format(Locale.US, "INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(%d, %d, %d, %d, %d, %f)",
							result.getInt("sensor_id"),
							getNextSequenceId(result.getInt("sensor_id")),
							currentPeriodTimestamp,
							currentPeriodTimestamp + FIVE_MINUTES_IN_MS -1,
							sum,
							confidence));
					
					// Reset variables
					currentPeriodTimestamp = periodTimestamp;
					confidence = sum = 0;
				}
				if(currentPeriodTimestamp == 0) currentPeriodTimestamp = periodTimestamp;
				sum += result.getInt("data");
				++count;
			}
			return null;
		}    	
    }
    
    private class HourAggregator implements SQLResultHandler<Object>{
		@Override
		public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			long currentPeriodTimestamp = 0;
			int sum = 0;
			float confidenceSum = 0;
			while(result.next()){
				long timestamp = result.getLong("timestamp_start");
				long periodTimestamp = getTimestampMinutePeriodStart(60, timestamp);
				if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
					float aggrConfidence = confidenceSum / 12f;
					logger.finer("Calculated hour period: "+ currentPeriodTimestamp +" sum: "+ sum +" confidence: "+ aggrConfidence);
					PowerChallenge.db.exec(String.format(Locale.US, "INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(%d, %d, %d, %d, %d, %f)",
							result.getInt("sensor_id"),
							getNextSequenceId(result.getInt("sensor_id")),
							currentPeriodTimestamp,
							currentPeriodTimestamp + HOUR_IN_MS -1,
							sum,
							aggrConfidence));
					
					// Reset variables
					currentPeriodTimestamp = periodTimestamp;
					confidenceSum = sum = 0;
				}
				if(currentPeriodTimestamp == 0) currentPeriodTimestamp = periodTimestamp;
				sum += result.getInt("data");
				confidenceSum += result.getFloat("confidence");
				
				//TODO: SHould not be here!
				PowerChallenge.db.exec("DELETE FROM sensor_data_aggr "
						+ "WHERE sensor_id == "+ result.getInt("sensor_id") +" AND sequence_id == "+ result.getInt("sequence_id"));
			}
			return null;
		}    	
    }
    
    private class DayAggregator implements SQLResultHandler<Object>{
		@Override
		public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			long currentPeriodTimestamp = 0;
			int sum = 0;
			float confidenceSum = 0;
			int samples = 0;
			while(result.next()){
				long timestamp = result.getLong("timestamp_start");
				long periodTimestamp = getTimestampHourPeriodStart(24, timestamp);
				if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
					float aggrConfidence = confidenceSum / 24f;
					logger.finer("Calculated day period: "+ currentPeriodTimestamp +" sum: "+ sum +" confidence: "+ aggrConfidence+ " samples: " + samples);
					PowerChallenge.db.exec(String.format(Locale.US, "INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(%d, %d, %d, %d, %d, %f)",
							result.getInt("sensor_id"),
							getNextSequenceId(result.getInt("sensor_id")),
							currentPeriodTimestamp,
							currentPeriodTimestamp + DAY_IN_MS -1,
							sum,
							aggrConfidence));
					
					// Reset variables
					currentPeriodTimestamp = periodTimestamp;
					confidenceSum = sum = 0;
					samples = 0;
				}
				if(currentPeriodTimestamp == 0) currentPeriodTimestamp = periodTimestamp;
				sum += result.getInt("data");
				confidenceSum += result.getFloat("confidence");
				samples++;
				
				//TODO: SHould not be here!
				PowerChallenge.db.exec("DELETE FROM sensor_data_aggr "
						+ "WHERE sensor_id == "+ result.getInt("sensor_id") +" AND sequence_id == "+ result.getInt("sequence_id"));
			}
			return null;
		}    	
    }
}
