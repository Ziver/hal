package se.koc.hal;

import zutil.db.DBConnection;
import zutil.db.handler.PropertiesSQLHandler;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public class HalContext {
    private static final Logger logger = LogUtil.getLogger();

    // Constants
    private static final String CONF_FILE = "hal.conf";
    private static final String DB_FILE = "hal.db";
    private static final String DEFAULT_DB_FILE = "hal-default.db";

    // Variables
    private static DBConnection db;

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
            FileReader in = new FileReader(CONF_FILE);
            fileConf.load(in);
            in.close();

            // Init DB
            if(FileUtil.find(DB_FILE) == null){
                logger.info("Creating new DB...");
                resetDB();
            }
            db = new DBConnection(DBConnection.DBMS.SQLite, DB_FILE);

            // Read DB conf
            dbConf = new Properties();
            db.exec("SELECT * FROM conf", new PropertiesSQLHandler(dbConf));

            // Upgrade DB needed?
            DBConnection defaultDB = new DBConnection(DBConnection.DBMS.SQLite, DEFAULT_DB_FILE);
            Properties defaultDBConf =
                    db.exec("SELECT * FROM conf", new PropertiesSQLHandler(new Properties()));
            if(defaultDBConf.getProperty("db_version").compareTo(dbConf.getProperty("db_version")) > 0) {
                logger.info("Upgrading DB (from: v"+dbConf.getProperty("db_version") +", to: v"+defaultDBConf.getProperty("db_version"));
                upgradeDB();
            }
            defaultDB.close();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
	}


    public static String getStringProperty(String key){
        String value = fileConf.getProperty(key);
        //if(value == null) // TODO: DB property
        //    value = dbConf.getProperty(key);
        return value;
    }
    public static int getIntegerProperty(String key){
        return Integer.parseInt(getStringProperty(key));
    }

    public static DBConnection getDB(){
        return db;
    }


    /*************************************************************************/

    private static void resetDB() throws IOException {
        FileUtil.copy(DEFAULT_DB_FILE, DB_FILE);
    }

    // Todo:
    private static void upgradeDB() throws SQLException {
        /*
        - beginTransaction
        - run a table creation with if not exists (we are doing an upgrade, so the table might not exists yet, it will fail alter and drop)
        - put in a list the existing columns List<String> columns = DBUtils.GetColumns(db, TableName);
        - backup table (ALTER table " + TableName + " RENAME TO 'temp_"                    + TableName)
        - create new table (the newest table creation schema)
        - get the intersection with the new columns, this time columns taken from the upgraded table (columns.retainAll(DBUtils.GetColumns(db, TableName));)
        - restore data (String cols = StringUtils.join(columns, ",");
                    db.execSQL(String.format(
                            "INSERT INTO %s (%s) SELECT %s from temp_%s",
                            TableName, cols, cols, TableName));
        )
        - remove backup table (DROP table 'temp_" + TableName)
        - setTransactionSuccessful
         */
        logger.severe("DB Upgrade not implemented yes!");
        //db.exec("BeginTransaction");

        //db.exec("EndTransaction");
    }
}
