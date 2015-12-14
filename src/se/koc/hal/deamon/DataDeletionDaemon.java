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

public class DataDeletionDaemon extends TimerTask implements HalDaemon {
	private static final Logger logger = LogUtil.getLogger();

    public void initiate(Timer timer){
        timer.schedule(this, 5000, TimeUtility.FIVE_MINUTES_IN_MS);
    }

    @Override
    public void run(){
    	try {
			List<Sensor> sensorList = Sensor.getSensors(HalContext.getDB());
			for(Sensor sensor : sensorList){
				logger.fine("Deleting old data for sensor id: " + sensor.getId());
				cleanupSensor(sensor.getId());
			}
            logger.fine("Data cleanup done");
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }

    public void cleanupSensor(long sensorId) {
    	DBConnection db = HalContext.getDB();
    	PreparedStatement stmt = null;
    	try {
    		Long maxDBTimestamp = null; 
    		
    		// delete too old 5 minute periods that already have been aggregated into hours
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, TimeUtility.HOUR_IN_MS-1);
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
    		stmt.setLong(3, TimeUtility.FIVE_MINUTES_IN_MS-1);
    		stmt.setLong(4, System.currentTimeMillis()-TimeUtility.DAY_IN_MS);
    		DBConnection.exec(stmt, new AggregateDataDeleter(sensorId));
    		
    		// delete too old 1 hour periods that already have been aggregated into days
    		stmt = db.getPreparedStatement("SELECT MAX(timestamp_end) FROM sensor_data_aggr"
    				+" WHERE sensor_id == ? AND timestamp_end-timestamp_start == ?");
    		stmt.setLong(1, sensorId);
    		stmt.setLong(2, TimeUtility.DAY_IN_MS-1);
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
    		stmt.setLong(3, TimeUtility.HOUR_IN_MS-1);
    		stmt.setLong(4, System.currentTimeMillis()-TimeUtility.WEEK_IN_MS);
    		DBConnection.exec(stmt, new AggregateDataDeleter(sensorId));
		} catch (SQLException e) {
			logger.log(Level.SEVERE, null, e);
		}
    }
    
    private class AggregateDataDeleter implements SQLResultHandler<Object>{
    	private long sensorId = -1;
    	
    	public AggregateDataDeleter(long sensorId){
    		this.sensorId = sensorId;
    	}
    	
		@Override
		public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			try{
				HalContext.getDB().getConnection().setAutoCommit(false);
				PreparedStatement preparedDeleteStmt = HalContext.getDB().getPreparedStatement("DELETE FROM sensor_data_aggr WHERE sensor_id == ? AND sequence_id == ?");
				while(result.next()){
					if(sensorId != result.getInt("sensor_id")){
						throw new IllegalArgumentException("Found entry for aggregation for the wrong sensorId (expecting: "+sensorId+", but was: "+result.getInt("sensor_id")+")");
					}
                    logger.finer("Deleting sensor aggregate entry timestamp: "+ result.getLong("timestamp_start") +" - "+ result.getLong("timestamp_end"));
					preparedDeleteStmt.setInt(1, result.getInt("sensor_id"));
					preparedDeleteStmt.setLong(2, result.getLong("sequence_id"));
					preparedDeleteStmt.addBatch();
				}

                DBConnection.execBatch(preparedDeleteStmt);
                HalContext.getDB().getConnection().commit();
			}catch(Exception e){
				HalContext.getDB().getConnection().rollback();
			}finally{
				HalContext.getDB().getConnection().setAutoCommit(true);
			}
			return null;
		}
    	
    }
        
}
