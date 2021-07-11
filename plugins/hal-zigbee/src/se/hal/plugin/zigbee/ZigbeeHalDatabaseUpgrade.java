package se.hal.plugin.zigbee;

import se.hal.intf.HalDatabaseUpgrade;
import zutil.db.DBConnection;
import zutil.log.LogUtil;

import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * The DB upgrade class for Hal-Core
 */
public class ZigbeeHalDatabaseUpgrade extends HalDatabaseUpgrade {
    private static final int    REFERENCE_DB_VERSION = 1;
    private static final String REFERENCE_DB_PATH = "resource/hal-zigbee-reference.db";


    public ZigbeeHalDatabaseUpgrade() {
        super(REFERENCE_DB_VERSION, REFERENCE_DB_PATH);
    }

}
