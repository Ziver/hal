package se.koc.hal.deamon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Logger;

import se.koc.hal.HalContext;
import se.koc.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.db.handler.SimpleSQLResult;
import zutil.log.LogUtil;

public class DataAggregatorDaemon extends TimerTask implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();
	public static final long FIVE_MINUTES_IN_MS = 5 * 60 * 1000;
    public static final long HOUR_IN_MS = FIVE_MINUTES_IN_MS * 12;
    public static final long DAY_IN_MS = HOUR_IN_MS * 24;
    
    private static final long HOUR_AGGREGATION_OFFSET = DAY_IN_MS;    
    private static final long DAY_AGGREGATION_OFFSET = DAY_IN_MS * 3;


    public void initiate(Timer timer){
        timer.schedule(this, 0, FIVE_MINUTES_IN_MS);
    }

    @Override
    public void run(){
    	try {
			List<Sensor> sensorList = Sensor.getLocalSensors(HalContext.getDB());
			for(Sensor sensor : sensorList){
				logger.fine("Aggregating sensor_id: " + sensor.getId());
				aggregateSensor(sensor.getId());
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public void aggregateSensor(long sensorId) {
    	DBConnection db = HalContext.getDB();
    	PreparedStatement stmt = null;
    	try {
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr WHERE sensor_id == ?");
    		stmt.setLong(1, sensorId);
    		Long maxDBTimestamp = DBConnection.exec(stmt, new SimpleSQLResult<Long>());
    		if(maxDBTimestamp == null)
    			maxDBTimestamp = 0l;
    		
    		// 5 minute aggregation
    		long minPeriodTimestamp = getTimestampMinutePeriodStart(5, System.currentTimeMillis());
    		logger.fine("Calculating 5 min periods... (from:"+ maxDBTimestamp +", to:"+ minPeriodTimestamp +")");
    		stmt = db.getPreparedStatement("SELECT * FROM sensor_data_raw"
    				+" WHERE sensor_id == ? AND ? < timestamp AND timestamp < ? "
    				+" ORDER BY timestamp ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxDBTimestamp);
    		stmt.setLong(3, minPeriodTimestamp);
    		DBConnection.exec(stmt, new FiveMinuteAggregator());
    		
    		// hour aggregation
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, HOUR_IN_MS-1);
    		maxDBTimestamp = DBConnection.exec(stmt, new SimpleSQLResult<Long>());
    		if(maxDBTimestamp == null)
    			maxDBTimestamp = 0l;
    		long hourPeriodTimestamp = getTimestampMinutePeriodStart(60, System.currentTimeMillis()-HOUR_AGGREGATION_OFFSET);
    		logger.fine("Calculating hour periods... (from:"+ maxDBTimestamp +", to:"+ hourPeriodTimestamp +")");
    		stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? AND ? < timestamp_start AND timestamp_start < ? AND timestamp_end-timestamp_start == ?" 
    				+" ORDER BY timestamp_start ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxDBTimestamp);
    		stmt.setLong(3, hourPeriodTimestamp);
    		stmt.setLong(4, FIVE_MINUTES_IN_MS-1);
    		DBConnection.exec(stmt, new HourAggregator());
    		
    		// day aggregation
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr WHERE sensor_id == ? AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, DAY_IN_MS-1);
    		maxDBTimestamp = DBConnection.exec(stmt, new SimpleSQLResult<Long>());
    		if(maxDBTimestamp == null)
    			maxDBTimestamp = 0l;
    		long dayPeriodTimestamp = getTimestampHourPeriodStart(24, System.currentTimeMillis()-DAY_AGGREGATION_OFFSET);
    		logger.fine("Calculating day periods... (from:"+ maxDBTimestamp +", to:"+ dayPeriodTimestamp +")");
    		stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? AND ? < timestamp_start AND timestamp_start < ? AND timestamp_end-timestamp_start == ?"
    				+" ORDER BY timestamp_start ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxDBTimestamp);
    		stmt.setLong(3, dayPeriodTimestamp);
    		stmt.setLong(4, HOUR_IN_MS-1);
    		DBConnection.exec(stmt, new DayAggregator());
    		
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
    
    
    private class FiveMinuteAggregator implements SQLResultHandler<Object>{
		@Override
		public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			long currentPeriodTimestamp = 0;
			int sum = 0;
			int count = 0;
			int samples = 0;
			while(result.next()){
				long timestamp = result.getLong("timestamp");
				long periodTimestamp = getTimestampMinutePeriodStart(5, timestamp);
				if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
					float confidence = count / 5f;
					logger.finer("Calculated minute period: "+ currentPeriodTimestamp +" sum: "+ sum +" confidence: "+ confidence+ " samples: " + samples);
					PreparedStatement prepStmt = HalContext.getDB().getPreparedStatement("INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(?, ?, ?, ?, ?, ?)");
					prepStmt.setInt(1, result.getInt("sensor_id"));
					prepStmt.setLong(2, Sensor.getHighestSequenceId(result.getInt("sensor_id")) + 1);
					prepStmt.setLong(3, currentPeriodTimestamp);
					prepStmt.setLong(4, currentPeriodTimestamp + FIVE_MINUTES_IN_MS - 1);
					prepStmt.setInt(5, sum);
					prepStmt.setFloat(6, confidence);
					DBConnection.exec(prepStmt);
					
					// Reset variables
					currentPeriodTimestamp = periodTimestamp;
					sum = count = samples = 0;
				}
				if(currentPeriodTimestamp == 0) currentPeriodTimestamp = periodTimestamp;
				sum += result.getInt("data");
				++count;
				++samples;
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
			int samples = 0;
			while(result.next()){
				long timestamp = result.getLong("timestamp_start");
				long periodTimestamp = getTimestampMinutePeriodStart(60, timestamp);
				if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
					float aggrConfidence = confidenceSum / 12f;
					logger.finer("Calculated hour period: "+ currentPeriodTimestamp +" sum: "+ sum +" confidence: "+ aggrConfidence+ " samples: " + samples);
					PreparedStatement prepStmt = HalContext.getDB().getPreparedStatement("INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(?, ?, ?, ?, ?, ?)");
					prepStmt.setInt(1, result.getInt("sensor_id"));
					prepStmt.setLong(2, Sensor.getHighestSequenceId(result.getInt("sensor_id")) + 1);
					prepStmt.setLong(3, currentPeriodTimestamp);
					prepStmt.setLong(4, currentPeriodTimestamp + HOUR_IN_MS - 1);
					prepStmt.setInt(5, sum);
					prepStmt.setFloat(6, aggrConfidence);
					DBConnection.exec(prepStmt);
					
					// Reset variables
					currentPeriodTimestamp = periodTimestamp;
					confidenceSum = sum = samples = 0;
				}
				if(currentPeriodTimestamp == 0) currentPeriodTimestamp = periodTimestamp;
				sum += result.getInt("data");
				confidenceSum += result.getFloat("confidence");
				samples++;
				
				//TODO: SHould not be here!
				PreparedStatement prepStmt = HalContext.getDB().getPreparedStatement("DELETE FROM sensor_data_aggr WHERE sensor_id == ? AND sequence_id == ?");
				prepStmt.setInt(1, result.getInt("sensor_id"));
				prepStmt.setInt(2, result.getInt("sequence_id"));
				DBConnection.exec(prepStmt);
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
					PreparedStatement prepStmt = HalContext.getDB().getPreparedStatement("INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(?, ?, ?, ?, ?, ?)");
					prepStmt.setInt(1, result.getInt("sensor_id"));
					prepStmt.setLong(2, Sensor.getHighestSequenceId(result.getInt("sensor_id")) + 1);
					prepStmt.setLong(3, currentPeriodTimestamp);
					prepStmt.setLong(4, currentPeriodTimestamp + DAY_IN_MS - 1);
					prepStmt.setInt(5, sum);
					prepStmt.setFloat(6, aggrConfidence);
					DBConnection.exec(prepStmt);
					
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
				PreparedStatement prepStmt = HalContext.getDB().getPreparedStatement("DELETE FROM sensor_data_aggr WHERE sensor_id == ? AND sequence_id == ?");
				prepStmt.setInt(1, result.getInt("sensor_id"));
				prepStmt.setInt(2, result.getInt("sequence_id"));
				DBConnection.exec(prepStmt);
			}
			return null;
		}    	
    }
}
