package se.koc.hal.struct;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import se.koc.hal.HalContext;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.db.handler.SimpleSQLResult;

@DBBean.DBTable("sensor")
public class Sensor extends DBBean{
	// Sensor specific data
	private String name;
	private String type;
	private String config;

	// User configuration
	private long user_id;
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

	public long getUserId() {
		return user_id;
	}
	public void setUserId(User user) {
		this.user_id = user.getId();
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
}
