package se.hal.struct;

import zutil.ui.conf.Configurator;
import zutil.ui.conf.Configurator.ConfigurationParam;

/**
 * A Data class used by the dynamic class configuration pages
 */
public class ClassConfigurationData {
        public Class clazz;
        public ConfigurationParam[] params;


    public ClassConfigurationData(Class clazz) {
        this.clazz = clazz;
        this.params = Configurator.getConfiguration(clazz);
    }
}