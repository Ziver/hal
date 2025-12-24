/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2025 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.hal;

import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.db.handler.PropertiesSQLResult;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.ui.UserMessageManager;

import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;


public class HalContext {
    private static final Logger logger = LogUtil.getLogger();

    // Constants

    public static final String CONFIG_HTTP_PORT = "hal_core.http_port";
    public static final String CONFIG_HTTP_EXTERNAL_PORT = "hal_core.http_external_port";
    public static final String CONFIG_HTTP_EXTERNAL_DOMAIN = "hal_core.http_external_domain";
    public static final String CONFIG_HTTP_EXTERNAL_CERT = "hal_core.http_external_cert";
    public static final String CONFIG_DNS_LOCAL_DOMAIN = "hal_core.dns_local_domain";
    public static final String CONFIG_MAP_BACKGROUND_IMAGE = "hal_core.map_bgimage";

    // Path Constants

    public static final String RUNTIME_ROOT;
    static {
        logger.info("Runtime folder: " + new File("").getAbsolutePath());

        if (FileUtil.find("build/install/Hal") != null) // Development environment
            RUNTIME_ROOT = "build/install/Hal";
        else
            RUNTIME_ROOT = ".";
    }

    public static final String RESOURCE_ROOT     = HalContext.RUNTIME_ROOT + "/resources";
    public static final String RESOURCE_WEB_ROOT = HalContext.RUNTIME_ROOT + "/web";
    public static final String RESOURCE_BIN_ROOT = HalContext.RUNTIME_ROOT + "/bin";

    private static final String CONF_FILE = RUNTIME_ROOT + "/hal.conf";
    static final String DB_FILE           = RUNTIME_ROOT + "/hal.db";

    // Variables

    private static DBConnection db; // TODO: Should probably be a db pool as we have multiple threads accessing the DB

    private static HashMap<String,String> registeredConf = new HashMap<>();
    private static Properties fileConf = new Properties();
    private static Properties dbConf = new Properties();

    private static UserMessageManager messageManager = new UserMessageManager();

    static {
        // Set default values to get Hal up and running before DB is read in
        fileConf.setProperty(CONFIG_HTTP_PORT, "" + 8080);
    }


    public static void initialize(){
        try {
            // Read conf
            if (FileUtil.find(CONF_FILE) != null) {
                FileReader in = new FileReader(CONF_FILE);
                fileConf.load(in);
                in.close();
            } else {
                logger.info("No hal.conf file found at: " + CONF_FILE);
            }

            // Init DB
            File dbFile = FileUtil.find(DB_FILE);
            db = new DBConnection(DBConnection.DBMS.SQLite, DB_FILE);

            if (dbFile == null){
                logger.severe("Unable to find Hal DB: " + DB_FILE);
                System.exit(1);
            } else {
                dbConf = db.exec("SELECT * FROM conf", new PropertiesSQLResult());
            }
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
        return getStringProperty(key, null);
    }
    public static String getStringProperty(String key, String defaultValue){
        registerProperty(key);

        String value = null;
        if (fileConf != null)
            value = fileConf.getProperty(key);
        if (value == null && dbConf != null)
            value = dbConf.getProperty(key);
        return value != null ? value : defaultValue;
    }

    public static int getIntegerProperty(String key){
        return getIntegerProperty(key, 0);
    }
    public static int getIntegerProperty(String key, int defaultValue){
        String value = getStringProperty(key);
        return value != null ? Integer.parseInt(getStringProperty(key)) : defaultValue;
    }

    public static long getLongProperty(String key){
        return getLongProperty(key, 0);
    }
    public static long getLongProperty(String key, long defaultValue){
        String value = getStringProperty(key);
        return value != null ? Long.parseLong(getStringProperty(key)) : defaultValue;
    }

    public static boolean getBooleanProperty(String key) {
        return getBooleanProperty(key, false);
    }
    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getStringProperty(key);
        return value != null ? Boolean.parseBoolean(getStringProperty(key)) : defaultValue;
    }

    public static void setProperty(String key, String value) {
        try {
            PreparedStatement stmt = db.getPreparedStatement("REPLACE INTO conf (key, value) VALUES (?, ?)");
            stmt.setObject(1, key);
            stmt.setObject(2, value);
            DBConnection.exec(stmt);
            dbConf.setProperty(key, value);
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Was unable to save property: " + key + " = " + value, e);
        }
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


    public static UserMessageManager getUserMessageManager() {
        return messageManager;
    }
}
