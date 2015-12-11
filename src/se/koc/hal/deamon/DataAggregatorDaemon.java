package se.koc.hal.deamon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
    
    public void initiate(Timer timer){
        timer.schedule(this, 0, TimeConstants.FIVE_MINUTES_IN_MS);
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
    		long minPeriodTimestamp = getTimestampPeriodStart(TimeConstants.FIVE_MINUTES_IN_MS, System.currentTimeMillis());
    		logger.fine("Calculating 5 min periods... (from:"+ maxDBTimestamp +", to:"+ minPeriodTimestamp +")");
    		stmt = db.getPreparedStatement("SELECT *, 1 AS confidence, timestamp AS timestamp_start FROM sensor_data_raw"
    				+" WHERE sensor_id == ? AND ? < timestamp AND timestamp < ? "
    				+" ORDER BY timestamp ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxDBTimestamp);
    		stmt.setLong(3, minPeriodTimestamp);
    		DBConnection.exec(stmt, new DataAggregator(sensorId, TimeConstants.FIVE_MINUTES_IN_MS, 5));
    		
    		// hour aggregation
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, TimeConstants.HOUR_IN_MS-1);
    		maxDBTimestamp = DBConnection.exec(stmt, new SimpleSQLResult<Long>());
    		if(maxDBTimestamp == null)
    			maxDBTimestamp = 0l;
    		long hourPeriodTimestamp = getTimestampPeriodStart(TimeConstants.HOUR_IN_MS, System.currentTimeMillis());
    		logger.fine("Calculating hour periods... (from:"+ maxDBTimestamp +", to:"+ hourPeriodTimestamp +")");
    		stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? AND ? < timestamp_start AND timestamp_start < ? AND timestamp_end-timestamp_start == ?" 
    				+" ORDER BY timestamp_start ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxDBTimestamp);
    		stmt.setLong(3, hourPeriodTimestamp);
    		stmt.setLong(4, TimeConstants.FIVE_MINUTES_IN_MS-1);
    		DBConnection.exec(stmt, new DataAggregator(sensorId, TimeConstants.HOUR_IN_MS, 12));
    		
    		// day aggregation
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr WHERE sensor_id == ? AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, TimeConstants.DAY_IN_MS-1);
    		maxDBTimestamp = DBConnection.exec(stmt, new SimpleSQLResult<Long>());
    		if(maxDBTimestamp == null)
    			maxDBTimestamp = 0l;
    		long dayPeriodTimestamp = getTimestampPeriodStart(TimeConstants.DAY_IN_MS, System.currentTimeMillis());
    		logger.fine("Calculating day periods... (from:"+ maxDBTimestamp +", to:"+ dayPeriodTimestamp +")");
    		stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? AND ? < timestamp_start AND timestamp_start < ? AND timestamp_end-timestamp_start == ?"
    				+" ORDER BY timestamp_start ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxDBTimestamp);
    		stmt.setLong(3, dayPeriodTimestamp);
    		stmt.setLong(4, TimeConstants.HOUR_IN_MS-1);
    		DBConnection.exec(stmt, new DataAggregator(sensorId, TimeConstants.DAY_IN_MS, 24));
    		    		
    		logger.fine("Done aggregation");
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (ValueOutsideOfRangeException e) {
			e.printStackTrace();
		}
    }
    
    private class DataAggregator implements SQLResultHandler<Object>{
    	private long sensorId = -1;
    	private long aggrTimeInMs = -1;
    	private int expectedSampleCount = -1;
    	
    	public DataAggregator(long sensorId, long aggrTimeInMs, int expectedSampleCount) {
    		this.sensorId = sensorId;
    		this.aggrTimeInMs = aggrTimeInMs;
    		this.expectedSampleCount = expectedSampleCount;
		}
    	
		@Override
		public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			try{
				long currentPeriodTimestamp = 0;
				int sum = 0;
				float confidenceSum = 0;
				int samples = 0;
				long highestSequenceId = Sensor.getHighestSequenceId(sensorId);
				HalContext.getDB().getConnection().setAutoCommit(false);
				boolean rollback = false;
				PreparedStatement preparedInsertStmt = HalContext.getDB().getPreparedStatement("INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(?, ?, ?, ?, ?, ?)");
				while(result.next()){
					if(sensorId != result.getInt("sensor_id")){
						logger.severe("found entry for aggregation for the wrong sensorId (expecting: "+sensorId+", but was: "+result.getInt("sensor_id")+")");
						rollback = true;
						break;
					}
					long timestamp = result.getLong("timestamp_start");
					long periodTimestamp = getTimestampPeriodStart(this.aggrTimeInMs, timestamp);
					if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
						float aggrConfidence = confidenceSum / (float)this.expectedSampleCount;
						logger.finer("Calculated day period: "+ currentPeriodTimestamp +" sum: "+ sum +" confidence: "+ aggrConfidence+ " samples: " + samples);
						preparedInsertStmt.setInt(1, result.getInt("sensor_id"));
						preparedInsertStmt.setLong(2, ++highestSequenceId);
						preparedInsertStmt.setLong(3, currentPeriodTimestamp);
						preparedInsertStmt.setLong(4, currentPeriodTimestamp + this.aggrTimeInMs - 1);
						preparedInsertStmt.setInt(5, sum);
						preparedInsertStmt.setFloat(6, aggrConfidence);
						preparedInsertStmt.addBatch();
						
						// Reset variables
						currentPeriodTimestamp = periodTimestamp;
						confidenceSum = sum = samples = 0;
					}
					if(currentPeriodTimestamp == 0) currentPeriodTimestamp = periodTimestamp;
					sum += result.getInt("data");
					confidenceSum += result.getFloat("confidence");
					samples++;
				}
				if(!rollback){
					DBConnection.execBatch(preparedInsertStmt);
					HalContext.getDB().getConnection().commit();
				}else{
					HalContext.getDB().getConnection().rollback();
				}
			}catch(SQLException e){
				HalContext.getDB().getConnection().rollback();
				throw e;
			} catch (ValueOutsideOfRangeException e) {
				HalContext.getDB().getConnection().rollback();
				e.printStackTrace();
			}finally{
				HalContext.getDB().getConnection().setAutoCommit(true);
			}
			return null;
		}    	
    }
    
    private static long getTimestampPeriodStart(long periodLengthInMs, long timestamp) throws ValueOutsideOfRangeException{
    	long tmp = timestamp % periodLengthInMs;
    	return timestamp - tmp;
    }
        
}
