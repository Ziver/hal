package se.koc.hal.struct;

import se.koc.hal.HalContext;
import se.koc.hal.intf.HalEvent;
import se.koc.hal.intf.HalSensor;
import se.koc.hal.intf.HalSensorController;
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

@DBBean.DBTable("sensor")
public class Sensor extends DBBean{
    private static final Logger logger = LogUtil.getLogger();

    // Sensor specific data
	private String name;
	private String type;
	private String config;
    // Sensor specific data
    private transient HalSensor sensorData;

	// User configuration
    @DBColumn("user_id")
	private User user;
	private long external_id = -1;
    /** local sensor= if sensor should be public. external sensor= if sensor should be requested from host **/
    private boolean sync = false;



	public static List<Sensor> getExternalSensors(DBConnection db) throws SQLException{
		PreparedStatement stmt = db.getPreparedStatement( "SELECT sensor.* FROM sensor,user WHERE user.external == 1 AND user.id == sensor.user_id" );
		return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
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

    public static Sensor getSensor(DBConnection db, int id) throws SQLException{
        return DBBean.load(db, Sensor.class, id);
    }

    public static long getHighestSequenceId(long sensorId) throws SQLException{
    	PreparedStatement stmt = HalContext.getDB().getPreparedStatement("SELECT MAX(sequence_id) FROM sensor_data_aggr WHERE sensor_id == ?");
    	stmt.setLong(1, sensorId);
    	Integer id = DBConnection.exec(stmt, new SimpleSQLResult<Integer>());
    	return (id != null ? id+1 : 1);
   }


    private HalSensor getSensorData(){
        if(sensorData == null) {
            try {
                Class c = Class.forName(type);
                sensorData = (HalSensor) c.newInstance();
            } catch (Exception e){
                logger.log(Level.SEVERE, null, e);
            }
        }
        return sensorData;
    }
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getConfig() {
		return config;
	}
	public void setConfig(String config) {
		this.config = config;
	}

	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
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


    public HalSensor.AggregationMethod getAggregationMethod(){
        return getSensorData().getAggregationMethod();
    }

	public Class<? extends HalSensorController> getController(){
		return getSensorData().getController();
	}
}
