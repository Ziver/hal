package se.koc.hal.deamon;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import se.koc.hal.HalContext;
import se.koc.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.db.handler.SimpleSQLResult;
import zutil.log.LogUtil;

public class DataAggregatorDaemon extends TimerTask implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();
    
	private enum AggregationMethod{
		SUM,
		AVG
	}
	
    public void initiate(Timer timer){
        timer.schedule(this, 0, TimeUtility.FIVE_MINUTES_IN_MS);
    }

    @Override
    public void run(){
    	try {
			List<Sensor> sensorList = Sensor.getLocalSensors(HalContext.getDB());
			for(Sensor sensor : sensorList){
				logger.fine("Aggregating sensor_id: " + sensor.getId());
				aggregateSensor(sensor);
			}
            logger.fine("Aggregation Done");
		} catch (SQLException e) {
            logger.log(Level.SEVERE, null, e);
		}
    }
    
    public void aggregateSensor(Sensor sensor) {
    	logger.fine("The sensor is of type: " + sensor.getType());
    	if(sensor.getType().equals("PowerMeter")){
    		logger.fine("aggregating raw data to five minute periods");
			aggregateRawData(sensor.getId(), TimeUtility.FIVE_MINUTES_IN_MS, 5, AggregationMethod.SUM);
			logger.fine("aggregating five minute periods into hour periods");
			aggrigateAggregatedData(sensor.getId(), TimeUtility.FIVE_MINUTES_IN_MS, TimeUtility.HOUR_IN_MS, 12, AggregationMethod.SUM);
			logger.fine("aggregating one hour periods into one day periods");
			aggrigateAggregatedData(sensor.getId(), TimeUtility.HOUR_IN_MS, TimeUtility.DAY_IN_MS, 24, AggregationMethod.SUM);
    	}else{
    		logger.fine("The sensor type is not supported by the aggregation deamon. Ignoring");
    	}
    }
    
    /**
     * Aggregate data from the raw DB table to the aggregated table
     * @param sensorId	The sensor for to aggregate data
     * @param toPeriodSizeInMs The period length in ms to aggregate to
     */
    private void aggregateRawData(long sensorId, long toPeriodSizeInMs, int expectedSampleCount, AggregationMethod aggrMethod){
    	DBConnection db = HalContext.getDB();
    	PreparedStatement stmt = null;
    	try {
    		
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr"
    				+ " WHERE sensor_id == ?"
    				+ " AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, toPeriodSizeInMs-1);
    		Long maxTimestampFoundForSensor = DBConnection.exec(stmt, new SimpleSQLResult<Long>());
    		if(maxTimestampFoundForSensor == null)
    			maxTimestampFoundForSensor = 0l;
    		long currentPeriodStartTimestamp = TimeUtility.getTimestampPeriodStart(toPeriodSizeInMs, System.currentTimeMillis());
    		logger.fine("Calculating periods... (from:"+ maxTimestampFoundForSensor +", to:"+ currentPeriodStartTimestamp +")");
    		stmt = db.getPreparedStatement("SELECT *, 1 AS confidence, timestamp AS timestamp_start FROM sensor_data_raw"
    				+" WHERE sensor_id == ?"
    					+ " AND ? < timestamp"
    					+ " AND timestamp < ? "
    				+" ORDER BY timestamp ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxTimestampFoundForSensor);
    		stmt.setLong(3, currentPeriodStartTimestamp);
    		DBConnection.exec(stmt, new DataAggregator(sensorId, toPeriodSizeInMs, expectedSampleCount, aggrMethod));
    	} catch (SQLException e) {
            logger.log(Level.SEVERE, null, e);
		}
    }
    
    /**
     * Re-aggregate data from the aggregated DB table to itself
     * @param sensorId The sensor for to aggregate data
     * @param fromPeriodSizeInMs The period length in ms to aggregate from
     * @param toPeriodSizeInMs The period length in ms to aggregate to
     */
    private void aggrigateAggregatedData(long sensorId, long fromPeriodSizeInMs, long toPeriodSizeInMs, int expectedSampleCount, AggregationMethod aggrMethod){
    	DBConnection db = HalContext.getDB();
    	PreparedStatement stmt = null;
    	try {
    		
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr"
    				+" WHERE sensor_id == ?"
    				+ " AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, toPeriodSizeInMs-1);
    		Long maxTimestampFoundForSensor = DBConnection.exec(stmt, new SimpleSQLResult<Long>());
    		if(maxTimestampFoundForSensor == null)
    			maxTimestampFoundForSensor = 0l;
    		long currentPeriodStartTimestamp = TimeUtility.getTimestampPeriodStart(toPeriodSizeInMs, System.currentTimeMillis());
    		logger.fine("Calculating periods... (from:"+ maxTimestampFoundForSensor +", to:"+ currentPeriodStartTimestamp +")");

    		stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr"
    				+" WHERE sensor_id == ?"
	    				+ " AND ? < timestamp_start"
	    				+ " AND timestamp_start < ?"
	    				+ " AND timestamp_end-timestamp_start == ?" 
    				+" ORDER BY timestamp_start ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxTimestampFoundForSensor);
    		stmt.setLong(3, currentPeriodStartTimestamp);
    		stmt.setLong(4, fromPeriodSizeInMs-1);
    		DBConnection.exec(stmt, new DataAggregator(sensorId, toPeriodSizeInMs, expectedSampleCount, aggrMethod));
		} catch (SQLException e) {
            logger.log(Level.SEVERE, null, e);
		}
    }
    
    /**
     * Internal class for aggregating data to the aggregated DB table
     */
    private class DataAggregator implements SQLResultHandler<Object>{
    	private long sensorId = -1;
    	private long aggrTimeInMs = -1;
    	private int expectedSampleCount = -1;
    	private AggregationMethod aggrMethod = null;
    	
    	public DataAggregator(long sensorId, long aggrTimeInMs, int expectedSampleCount, AggregationMethod aggrMethod) {
    		this.sensorId = sensorId;
    		this.aggrTimeInMs = aggrTimeInMs;
    		this.expectedSampleCount = expectedSampleCount;
    		this.aggrMethod = aggrMethod;
		}
    	
		@Override
		public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			try{
				HalContext.getDB().getConnection().setAutoCommit(false);
				
				long currentPeriodTimestamp = 0;
				int sum = 0;
				float confidenceSum = 0;
				int samples = 0;
				long highestSequenceId = Sensor.getHighestSequenceId(sensorId);
				PreparedStatement preparedInsertStmt = HalContext.getDB().getPreparedStatement("INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(?, ?, ?, ?, ?, ?)");
				while(result.next()){
					if(sensorId != result.getInt("sensor_id")){
						throw new IllegalArgumentException("found entry for aggregation for the wrong sensorId (expecting: "+sensorId+", but was: "+result.getInt("sensor_id")+")");
					}
					long timestamp = result.getLong("timestamp_start");
					long periodTimestamp = TimeUtility.getTimestampPeriodStart(this.aggrTimeInMs, timestamp);
					if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
						float aggrConfidence = confidenceSum / (float)this.expectedSampleCount;
						float data = -1;
						switch(aggrMethod){
							case SUM: data = sum; break;
							case AVG: data = sum/samples; break;
						}
						logger.finer("Calculated day period: " + currentPeriodTimestamp + ", data: " + sum + ", confidence: " + aggrConfidence + ", samples: " + samples + ", aggrMethod: " + aggrMethod);
						preparedInsertStmt.setInt(1, result.getInt("sensor_id"));
						preparedInsertStmt.setLong(2, ++highestSequenceId);
						preparedInsertStmt.setLong(3, currentPeriodTimestamp);
						preparedInsertStmt.setLong(4, currentPeriodTimestamp + this.aggrTimeInMs - 1);
						preparedInsertStmt.setInt(5, (int)data);	//TODO: make data float in DB to handle aggrMethod.AVG where the data must be able to be saved as a float
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

                DBConnection.execBatch(preparedInsertStmt);
                HalContext.getDB().getConnection().commit();

			}catch(Exception e){
				HalContext.getDB().getConnection().rollback();
				throw e;
			}finally{
				HalContext.getDB().getConnection().setAutoCommit(true);
			}
			return null;
		}    	
    }
        
}
