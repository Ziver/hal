package se.hal.util;

import zutil.ui.conf.Configurator;
import zutil.ui.conf.Configurator.ConfigurationParam;

/**
 * A Data class used by the dynamic class configuration pages
 */
public class ClassConfigurationFacade {
        public Class clazz;
        public ConfigurationParam[] params;


    public ClassConfigurationFacade(Class clazz) {
        this.clazz = clazz;
        this.params = Configurator.getConfiguration(clazz);
    }
}