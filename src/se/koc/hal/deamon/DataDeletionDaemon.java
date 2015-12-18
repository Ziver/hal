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
import se.koc.hal.intf.HalDaemon;
import se.koc.hal.struct.HalSensor;
import se.koc.hal.util.TimeUtility;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.db.handler.SimpleSQLResult;
import zutil.log.LogUtil;

public class DataDeletionDaemon extends TimerTask implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();

    public void initiate(Timer timer){
        timer.schedule(this, 5000, TimeUtility.FIVE_MINUTES_IN_MS);
    }

    @Override
    public void run(){
    	try {
			List<HalSensor> sensorList = HalSensor.getSensors(HalContext.getDB());
			for(HalSensor sensor : sensorList){
				logger.fine("Deleting old data for sensor id: " + sensor.getId());
				cleanupSensor(sensor);
			}
            logger.fine("Data cleanup done");
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public void cleanupSensor(HalSensor sensor) {
    	logger.fine("The sensor is of type: " + sensor.getType());
    	if(sensor.getType().equals("PowerMeter")){	//TODO: use instanceof instead
    		if(sensor.getUser().isExternal()){
    			cleanupExternalSensorData(sensor.getId(), TimeUtility.FIVE_MINUTES_IN_MS, TimeUtility.DAY_IN_MS);
        		cleanupExternalSensorData(sensor.getId(), TimeUtility.DAY_IN_MS, TimeUtility.WEEK_IN_MS);
    		}else{
	    		cleanupInternalSensorData(sensor.getId(), TimeUtility.HOUR_IN_MS, TimeUtility.FIVE_MINUTES_IN_MS, TimeUtility.DAY_IN_MS);
	    		cleanupInternalSensorData(sensor.getId(), TimeUtility.DAY_IN_MS, TimeUtility.HOUR_IN_MS, TimeUtility.WEEK_IN_MS);
    		}
    	}else{
    		logger.fine("The sensor type is not supported by the cleanup deamon. Ignoring");
    	}
    }
    
    /**
     * Will clear periods only if it has been aggregated and are too old.
     * @param sensorId
     * @Param referencePeriodlength Will only clear periods older than the newest period of this length.
     * @Param clearPeriodlength Will clear periods with this length
     * @param olderThan Data must be older than this many ms to be cleared from the DB
     */
    private void cleanupInternalSensorData(long sensorId, long referencePeriodlength, long clearPeriodlength, long olderThan){
    	DBConnection db = HalContext.getDB();
    	PreparedStatement stmt = null;
    	try {
    		Long maxDBTimestamp = null; 
    		
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, referencePeriodlength-1);
    		maxDBTimestamp = DBConnection.exec(stmt, new SimpleSQLResult<Long>());
    		if(maxDBTimestamp == null)
    			maxDBTimestamp = 0l;

    		stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? "
	    				+ "AND timestamp_end < ? "
	    				+ "AND timestamp_end-timestamp_start == ?"
	    				+ "AND timestamp_end < ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, maxDBTimestamp);
    		stmt.setLong(3, clearPeriodlength-1);
    		stmt.setLong(4, System.currentTimeMillis()-olderThan);
    		DBConnection.exec(stmt, new AggregateDataDeleter(sensorId));
		} catch (SQLException e) {
			logger.log(Level.SEVERE, null, e);
		}
    }
    
    /**
     * Will clear periods if they are too old.
     * @param sensorId
     * @Param clearPeriodlength Will clear periods with this length
     * @param olderThan Data must be older than this many ms to be cleared from the DB
     */
    private void cleanupExternalSensorData(long sensorId, long clearPeriodlength, long olderThan){
    	DBConnection db = HalContext.getDB();
    	PreparedStatement stmt = null;
    	try {

    		stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? "
	    				+ "AND timestamp_end-timestamp_start == ?"
	    				+ "AND timestamp_end < ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, clearPeriodlength-1);
    		stmt.setLong(3, System.currentTimeMillis()-olderThan);
    		DBConnection.exec(stmt, new AggregateDataDeleter(sensorId));
		} catch (SQLException e) {
			logger.log(Level.SEVERE, null, e);
		}
    }
    
    private class AggregateDataDeleter implements SQLResultHandler<Long>{
    	private long sensorId = -1;
    	
    	public AggregateDataDeleter(long sensorId){
    		this.sensorId = sensorId;
    	}
    	
		@Override
		public Long handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			long count = 0;
			try{
				HalContext.getDB().getConnection().setAutoCommit(false);
				PreparedStatement preparedDeleteStmt = HalContext.getDB().getPreparedStatement("DELETE FROM sensor_data_aggr WHERE sensor_id == ? AND sequence_id == ?");
				while(result.next()){
					if(sensorId != result.getInt("sensor_id")){
						throw new IllegalArgumentException("Found entry for aggregation for the wrong sensorId (expecting: "+sensorId+", but was: "+result.getInt("sensor_id")+")");
					}
                    logger.finer("Deleting sensor aggregate entry timestamp: "+ result.getLong("timestamp_start") +" - "+ result.getLong("timestamp_end") + " (" + TimeUtility.msToString(result.getLong("timestamp_end")-result.getLong("timestamp_start")) + ")");
					preparedDeleteStmt.setInt(1, result.getInt("sensor_id"));
					preparedDeleteStmt.setLong(2, result.getLong("sequence_id"));
					preparedDeleteStmt.addBatch();
					count++;
				}

                DBConnection.execBatch(preparedDeleteStmt);
                HalContext.getDB().getConnection().commit();
			}catch(Exception e){
				HalContext.getDB().getConnection().rollback();
				throw e;
			}finally{
				HalContext.getDB().getConnection().setAutoCommit(true);
			}
			return count;
		}
    	
    }
        
}
