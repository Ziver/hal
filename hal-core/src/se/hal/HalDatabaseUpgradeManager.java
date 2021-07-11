package se.hal;

import se.hal.intf.HalDatabaseUpgrade;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.db.DBUpgradeHandler;
import zutil.db.handler.PropertiesSQLResult;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.plugin.PluginManager;

import java.io.File;
import java.sql.PreparedStatement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Queue;
import java.util.logging.Logger;

/**
 * A manager class handling all upgrades for the main Hal DB and any plugin changes.
 */
public class HalDatabaseUpgradeManager {
    private static final Logger logger = LogUtil.getLogger();

    private static Queue<HalDatabaseUpgrade> upgradeQueue;


    /**
     * Method will read in all HalDatabaseUpgrade plugins and populate the upgrade queue.
     * Node, method will only read in plugins on the first call, any subsequent calls will be ignored.
     *
     * @param pluginManager
     */
    public static void initialize(PluginManager pluginManager) {
        if (upgradeQueue != null)
            return;

        upgradeQueue = new LinkedList<>();

        for (Iterator<HalDatabaseUpgrade> it = pluginManager.getSingletonIterator(HalDatabaseUpgrade.class); it.hasNext(); ) {
            HalDatabaseUpgrade dbUpgrade = it.next();
            upgradeQueue.add(dbUpgrade);
        }
    }

    /**
     * Method will execute all queued database upgrades.
     */
    public static void upgrade() {
        if (upgradeQueue == null)
            return;

        while (!upgradeQueue.isEmpty()) {
            upgrade(upgradeQueue.poll());
        }
    }

    private synchronized static void upgrade(HalDatabaseUpgrade dbUpgrade) {
        DBConnection mainDB = null;
        DBConnection referenceDB = null;

        String referenceDBPath = dbUpgrade.getReferenceDBPath();

        try {
            if (FileUtil.find(referenceDBPath) == null)
                throw new IllegalArgumentException("Unable to find default DB: " + referenceDBPath);

            // Init DB
            File dbFile = FileUtil.find(HalContext.DB_FILE);
            mainDB = new DBConnection(DBConnection.DBMS.SQLite, HalContext.DB_FILE);
            Properties dbConf =
                    mainDB.exec("SELECT * FROM conf", new PropertiesSQLResult());

            if (dbFile == null) {
                logger.info("No database file found, creating new DB...");
            }

            // Upgrade DB needed?
            referenceDB = new DBConnection(DBConnection.DBMS.SQLite, referenceDBPath);
            String mainDBVersionProperty = dbUpgrade.getClass().getSimpleName() + ".db_version";

            // Check DB version
            final int referenceDBVersion = dbUpgrade.getReferenceDBVersion();
            final int mainDBVersion = (dbConf.getProperty(mainDBVersionProperty) != null ?
                    Integer.parseInt(dbConf.getProperty(mainDBVersionProperty)) :
                    -1);
            logger.info("DB version: " + mainDBVersion);

            if (referenceDBVersion > mainDBVersion) {
                // ----------------------------------------
                // Prepare upgrade
                // ----------------------------------------

                logger.info("Starting DB upgrade from v" + mainDBVersion + " to v" + referenceDBVersion + "...");

                if (dbFile != null){
                    File backupDB = FileUtil.getNextFile(dbFile);
                    logger.fine("Backing up DB to: "+ backupDB);
                    FileUtil.copy(dbFile, backupDB);
                }

                final DBUpgradeHandler handler = new DBUpgradeHandler(referenceDB);
                handler.addIgnoredTable("sqlite_sequence");	// sqlite internal
                handler.setTargetDB(mainDB);

                logger.fine("Performing pre-upgrade activities");

                /*if (doForceUpgrade) {
                    logger.fine("Forced upgrade enabled.");
                    handler.setForcedDBUpgrade(true); // set to true if any of the intermediate db version requires it.
                }*/

                dbUpgrade.preDatabaseUpgrade(mainDB, mainDBVersion, referenceDBVersion);

                // ----------------------------------------
                // Upgrade
                // ----------------------------------------

                handler.upgrade();

                // ----------------------------------------
                // Post-upgrade
                // ----------------------------------------

                logger.fine("Performing post-upgrade activities.");

                // Check if there is a local user

                User localUser = User.getLocalUser(mainDB);
                if (localUser == null){
                    logger.fine("Creating local user.");
                    localUser = new User();
                    localUser.setExternal(false);
                    localUser.save(mainDB);
                }

                dbUpgrade.postDatabaseUpgrade(mainDB, mainDBVersion, referenceDBVersion);

                // Update DB version

                PreparedStatement stmt = mainDB.getPreparedStatement("REPLACE INTO conf (key, value) VALUES (?, ?)");
                stmt.setString(1, mainDBVersionProperty);
                stmt.setInt(2, referenceDBVersion);
                DBConnection.exec(stmt);

                logger.info("DB upgrade done.");
            } else {
                logger.info("No DB upgrade needed");
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        } finally {
            if (mainDB != null)
                mainDB.close();
            if (referenceDB != null)
                referenceDB.close();
        }
    }
}
