package se.hal.deamon;

import se.hal.HalContext;
import se.hal.intf.HalDaemon;
import se.hal.intf.HalSensorData;
import se.hal.struct.Sensor;
import se.hal.intf.HalSensorData.AggregationMethod;
import se.hal.util.TimeUtility;
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

    public void initiate(ScheduledExecutorService executor){
        executor.scheduleAtFixedRate(this, 0, TimeUtility.FIVE_MINUTES_IN_MS, TimeUnit.MILLISECONDS);
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
		
		logger.fine("aggregating raw data up to a day old into five minute periods");
		aggregateRawData(sensor, TimeUtility.FIVE_MINUTES_IN_MS, TimeUtility.DAY_IN_MS, 5);
		
		logger.fine("aggregating raw data up to a week old into five minute periods");
		aggregateRawData(sensor, TimeUtility.HOUR_IN_MS, TimeUtility.WEEK_IN_MS, 60);
		
		logger.fine("aggregating raw data into one day periods");
		aggregateRawData(sensor, TimeUtility.DAY_IN_MS, TimeUtility.INFINITY, 60*24);
		
		logger.fine("aggregating raw data into one week periods");
		aggregateRawData(sensor, TimeUtility.WEEK_IN_MS, TimeUtility.INFINITY, 60*24*7);
    }
    
    /**
     * Aggregate data from the raw DB table to the aggregated table
     * @param	sensor				The sensor for to aggregate data
     * @param   ageLimitInMs		Only aggregate up to this age
     * @param	toPeriodSizeInMs	The period length in ms to aggregate to
     */
    private void aggregateRawData(Sensor sensor, long toPeriodSizeInMs, long ageLimitInMs, int expectedSampleCount){
    	long sensorId = sensor.getId();
    	AggregationMethod aggrMethod = sensor.getDeviceData().getAggregationMethod();
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
    		
    		long currentPeriodStartTimestamp = TimeUtility.getTimestampPeriodStart_UTC(toPeriodSizeInMs, System.currentTimeMillis());
    		
    		logger.fine("Calculating periods... (from:"+ maxTimestampFoundForSensor +", to:"+ currentPeriodStartTimestamp +") with expected sample count: " + expectedSampleCount);
    		
    		stmt = db.getPreparedStatement("SELECT *, 1 AS confidence FROM sensor_data_raw"
    				+" WHERE sensor_id == ?"
    					+ " AND timestamp > ?"
    					+ " AND timestamp < ? "
    					+ " AND timestamp > ? "
    				+" ORDER BY timestamp ASC");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxTimestampFoundForSensor);
    		stmt.setLong(3, currentPeriodStartTimestamp);
    		stmt.setLong(4, System.currentTimeMillis()-ageLimitInMs);
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
				PreparedStatement preparedInsertStmt = HalContext.getDB().getPreparedStatement(
						"INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(?, ?, ?, ?, ?, ?)");
				while(result.next()){
					if(sensorId != result.getInt("sensor_id")){
						throw new IllegalArgumentException("found entry for aggregation for the wrong sensorId (expecting: "+sensorId+", but was: "+result.getInt("sensor_id")+")");
					}
					long timestamp = result.getLong("timestamp");
					long periodTimestamp = TimeUtility.getTimestampPeriodStart_UTC(this.aggrTimeInMs, timestamp);
					if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
						float aggrConfidence = confidenceSum / (float)this.expectedSampleCount;
						float data = -1;
						switch(aggrMethod){
							case SUM: data = sum; break;
							case AVERAGE: data = sum/samples; break;
						}
						logger.finer("Calculated period starting at timestamp: " + currentPeriodTimestamp + ", data: " + sum + ", confidence: " + aggrConfidence + ", samples: " + samples + ", aggrMethod: " + aggrMethod);
						preparedInsertStmt.setInt(1, result.getInt("sensor_id"));
						preparedInsertStmt.setLong(2, ++highestSequenceId);
						preparedInsertStmt.setLong(3, currentPeriodTimestamp);
						preparedInsertStmt.setLong(4, currentPeriodTimestamp + this.aggrTimeInMs - 1);
						preparedInsertStmt.setFloat(5, data);
						preparedInsertStmt.setFloat(6, aggrConfidence);
						preparedInsertStmt.addBatch();
						
						// Reset variables
						currentPeriodTimestamp = periodTimestamp;
						confidenceSum = 0;
						sum = 0;
						samples = 0;
					}
					if(currentPeriodTimestamp == 0) currentPeriodTimestamp = periodTimestamp;
					sum += result.getInt("data");
					confidenceSum += result.getFloat("confidence");
					++samples;
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
