package se.hal.daemon;

import java.sql.PreparedStatement;

import org.junit.Before;
import org.junit.Test;

import se.hal.HalContext;
import se.hal.plugin.netscan.NetworkDevice;
import se.hal.util.UTCTimeUtility;
import zutil.db.DBConnection;
import zutil.db.DBUpgradeHandler;
import zutil.log.LogUtil;

public class SensorDataAggregationDeamonTest {
	private static final String DEFAULT_DB_FILE = "hal-default.db";

	private static DBConnection db;
	
	@Before
	public void setupDatabase() throws Exception{
		System.out.println("-----------------------------------------------");
		
		//setup loggin
		System.out.println("Setting up logging");
		LogUtil.readConfiguration("logging.properties");
		
		//create in memory database
		System.out.println("Creating a in-memory databse for test");
		db = new DBConnection(DBConnection.DBMS.SQLite, ":memory:");
		HalContext.setDB(db);
		
		//upgrade the database to latest version
		System.out.println("Upgrading in-memory databse to latest version");
		DBConnection referenceDB = new DBConnection(DBConnection.DBMS.SQLite, DEFAULT_DB_FILE);
		final DBUpgradeHandler handler = new DBUpgradeHandler(referenceDB);
		handler.addIgnoredTable("db_version_history");
        handler.addIgnoredTable("sqlite_sequence");	//sqlite internal
        handler.setTargetDB(db);
		handler.upgrade();
		
		//populate the database with data
		System.out.println("Adding user to database");
		db.exec("INSERT INTO user(id, external, username) VALUES(222, 0, 'test')");	//adding user
		System.out.println("Adding sensor to database");
		db.exec("INSERT INTO sensor(id, user_id, external_id, type) VALUES(111, 222, 333, '"+NetworkDevice.class.getName()+"')");	//adding sensor
		System.out.println("Generating raw data and saving it to the database...");
		PreparedStatement stmt = db.getPreparedStatement("INSERT INTO sensor_data_raw (timestamp, sensor_id, data) VALUES(?, ?, ?)");
		try{
			db.getConnection().setAutoCommit(false);
			
			long startTime = System.currentTimeMillis();
			for(int i = 0; i < 100000; ++i){
				stmt.setLong(1, startTime-(UTCTimeUtility.MINUTE_IN_MS*i));
				stmt.setLong(2, 111);
				stmt.setFloat(3, 7.323f);
				stmt.addBatch();
			}
			
			DBConnection.execBatch(stmt);
			db.getConnection().commit();
		}catch(Exception e){
			db.getConnection().rollback();
			throw e;
		}finally{
			db.getConnection().setAutoCommit(true);
		}
		
		System.out.println("Ready for test!");
	}
	
	@Test
	public void testAggregation(){
		System.out.println("Start testing raw data aggregation");
		SensorDataAggregatorDaemon aggrDeamon = new SensorDataAggregatorDaemon();
		aggrDeamon.run();
		
		//TODO: verify the aggregation
		
		System.out.println("Finished testing raw data aggregation");
	}
	
	
}
