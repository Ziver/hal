package se.hal.struct;

import se.hal.HalContext;
import se.hal.intf.HalDeviceReportListener;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorData;
import se.hal.util.DeviceDataSqlResult;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.db.handler.SimpleSQLResult;
import zutil.log.LogUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


@DBBean.DBTable(value="sensor", superBean=true)
public class Sensor extends AbstractDevice<Sensor, HalSensorConfig,HalSensorData>{
    private static final Logger logger = LogUtil.getLogger();

    private long external_id = -1;
    /** local sensor= if sensor should be public. external sensor= if sensor should be requested from host **/
    private boolean sync = false;
    private long aggr_version;


    public static List<Sensor> getExternalSensors(DBConnection db) throws SQLException{
        PreparedStatement stmt = db.getPreparedStatement( "SELECT sensor.* FROM sensor,user WHERE user.external == 1 AND user.id == sensor.user_id" );
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
    }
    public static Sensor getExternalSensor(DBConnection db, User user, long external_id) throws SQLException{
        PreparedStatement stmt = db.getPreparedStatement( "SELECT sensor.* FROM sensor WHERE ? == sensor.user_id AND sensor.external_id == ?" );
        stmt.setLong(1, user.getId());
        stmt.setLong(2, external_id);
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.create(Sensor.class, db) );
    }

    public static List<Sensor> getLocalSensors(DBConnection db) throws SQLException{
        PreparedStatement stmt = db.getPreparedStatement( "SELECT sensor.* FROM sensor,user WHERE user.external == 0 AND user.id == sensor.user_id" );
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
    }

    public static List<Sensor> getSensors(DBConnection db, User user) throws SQLException{
        PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM sensor WHERE user_id == ?" );
        stmt.setLong(1, user.getId());
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
    }

    public static List<Sensor> getSensors(DBConnection db) throws SQLException{
        PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM sensor" );
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
    }

    public static Sensor getSensor(DBConnection db, long id) throws SQLException{
        return DBBean.load(db, Sensor.class, id);
    }

    public static long getHighestSequenceId(long sensorId) throws SQLException{
        PreparedStatement stmt = HalContext.getDB().getPreparedStatement("SELECT MAX(sequence_id) FROM sensor_data_aggr WHERE sensor_id == ?");
        stmt.setLong(1, sensorId);
        Integer id = DBConnection.exec(stmt, new SimpleSQLResult<Integer>());
        return (id != null ? id : 0);
    }


    /**
     * Will delete this Sensor and its aggregate data
     * (raw data will never be deleted as a safety precaution!)
     */
    @Override
    public void delete(DBConnection db) throws SQLException {
        clearAggregatedData(db);
        super.delete(db);
    }

    /**
     * Will clear all aggregated data for this Sensor and increment the AggregationVersion
     */
    public void clearAggregatedData(DBConnection db) throws SQLException{
        logger.fine("Clearing all aggregate data for sensor id: "+this.getId());
        PreparedStatement stmt = db.getPreparedStatement( "DELETE FROM sensor_data_aggr WHERE sensor_id == ?" );
        stmt.setLong(1, getId());
        DBConnection.exec(stmt);
        aggr_version++;
    }


    public long getExternalId() {
        return external_id;
    }
    public void setExternalId(long external_id) {
        this.external_id = external_id;
    }
    public boolean isSynced() {
        return sync;
    }
    public void setSynced(boolean synced) {
        this.sync = synced;
    }
    public long getAggregationVersion(){
        return this.aggr_version;
    }
    public void setAggregationVersion(long aggr_version){
        this.aggr_version = aggr_version;
    }


    @Override
    public Class<? extends HalSensorController> getController(){
        return getDeviceConfig().getSensorControllerClass();
    }

    @Override
    protected HalSensorData getLatestDeviceData(DBConnection db) {
        try {
            Class deviceDataClass = getDeviceConfig().getSensorDataClass();
            if (deviceDataClass == null)
                throw new ClassNotFoundException("Unknown sensor data class for: " + getDeviceConfig().getClass());

            if (getId() != null) {
                PreparedStatement stmt = db.getPreparedStatement(
                        "SELECT * FROM sensor_data_raw WHERE sensor_id == ? ORDER BY timestamp DESC LIMIT 1");
                stmt.setLong(1, getId());
                return (HalSensorData)
                        DBConnection.exec(stmt, new DeviceDataSqlResult(deviceDataClass));
            }
        } catch (Exception e){
            logger.log(Level.WARNING, null, e);
        }
        return null;
    }
}
