package se.hal.plugin.zigbee.db;

import se.hal.HalContext;
import se.hal.intf.HalDatabaseUpgrader;

/**
 * The DB upgrade class for Hal-Zigbee plugin
 */
public class ZigbeeHalDatabaseUpgrader extends HalDatabaseUpgrader {
    private static final int    REFERENCE_DB_VERSION = 1;
    private static final String REFERENCE_DB_PATH = HalContext.RESOURCE_ROOT + "/hal-zigbee-reference.db";


    public ZigbeeHalDatabaseUpgrader() {
        super(REFERENCE_DB_VERSION, REFERENCE_DB_PATH);
    }

}
