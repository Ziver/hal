package se.koc.hal.struct;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import se.koc.hal.HalContext;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.db.handler.SimpleSQLResult;

/**
 * Created by Ziver on 2015-12-03.
 */
@DBBean.DBTable("sensor")
public class Sensor extends DBBean{
	private String name;
	private long user_id;
	private String type;
	private String config;
	private long external_id;

	
	public static List<Sensor> getExternalSensors(DBConnection db) throws SQLException{
		PreparedStatement stmt = db.getPreparedStatement( "SELECT sensor.* FROM sensor,user WHERE user.external == 1 AND user.id == sensor.user_id" );
		return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
	}
	
	public static List<Sensor> getLocalSensors(DBConnection db) throws SQLException{
		PreparedStatement stmt = db.getPreparedStatement( "SELECT sensor.* FROM sensor,user WHERE user.external == 0 AND user.id == sensor.user_id" );
		return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
	}

	public static List<Sensor> getSensors(DBConnection db, User user) throws SQLException{
		PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM sensor WHERE user_id == " + user.getId() );
		return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
	}

	
    public static long getHighestSequenceId(long sensorId) throws SQLException{
   	 Integer id = HalContext.getDB().exec("SELECT MAX(sequence_id) FROM sensor_data_aggr WHERE sensor_id == "+ sensorId, new SimpleSQLResult<Integer>());
   	 return (id != null ? id+1 : 1);
   }
	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public long getUserId() {
		return user_id;
	}
	public void setUserId(long user_id) {
		this.user_id = user_id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public long getExternalId() {
		return external_id;
	}
	public void setExternalId(long external_id) {
		this.external_id = external_id;
	}
}
