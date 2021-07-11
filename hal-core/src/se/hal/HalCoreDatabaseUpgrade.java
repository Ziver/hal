package se.hal;

import se.hal.intf.HalDatabaseUpgrade;
import zutil.db.DBConnection;
import zutil.log.LogUtil;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * The DB upgrade class for Hal-Core
 */
public class HalCoreDatabaseUpgrade extends HalDatabaseUpgrade {
    private static final Logger logger = LogUtil.getLogger();

    private static final int    REFERENCE_DB_VERSION = 16;
    private static final String REFERENCE_DB_PATH = HalContext.RESOURCE_ROOT + "/resource/hal-core-reference.db";

    private static final int CLEAR_INTERNAL_AGGR_DATA_DB_VERSION = 11;
    private static final int CLEAR_EXTERNAL_AGGR_DATA_DB_VERSION = 0;


    public HalCoreDatabaseUpgrade() {
        super(REFERENCE_DB_VERSION, REFERENCE_DB_PATH);
    }


    @Override
    public void postDatabaseUpgrade(DBConnection db, int fromDBVersion, int toDBVersion) throws SQLException {
        if (fromDBVersion <= CLEAR_EXTERNAL_AGGR_DATA_DB_VERSION){
            logger.fine("Clearing external aggregate data.");
            db.exec("DELETE FROM sensor_data_aggr WHERE sensor_id = "
                    + "(SELECT sensor.id FROM user, sensor WHERE user.external == 1 AND sensor.user_id = user.id)");
        }
        if (fromDBVersion <= CLEAR_INTERNAL_AGGR_DATA_DB_VERSION){
            logger.fine("Clearing local aggregate data.");
            db.exec("DELETE FROM sensor_data_aggr WHERE sensor_id IN "
                    + "(SELECT sensor.id FROM user, sensor WHERE user.external == 0 AND sensor.user_id = user.id)");
            // Update all internal sensors aggregation version to indicate for peers that they need to re-sync all data
            db.exec("UPDATE sensor SET aggr_version = (aggr_version+1) WHERE id = "
                    + "(SELECT sensor.id FROM user, sensor WHERE user.external == 0 AND sensor.user_id = user.id)");
        }
    }
}
