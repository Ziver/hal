package se.hal.plugin.nvr;

import se.hal.HalContext;
import se.hal.intf.HalDatabaseUpgrader;

/**
 * The DB upgrade class for Hal-NVR plugin
 */
public class NVRDatabaseUpgrader extends HalDatabaseUpgrader {
    private static final int    REFERENCE_DB_VERSION = 1;
    private static final String REFERENCE_DB_PATH = HalContext.RESOURCE_ROOT + "/resource/hal-nvr-reference.db";


    public NVRDatabaseUpgrader() {
        super(REFERENCE_DB_VERSION, REFERENCE_DB_PATH);
    }

}
