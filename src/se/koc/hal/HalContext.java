package se.koc.hal;

import zutil.db.DBConnection;

public class HalContext {
	
	public static DBConnection db;
	
	
	
	
	
	public static void initialize() throws Exception{
		db = new DBConnection(DBConnection.DBMS.SQLite, "hal.db");
	}
}
