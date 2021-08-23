package se.hal.daemon;

import se.hal.HalContext;
import se.hal.daemon.SensorDataAggregatorDaemon.AggregationPeriodLength;
import se.hal.intf.HalDaemon;
import se.hal.struct.Sensor;
import se.hal.util.UTCTimeUtility;
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

public class SensorDataCleanupDaemon implements HalDaemon, Runnable {
    private static final Logger logger = LogUtil.getLogger();

    public void initiate(ScheduledExecutorService executor){
        executor.scheduleAtFixedRate(this, 5000, UTCTimeUtility.FIVE_MINUTES_IN_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void run(){
        try {
            List<Sensor> sensorList = Sensor.getSensors(HalContext.getDB());
            for (Sensor sensor : sensorList){
                logger.fine("Deleting old aggregated data for sensor id: " + sensor.getId());
                cleanupSensor(sensor);
            }
            logger.fine("Data cleanup done");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Thread has crashed", e);
        }
    }

    public void cleanupSensor(Sensor sensor) {
        if (sensor.getUser() != null) {
            cleanupSensorData(sensor.getId(), AggregationPeriodLength.FIVE_MINUTES, UTCTimeUtility.DAY_IN_MS);	//clear 5-minute data older than a day
            cleanupSensorData(sensor.getId(), AggregationPeriodLength.HOUR, UTCTimeUtility.WEEK_IN_MS);			//clear 1-hour data older than a week
            //cleanupSensorData(sensor.getId(), AggregationPeriodLength.day, TimeUtility.INFINITY);			    //clear 1-day data older than infinity
            //cleanupSensorData(sensor.getId(), AggregationPeriodLength.week, TimeUtility.INFINITY);			//clear 1-week data older than infinity
        }
    }


    /**
     * Will clear periods if they are too old.
     *
     * @param sensorId
     * @Param cleanupPeriodLength Will clear periods with this length
     * @param olderThan Data must be older than this many ms to be cleared from the DB
     */
    private void cleanupSensorData(long sensorId, AggregationPeriodLength cleanupPeriodLength, long olderThan){
        DBConnection db = HalContext.getDB();
        PreparedStatement stmt = null;
        try {

            stmt = db.getPreparedStatement("SELECT * FROM sensor_data_aggr"
                    +" WHERE sensor_id == ? "
                        + "AND timestamp_end-timestamp_start == ?"
                        + "AND timestamp_end < ?");
            stmt.setLong(1, sensorId);
            switch(cleanupPeriodLength){
                case SECOND: stmt.setLong(2, UTCTimeUtility.SECOND_IN_MS-1); break;
                case MINUTE: stmt.setLong(2, UTCTimeUtility.MINUTE_IN_MS-1); break;
                case FIVE_MINUTES: stmt.setLong(2, UTCTimeUtility.FIVE_MINUTES_IN_MS-1); break;
                case FIFTEEN_MINUTES: stmt.setLong(2, UTCTimeUtility.FIFTEEN_MINUTES_IN_MS-1); break;
                case HOUR: stmt.setLong(2, UTCTimeUtility.HOUR_IN_MS-1); break;
                case DAY: stmt.setLong(2, UTCTimeUtility.DAY_IN_MS-1); break;
                case WEEK: stmt.setLong(2, UTCTimeUtility.WEEK_IN_MS-1); break;
                default: logger.warning("cleanup period length is not supported."); return;
            }
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
                while (result.next()){
                    if (sensorId != result.getInt("sensor_id")){
                        throw new IllegalArgumentException("Found entry for aggregation for the wrong sensorId (expecting: "+sensorId+", but was: "+result.getInt("sensor_id")+")");
                    }
                    logger.finer("Deleting sensor(id: "+ sensorId +") aggregate entry timestamp: "+ result.getLong("timestamp_start") +" - "+ result.getLong("timestamp_end") + " (" + UTCTimeUtility.timeInMsToString(1+result.getLong("timestamp_end")-result.getLong("timestamp_start")) + ")");
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
