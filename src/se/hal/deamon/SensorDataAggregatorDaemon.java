package se.hal.deamon;

import se.hal.HalContext;
import se.hal.intf.HalDaemon;
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
    	//if(sensor.getSensorData() instanceof PowerConsumptionSensorData){
    		logger.fine("The sensor is of type: " + sensor.getDeviceData().getClass().getName());
    		logger.fine("aggregating raw data to five minute periods");
			aggregateRawData(sensor, TimeUtility.FIVE_MINUTES_IN_MS, 5);
			logger.fine("aggregating five minute periods into hour periods");
			aggregateAggregatedData(sensor, TimeUtility.FIVE_MINUTES_IN_MS, TimeUtility.HOUR_IN_MS);
			logger.fine("aggregating one hour periods into one day periods");
			aggregateAggregatedData(sensor, TimeUtility.HOUR_IN_MS, TimeUtility.DAY_IN_MS);
    	//}else{
    	//	logger.fine("The sensor type("+ sensor.getSensorData().getClass().getName() +") is not supported by the aggregation daemon. Ignoring");
    	//}
    }
    
    /**
     * Aggregate data from the raw DB table to the aggregated table
     * @param	sensor				The sensor for to aggregate data
     * @param	toPeriodSizeInMs	The period length in ms to aggregate to
     */
    private void aggregateRawData(Sensor sensor, long toPeriodSizeInMs, int expectedSampleCount){
    	long sensorId = sensor.getId();
    	AggregationMethod aggrMethod = sensor.getAggregationMethod();
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
    		stmt = db.getPreparedStatement("SELECT *, 1 AS confidence, timestamp AS timestamp_start FROM sensor_data_raw"
    				+" WHERE sensor_id == ?"
    					+ " AND ? < timestamp_start"
    					+ " AND timestamp_start < ? "
    				+" ORDER BY timestamp_start ASC");
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
     * @param	sensor				The sensor for to aggregate data
     * @param	fromPeriodSizeInMs	The period length in ms to aggregate from
     * @param	toPeriodSizeInMs	The period length in ms to aggregate to
     */
    private void aggregateAggregatedData(Sensor sensor, long fromPeriodSizeInMs, long toPeriodSizeInMs){
    	long sensorId = sensor.getId();
    	AggregationMethod aggrMethod = sensor.getAggregationMethod();
    	int expectedSampleCount = (int)Math.ceil((double)toPeriodSizeInMs / (double)fromPeriodSizeInMs);
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
    		long currentPeriodStartTimestamp = TimeUtility.getTimestampPeriodStart_UTC(toPeriodSizeInMs, System.currentTimeMillis());
    		logger.fine("Calculating periods... (from:"+ maxTimestampFoundForSensor +", to:"+ currentPeriodStartTimestamp +") with expected sample count: " + expectedSampleCount);

    		stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr"
    				+" WHERE sensor_id == ?"
	    				+ " AND ? < timestamp_start"
	    				+ " AND timestamp_start <= ?"
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
				PreparedStatement preparedInsertStmt = HalContext.getDB().getPreparedStatement(
						"INSERT INTO sensor_data_aggr(sensor_id, sequence_id, timestamp_start, timestamp_end, data, confidence) VALUES(?, ?, ?, ?, ?, ?)");
				while(result.next()){
					if(sensorId != result.getInt("sensor_id")){
						throw new IllegalArgumentException("found entry for aggregation for the wrong sensorId (expecting: "+sensorId+", but was: "+result.getInt("sensor_id")+")");
					}
					long timestamp = result.getLong("timestamp_start");
					long periodTimestamp = TimeUtility.getTimestampPeriodStart_UTC(this.aggrTimeInMs, timestamp);
					if(currentPeriodTimestamp != 0 && periodTimestamp != currentPeriodTimestamp){
						float aggrConfidence = confidenceSum / (float)this.expectedSampleCount;
						float data = -1;
						switch(aggrMethod){
							case SUM: data = sum; break;
							case AVERAGE: data = sum/samples; break;
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
