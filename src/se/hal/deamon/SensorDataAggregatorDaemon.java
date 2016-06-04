package se.hal.deamon;

import se.hal.HalContext;
import se.hal.intf.HalDaemon;
import se.hal.intf.HalSensorData.AggregationMethod;
import se.hal.struct.Sensor;
import se.hal.util.UTCTimePeriod;
import se.hal.util.UTCTimeUtility;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.db.handler.SimpleSQLResult;
import zutil.log.LogUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SensorDataAggregatorDaemon implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();
	
	public enum AggregationPeriodLength{
		SECOND,
		MINUTE,
		FIVE_MINUTES,
		FIFTEEN_MINUTES,
		HOUR,
		DAY,
		WEEK,
		MONTH,
		YEAR
	}

    public void initiate(ScheduledExecutorService executor){
        executor.scheduleAtFixedRate(this, 0, UTCTimeUtility.FIVE_MINUTES_IN_MS, TimeUnit.MILLISECONDS);
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
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Thread has crashed", e);
		}
    }
    
    public void aggregateSensor(Sensor sensor) {
    	if(sensor.getDeviceData() == null){
    		logger.fine("The sensor type is not supported - ignoring it");
    		return;
    	}
		logger.fine("The sensor is of type: " + sensor.getDeviceData().getClass().getName());
		
		long aggregationStartTime = System.currentTimeMillis();
		
		logger.fine("Aggregating raw data up to a day old into five minute periods");
		aggregateRawData(sensor, AggregationPeriodLength.FIVE_MINUTES, UTCTimeUtility.DAY_IN_MS, 5, aggregationStartTime);
		
		logger.fine("Aggregating raw data up to a week old into one hour periods");
		aggregateRawData(sensor, AggregationPeriodLength.HOUR, UTCTimeUtility.WEEK_IN_MS, 60, aggregationStartTime);
		
		logger.fine("Aggregating raw data into one day periods");
		aggregateRawData(sensor, AggregationPeriodLength.DAY, UTCTimeUtility.INFINITY, 60*24, aggregationStartTime);
		
		logger.fine("Aggregating raw data into one week periods");
		aggregateRawData(sensor, AggregationPeriodLength.WEEK, UTCTimeUtility.INFINITY, 60*24*7, aggregationStartTime);
    }
    
    /**
     * Aggregate data from the raw DB table to the aggregated table
     * @param	sensor				The sensor for to aggregate data
     * @param   ageLimitInMs		Only aggregate up to this age
     */
    private void aggregateRawData(Sensor sensor, AggregationPeriodLength aggrPeriodLength, long ageLimitInMs, int expectedSampleCount, long aggregationStartTime){
    	long sensorId = sensor.getId();
    	AggregationMethod aggrMethod = sensor.getDeviceData().getAggregationMethod();
    	DBConnection db = HalContext.getDB();
    	PreparedStatement stmt = null;
    	try {
    		
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr"
    				+ " WHERE sensor_id == ?"
    					+ " AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		switch(aggrPeriodLength){
    			case SECOND: stmt.setLong(2, UTCTimeUtility.SECOND_IN_MS-1); break;
    			case MINUTE: stmt.setLong(2, UTCTimeUtility.MINUTE_IN_MS-1); break; 
				case FIVE_MINUTES: stmt.setLong(2, UTCTimeUtility.FIVE_MINUTES_IN_MS-1); break;
				case FIFTEEN_MINUTES: stmt.setLong(2, UTCTimeUtility.FIFTEEN_MINUTES_IN_MS-1); break;
				case HOUR: stmt.setLong(2, UTCTimeUtility.HOUR_IN_MS-1); break;
				case DAY: stmt.setLong(2, UTCTimeUtility.DAY_IN_MS-1); break;
				case WEEK: stmt.setLong(2, UTCTimeUtility.WEEK_IN_MS-1); break;
				default: logger.warning("aggregation period length is not supported."); return;
    		}
    		Long maxTimestampFoundForSensor = DBConnection.exec(stmt, new SimpleSQLResult<Long>());
    		if(maxTimestampFoundForSensor == null)
    			maxTimestampFoundForSensor = 0l;
    		
    		long latestCompletePeriodEndTimestamp = new UTCTimePeriod(aggregationStartTime, aggrPeriodLength).getPreviosPeriod().getEndTimestamp();
    		long oldestPeriodStartTimestamp = new UTCTimePeriod(aggregationStartTime-ageLimitInMs, aggrPeriodLength).getStartTimestamp();
    		
    		if(latestCompletePeriodEndTimestamp == maxTimestampFoundForSensor){
    			logger.fine("no new data to evaluate - aggregation is up to date");
    			return;
    		}else{
    			logger.fine("evaluating period: "+ (maxTimestampFoundForSensor+1) + "=>" + latestCompletePeriodEndTimestamp + " (" + UTCTimeUtility.getDateString(maxTimestampFoundForSensor+1) + "=>" + UTCTimeUtility.getDateString(latestCompletePeriodEndTimestamp) + ") with expected sample count: " + expectedSampleCount);
    		}
    		
    		stmt = db.getPreparedStatement("SELECT *, 1 AS confidence FROM sensor_data_raw"
    				+" WHERE sensor_id == ?"
    					+ " AND timestamp > ?"
    					+ " AND timestamp <= ? "
    					+ " AND timestamp >= ? "
    				+" ORDER BY timestamp ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxTimestampFoundForSensor);
    		stmt.setLong(3, latestCompletePeriodEndTimestamp);
    		stmt.setLong(4, oldestPeriodStartTimestamp);
    		DBConnection.exec(stmt, new DataAggregator(sensorId, aggrPeriodLength, expectedSampleCount, aggrMethod, aggregationStartTime));
    	} catch (SQLException e) {
            logger.log(Level.SEVERE, null, e);
		}
    }
    
    /**
     * Internal class for aggregating data to the aggregated DB table
     */
    private class DataAggregator implements SQLResultHandler<Object>{
    	private final long sensorId;
    	private final AggregationPeriodLength aggrPeriodLength;
    	private final int expectedSampleCount;
    	private final AggregationMethod aggrMethod;
    	private final long aggregationStartTime;
    	
    	public DataAggregator(long sensorId, AggregationPeriodLength aggrPeriodLength, int expectedSampleCount, AggregationMethod aggrMethod, long aggregationStartTime) {
    		this.sensorId = sensorId;
    		this.aggrPeriodLength = aggrPeriodLength;
    		this.expectedSampleCount = expectedSampleCount;
    		this.aggrMethod = aggrMethod;
    		this.aggregationStartTime = aggregationStartTime;
		}
    	
		@Override
		public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			try{
				HalContext.getDB().getConnection().setAutoCommit(false);
				
				UTCTimePeriod currentPeriod = null;
				float sum = 0;
				float confidenceSum = 0;
				int samples = 0;
				long highestSequenceId = Sensor.getHighestSequenceId(sensorId);
				PreparedStatement preparedInsertStmt = HalContext.getDB().getPreparedStatement(
						"INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(?, ?, ?, ?, ?, ?)");
				while(result.next()){
					if(sensorId != result.getInt("sensor_id")){
						throw new IllegalArgumentException("found entry for aggregation for the wrong sensorId (expecting: "+sensorId+", but was: "+result.getInt("sensor_id")+")");
					}
					
					long timestamp = result.getLong("timestamp");
					UTCTimePeriod dataPeriod = new UTCTimePeriod(timestamp, this.aggrPeriodLength);
					
					if(currentPeriod == null)
						currentPeriod = dataPeriod;
					
					if(!dataPeriod.equals(currentPeriod)){
						saveData(preparedInsertStmt, confidenceSum, sum, samples, currentPeriod, ++highestSequenceId);
						
						// Reset variables
						currentPeriod = dataPeriod;
						confidenceSum = 0;
						sum = 0;
						samples = 0;
					}
					sum += result.getFloat("data");
					confidenceSum += result.getFloat("confidence");
					++samples;
				}
				
				//check if the last period is complete and also should be aggregated
				if(currentPeriod != null && currentPeriod.getEndTimestamp() <= new UTCTimePeriod(aggregationStartTime, aggrPeriodLength).getPreviosPeriod().getEndTimestamp()){
					saveData(preparedInsertStmt, confidenceSum, sum, samples, currentPeriod, ++highestSequenceId);
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
		
		private void saveData(PreparedStatement preparedInsertStmt, float confidenceSum, float sum, int samples, UTCTimePeriod currentPeriod, long sequenceId) throws SQLException{
			float aggrConfidence = confidenceSum / (float)this.expectedSampleCount;
			float data = -1;
			switch(aggrMethod){
				case SUM: data = sum; break;
				case AVERAGE: data = sum/samples; break;
			}
			logger.finer("saved period: " + currentPeriod + ", data: " + sum + ", confidence: " + aggrConfidence + ", samples: " + samples + ", aggrMethod: " + aggrMethod);

			preparedInsertStmt.setLong(1, sensorId);
			preparedInsertStmt.setLong(2, sequenceId);
			preparedInsertStmt.setLong(3, currentPeriod.getStartTimestamp());
			preparedInsertStmt.setLong(4, currentPeriod.getEndTimestamp());
			preparedInsertStmt.setFloat(5, data);
			preparedInsertStmt.setFloat(6, aggrConfidence);
			preparedInsertStmt.addBatch();
		}
		
    }

}
