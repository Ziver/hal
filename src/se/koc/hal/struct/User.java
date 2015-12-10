package se.koc.hal.struct;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.ui.Configurator;

/**
 * Created by Ziver on 2015-12-03.
 */
@DBBean.DBTable("user")
public class User extends DBBean{

    private String username;
    private String address;
    private int external;

    private String hostname;
    private int port;
    
    
    
	public static List<User> getExternalUsers(DBConnection db) throws SQLException{
		PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM user WHERE user.external == 1" );
		return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(User.class, db) );
	}
	
	public static User getLocalUser(DBConnection db) throws SQLException{
		PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM user WHERE user.external == 0" );
		return DBConnection.exec(stmt, DBBeanSQLResultHandler.create(User.class, db) );
	}

	
	
	
	
	public String getUserName() {
		return username;
	}
	public void setUserName(String name) {
		this.username = name;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public boolean isExternal() {
		return external > 0;
	}
	public void setExternal(boolean external) {
		this.external = (external? 1:0 );
	}
	public String getHostname() {
		return hostname;
	}
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}

}
