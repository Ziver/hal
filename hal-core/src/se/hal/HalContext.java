package se.hal;

import se.hal.struct.User;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.db.DBUpgradeHandler;
import zutil.db.SQLResultHandler;
import zutil.db.handler.PropertiesSQLResult;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;

import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;


public class HalContext {
    private static final Logger logger = LogUtil.getLogger();

    // Constants
    public static final String PROPERTY_DB_VERSION = "hal.db_version";
    public static final String PROPERTY_HTTP_PORT = "hal.http_port";
    public static final String PROPERTY_MAP_BACKGROUND_IMAGE = "hal.map_bgimage";

    public static final String RESOURCE_ROOT;
    static {
        if (FileUtil.find("build/resources/") != null)
            RESOURCE_ROOT = "build/resources";
        else if (FileUtil.find("resource/resource/") != null)
            RESOURCE_ROOT = "resource";
        else
            RESOURCE_ROOT = ".";
    }

    public static final String RESOURCE_WEB_ROOT = HalContext.RESOURCE_ROOT + "/resource/web";

    private static final String CONF_FILE       = "hal.conf";
    private static final String DB_FILE         = "hal.db";
    private static final String DEFAULT_DB_FILE = "hal-default.db";

    // Variables
    private static DBConnection db; // TODO: Should probably be a db pool as we have multiple threads accessing the DB

    private static HashMap<String,String> registeredConf = new HashMap<>();
    private static Properties fileConf = new Properties();
    private static Properties dbConf = new Properties();;


    static {
        // Set default values to get Hal up and running
        fileConf.setProperty(PROPERTY_HTTP_PORT, "" + 8080);
    }


    public static void initialize(){
        try {
            // Read conf
            if (FileUtil.find(CONF_FILE) != null) {
                FileReader in = new FileReader(CONF_FILE);
                fileConf.load(in);
                in.close();
            } else {
                logger.info("No hal.conf file found");
            }

            if (FileUtil.find(DEFAULT_DB_FILE) == null){
                logger.severe("Unable to find default DB: " + DEFAULT_DB_FILE);
                System.exit(1);
            }

            // Init DB
            File dbFile = FileUtil.find(DB_FILE);
            db = new DBConnection(DBConnection.DBMS.SQLite, DB_FILE);

            if(dbFile == null){
                logger.info("No database file found, creating new DB...");
            } else {
                dbConf = db.exec("SELECT * FROM conf", new PropertiesSQLResult());
            }

            // Upgrade DB needed?
            DBConnection referenceDB = new DBConnection(DBConnection.DBMS.SQLite, DEFAULT_DB_FILE);
            Properties defaultDBConf =
                    referenceDB.exec("SELECT * FROM conf", new PropertiesSQLResult());

            // Check DB version
            final int defaultDBVersion = Integer.parseInt(defaultDBConf.getProperty(PROPERTY_DB_VERSION));
            final int dbVersion = (dbConf.getProperty(PROPERTY_DB_VERSION) != null ?
                                Integer.parseInt(dbConf.getProperty(PROPERTY_DB_VERSION)) :
                                -1);
            logger.info("DB version: "+ dbVersion);

            if(defaultDBVersion > dbVersion ) {
                logger.info("Starting DB upgrade from v" + dbVersion + " to v" + defaultDBVersion + "...");
                if(dbFile != null){
                    File backupDB = FileUtil.getNextFile(dbFile);
                    logger.fine("Backing up DB to: "+ backupDB);
                    FileUtil.copy(dbFile, backupDB);
                }

                logger.fine(String.format("Upgrading DB (from: v%s, to: v%s)...", dbVersion, defaultDBVersion));
                final DBUpgradeHandler handler = new DBUpgradeHandler(referenceDB);
                handler.addIgnoredTable("db_version_history");
                handler.addIgnoredTable("sqlite_sequence");	// sqlite internal
                handler.setTargetDB(db);

                logger.fine("Performing pre-upgrade activities");

                // Read upgrade path preferences from the reference database
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

                // Read upgrade path preferences from the reference database
                referenceDB.exec("SELECT * FROM db_version_history"
                        + " WHERE db_version <= " + defaultDBVersion
                        + " AND db_version > " + dbVersion,
                        new SQLResultHandler<Object>() {
                    @Override
                    public Object handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
                        boolean clearExternalAggrData = false;
                        boolean clearInternalAggrData = false;

                        while(result.next()){
                            if(result.getBoolean("clear_external_aggr_data"))
                                clearExternalAggrData = true;
                            if(result.getBoolean("clear_internal_aggr_data"))
                                clearInternalAggrData = true;
                        }

                        if(clearExternalAggrData){
                            logger.fine("Clearing external aggregate data");
                            db.exec("DELETE FROM sensor_data_aggr WHERE sensor_id = "
                                    + "(SELECT sensor.id FROM user, sensor WHERE user.external == 1 AND sensor.user_id = user.id)");
                        }
                        if(clearInternalAggrData){
                            logger.fine("Clearing local aggregate data");
                            db.exec("DELETE FROM sensor_data_aggr WHERE sensor_id IN "
                                    + "(SELECT sensor.id FROM user, sensor WHERE user.external == 0 AND sensor.user_id = user.id)");
                            //update all internal sensors aggregation version to indicate for peers that they need to re-sync all data
                            db.exec("UPDATE sensor SET aggr_version = (aggr_version+1) WHERE id = "
                                    + "(SELECT sensor.id FROM user, sensor WHERE user.external == 0 AND sensor.user_id = user.id)");
                        }
                        return null;
                    }
                });

                // Check if there is a local user
                User localUser = User.getLocalUser(db);
                if (localUser == null){
                    logger.fine("Creating local user.");
                    localUser = new User();
                    localUser.setExternal(false);
                    localUser.save(db);
                }

                logger.info("DB upgrade done");
                setProperty(PROPERTY_DB_VERSION, defaultDBConf.getProperty(PROPERTY_DB_VERSION));
            }
            referenceDB.close();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }



