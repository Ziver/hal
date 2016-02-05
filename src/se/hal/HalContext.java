package se.hal;

import zutil.db.DBConnection;
import zutil.db.DBUpgradeHandler;
import zutil.db.SQLResultHandler;
import zutil.db.handler.PropertiesSQLResult;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;

import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

import se.hal.struct.Sensor;

public class HalContext {
    private static final Logger logger = LogUtil.getLogger();

    // Constants
    private static final String PROPERTY_DB_VERSION = "db_version";

    private static final String CONF_FILE = "hal.conf";
    private static final String DB_FILE = "hal.db";
    private static final String DEFAULT_DB_FILE = "hal-default.db";

    // Variables
    private static DBConnection db; // TODO: Should probably be a db pool as we have multiple threads accessing the DB

    private static Properties defaultFileConf;
	private static Properties fileConf;
	private static Properties dbConf;


	static {
		defaultFileConf = new Properties();
		defaultFileConf.setProperty("http_port", ""+8080);
		defaultFileConf.setProperty("sync_port", ""+6666);
	}
	
	
	public static void initialize(){
        try {
            // Read conf
            fileConf = new Properties(defaultFileConf);
            if (FileUtil.find(CONF_FILE) != null) {
                FileReader in = new FileReader(CONF_FILE);
                fileConf.load(in);
                in.close();
            }
            else logger.info("No hal.conf file found");

            // Init DB
            File dbFile = FileUtil.find(DB_FILE);
            if(dbFile == null){
                logger.info("Creating new DB...");
                dbFile = new File(DB_FILE);
                dbFile.createNewFile();
                FileUtil.copy(FileUtil.find(DEFAULT_DB_FILE), dbFile);
            }
            db = new DBConnection(DBConnection.DBMS.SQLite, DB_FILE);

            // Read DB conf
            dbConf = db.exec("SELECT * FROM conf", new PropertiesSQLResult());


            // Upgrade DB needed?
            DBConnection referenceDB = new DBConnection(DBConnection.DBMS.SQLite, DEFAULT_DB_FILE);
            Properties defaultDBConf =
                    referenceDB.exec("SELECT * FROM conf", new PropertiesSQLResult());
            // Check DB version
            logger.info("DB version: "+ dbConf.getProperty(PROPERTY_DB_VERSION));
            final int defaultDBVersion = Integer.parseInt(defaultDBConf.getProperty(PROPERTY_DB_VERSION));
            final int dbVersion = (dbConf.getProperty(PROPERTY_DB_VERSION) != null ?
                                Integer.parseInt(dbConf.getProperty(PROPERTY_DB_VERSION)) :
                                -1);
            if(defaultDBVersion > dbVersion ) {
                logger.info("Starting DB upgrade...");
                File backupDB = FileUtil.getNextFile(dbFile);
                logger.fine("Backing up DB to: "+ backupDB);
                FileUtil.copy(dbFile, backupDB);

                logger.fine(String.format("Upgrading DB (from: v%s, to: v%s)...", dbVersion, defaultDBVersion));
                final DBUpgradeHandler handler = new DBUpgradeHandler(referenceDB);
                handler.addIgnoredTable("db_version_history");
                handler.setTargetDB(db);
                
                //read upgrade path preferences from the reference database
                referenceDB.exec("SELECT * FROM db_version_history"
                		+ " WHERE db_version <= " + defaultDBVersion
                		+ " AND db_version > " + dbVersion, 
                		new SQLResultHandler<Object>() {
					@Override
					public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
						while(result.next()){
							if(result.getBoolean("force_upgrade")){
                                logger.fine("Forced upgrade enabled");
								handler.setForcedDBUpgrade(true);	//set to true if any of the intermediate db version requires it.
							}
						}
						return null;
					}
				});
                
                handler.upgrade();
                
                logger.fine("Performing post-upgrade activities");
                //read upgrade path preferences from the reference database
                referenceDB.exec("SELECT * FROM db_version_history"
                		+ " WHERE db_version <= " + defaultDBVersion
                		+ " AND db_version > " + dbVersion, 
                		new SQLResultHandler<Object>() {
					@Override
					public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
						while(result.next()){
							if(result.getBoolean("clear_external_aggr_data")){
                                logger.fine("Clearing external aggregate data");
								db.exec("DELETE FROM sensor_data_aggr WHERE sensor_id = "
										+ "(SELECT sensor.id FROM user, sensor WHERE user.external == 1 AND sensor.user_id = user.id)");
							}
							if(result.getBoolean("clear_internal_aggr_data")){
                                logger.fine("Clearing local aggregate data");
								db.exec("DELETE FROM sensor_data_aggr WHERE sensor_id = "
										+ "(SELECT sensor.id FROM user, sensor WHERE user.external == 0 AND sensor.user_id = user.id)");
								//update all internal sensors aggregation version to indicate for peers that they need to re-sync all data
								db.exec("UPDATE sensor SET aggr_version = (aggr_version+1) WHERE id = "
										+ "(SELECT sensor.id FROM user, sensor WHERE user.external == 0 AND sensor.user_id = user.id)");
							}
						}
						return null;
					}
				});

                logger.info("DB upgrade done");
                dbConf.setProperty(PROPERTY_DB_VERSION, defaultDBConf.getProperty(PROPERTY_DB_VERSION));
                storeProperties();
            }
            referenceDB.close();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
	}


    public static String getStringProperty(String key){
        String value = null;
        if (fileConf != null)
            value = fileConf.getProperty(key);
        if (dbConf != null && value == null)
            value = dbConf.getProperty(key);
        return value;
    }
    public static int getIntegerProperty(String key){
        return Integer.parseInt(getStringProperty(key));
    }
    public synchronized static void storeProperties() throws SQLException {
        logger.fine("Saving conf to DB...");
        PreparedStatement stmt = db.getPreparedStatement("REPLACE INTO conf (key, value) VALUES (?, ?)");
        for(Object key : dbConf.keySet()){
            stmt.setObject(1, key);
            stmt.setObject(2, dbConf.get(key));
            stmt.addBatch();
        }
        DBConnection.execBatch(stmt);
    }

    public static DBConnection getDB(){
        return db;
    }

}
