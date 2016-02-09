package se.hal.deamon;

import se.hal.HalContext;
import se.hal.intf.HalDaemon;
import se.hal.struct.Sensor;
import se.hal.util.TimeUtility;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
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

public class SensorDataCleanupDaemon implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();

    public void initiate(ScheduledExecutorService executor){
		executor.scheduleAtFixedRate(this, 5000, TimeUtility.FIVE_MINUTES_IN_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run(){
    	try {
			List<Sensor> sensorList = Sensor.getSensors(HalContext.getDB());
			for(Sensor sensor : sensorList){
				logger.fine("Deleting old data for sensor id: " + sensor.getId());
				cleanupSensor(sensor);
			}
            logger.fine("Data cleanup done");
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Thread has crashed", e);
		}
    }

    public void cleanupSensor(Sensor sensor) {
    	if (sensor.getUser() != null) {
			cleanupSensorData(sensor.getId(), TimeUtility.FIVE_MINUTES_IN_MS, TimeUtility.DAY_IN_MS);	//clear 5-minute data older than a day
			cleanupSensorData(sensor.getId(), TimeUtility.HOUR_IN_MS, TimeUtility.WEEK_IN_MS);			//clear 1-hour data older than a week
			//cleanupSensorData(sensor.getId(), TimeUtility.DAY_IN_MS, TimeUtility.INFINITY);			//clear 1-day data older than infinity
			//cleanupSensorData(sensor.getId(), TimeUtility.WEEK_IN_MS, TimeUtility.INFINITY);			//clear 1-week data older than infinity
		}
    }

    
    /**
     * Will clear periods if they are too old.
     *
     * @param sensorId
     * @Param clearPeriodlength Will clear periods with this length
     * @param olderThan Data must be older than this many ms to be cleared from the DB
     */
    private void cleanupSensorData(long sensorId, long clearPeriodlength, long olderThan){
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
                    logger.finer("Deleting sensor(id: "+ sensorId +") aggregate entry timestamp: "+ result.getLong("timestamp_start") +" - "+ result.getLong("timestamp_end") + " (" + TimeUtility.msToString(result.getLong("timestamp_end")-result.getLong("timestamp_start")) + ")");
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
