package se.koc.hal.struct;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;

/**
 * Created by Ziver on 2015-12-03.
 */
@DBBean.DBTable("sensor")
public class Sensor extends DBBean{
	private String name;
	private int user_id;
	private String type;
	private String config;

	
	public static List<Sensor> getExternalSensors(DBConnection db) throws SQLException{
		PreparedStatement stmt = db.getPreparedStatement( "SELECT sensor.* FROM sensor,user WHERE user.external == 1 AND user.id == sensor.user_id" );
		return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
	}
	
	public static List<Sensor> getLocalSensors(DBConnection db) throws SQLException{
		PreparedStatement stmt = db.getPreparedStatement( "SELECT sensor.* FROM sensor,user WHERE user.external == 0 AND user.id == sensor.user_id" );
		return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Sensor.class, db) );
	}

	
	
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getUser_id() {
		return user_id;
	}
	public void setUser_id(int user_id) {
		this.user_id = user_id;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
