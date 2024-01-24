package se.hal.intf;

/**
 * A plugin interface for configuring Javascript modules
 */
public interface HalJavascriptModule {

    /**
     * @return a List of Objects defining the Javascript configuration
     */
    HalJsModule[] getJavascriptModules();


    /**
     * Defines configuration for a Javascript module to be loaded during webpage generation.
     */
    class HalJsModule {
        private String moduleName;
        private String scriptPath;

        public HalJsModule(String moduleName, String scriptPath) {
            this.moduleName = moduleName;
            this.scriptPath = scriptPath;
        }

        public String getModuleName() {
            return moduleName;
        }

        public String getScriptPath() {
            return scriptPath;
        }
    }

    /**
     * Declares a Javascript module to be assigned to a specific web address.
     */
    class HalJsModulePage extends HalJsModule {
        private String page;

        public HalJsModulePage(String moduleName, String scriptPath, String page) {
            super(moduleName, scriptPath);
            this.page = page;
        }

        public String getPage() {
            return page;
        }
    }
}
