package se.koc.hal;

import zutil.db.DBConnection;

import java.io.FileReader;
import java.util.Properties;

public class HalContext {
    // Constants
    private static final String CONF_FILE = "hal.conf";

    // Variables
    private static DBConnection db;

	public static Properties conf;
	private static Properties defaultConf;


	static {
		defaultConf = new Properties();
		defaultConf.setProperty("http_port", ""+8080);
		defaultConf.setProperty("sync_port", ""+6666);

        HalContext.initialize();
	}
	
	
	public static void initialize(){
        try {
            // Read conf
            conf = new Properties(defaultConf);
            FileReader in = new FileReader(CONF_FILE);
            conf.load(in);
            in.close();

            // Init DB
            db = new DBConnection(DBConnection.DBMS.SQLite, "hal.db");
        } catch (Exception e){
            throw new RuntimeException(e);
        }
	}


    public static DBConnection getDB(){
        return db;
    }
}
