package se.hal.intf;

import zutil.db.DBConnection;

import java.sql.SQLException;

/**
 * A plugin interface for custom plugin DB changes.
 */
public abstract class HalDatabaseUpgrade {

    private int referenceDBVersion;
    private String referenceDBPath;


    public HalDatabaseUpgrade(int referenceDBVersion, String referenceDBPath) {
        this.referenceDBVersion = referenceDBVersion;
        this.referenceDBPath = referenceDBPath;
    }


    /**
     * @return the reference DB version which will be used as a to state during the upgrade process.
     */
    public int getReferenceDBVersion() {
        return referenceDBVersion;
    }

    /**
     * @return the path to the reference DB file that represents the target structure of the DB.
     */
    public String getReferenceDBPath() {
        return referenceDBPath;
    }

    /**
     * Method will be called just before the DB upgrade is performed.
     *
     * @param db            Connection to the DB that will be upgraded.
     * @param fromDBVersion The current version of the to be upgraded DB.
     * @param toDBVersion   The target version that the DB will be upgraded to.
     */
    public void preDatabaseUpgrade(DBConnection db, int fromDBVersion, int toDBVersion) throws SQLException {}

    /**
     * Method will be called after the DB has been upgraded.
     *
     * @param db            Connection to the upgraded DB.
     * @param fromDBVersion The before upgrade version  of the DB.
     * @param toDBVersion   The target version of the DB.
     */
    public void postDatabaseUpgrade(DBConnection db, int fromDBVersion, int toDBVersion) throws SQLException {}
}