    public static Map<String,String> getProperties() {
        HashMap map = new HashMap();
        map.putAll(registeredConf);
        map.putAll(dbConf);
        map.putAll(fileConf);
        return map;

    }
    public static void registerProperty(String key) {
        registeredConf.put(key, "");
    }

    public static boolean containsProperty(String key) {
        return !ObjectUtil.isEmpty(getStringProperty(key));
    }
    public static String getStringProperty(String key){
        registerProperty(key);

        String value = null;
        if (fileConf != null)
            value = fileConf.getProperty(key);
        if (dbConf != null && value == null)
            value = dbConf.getProperty(key);
        return value;
    }
    public static String getStringProperty(String key, String defaultValue){
        if (!HalContext.containsProperty(key))
            return defaultValue;
        return getStringProperty(key);
    }

    public static int getIntegerProperty(String key){
        String value = getStringProperty(key);

        if (getStringProperty(key) == null)
            return 0;
        return Integer.parseInt(value);
    }
    public static int getIntegerProperty(String key, int defaultValue){
        if (!HalContext.containsProperty(key))
            return defaultValue;
        return getIntegerProperty(key);
    }

    public static boolean getBooleanProperty(String key) {
        return Boolean.parseBoolean(getStringProperty(key));
    }
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        if (!HalContext.containsProperty(key))
            return defaultValue;
        return getBooleanProperty(key);
    }

    public static void setProperty(String key, String value) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement("REPLACE INTO conf (key, value) VALUES (?, ?)");
        stmt.setObject(1, key);
        stmt.setObject(2, value);
        DBConnection.exec(stmt);
        dbConf.setProperty(key, value);
    }




    public static DBConnection getDB(){
        return db;
    }

    /**
     * For testing purposes.
     */
    public static void setDB(DBConnection db){
        HalContext.db = db;
    }


}
