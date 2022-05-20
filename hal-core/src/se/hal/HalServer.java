package se.hal;


import se.hal.daemon.HalExternalWebDaemon;
import se.hal.intf.*;
import se.hal.page.HalAlertManager;
import se.hal.page.StartupWebPage;
import se.hal.struct.PluginConfig;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpServer;
import zutil.net.http.page.HttpFilePage;
import zutil.net.http.page.HttpRedirectPage;
import zutil.plugin.PluginData;
import zutil.plugin.PluginManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Main class for Hal
 */
public class HalServer {
    private static final Logger logger = LogUtil.getLogger();

    private static List<HalAbstractControllerManager> controllerManagers = new ArrayList<>();

    private static ScheduledExecutorService daemonExecutor;
    private static List<HalDaemon> daemons = new ArrayList<>();

    private static HttpServer http;
    private static HalExternalWebDaemon httpExternal;
    private static PluginManager pluginManager;


    public static void main(String[] args) {
        try {
            // ------------------------------------
            // Initialize Hal
            // ------------------------------------

            // init logging
            LogUtil.readConfiguration("logging.properties");

            HalAlertManager.initialize();

            http = new HttpServer(HalContext.getIntegerProperty(HalContext.CONFIG_HTTP_PORT));
            http.setDefaultPage(new StartupWebPage());
            http.start();

            // init variables
            pluginManager = new PluginManager();
            daemonExecutor = Executors.newScheduledThreadPool(1); // We set only one thread for easier troubleshooting for now

            // Upgrade database
            HalDatabaseUpgradeManager.initialize(pluginManager);
            HalDatabaseUpgradeManager.upgrade();

            // init DB and other configurations

            HalContext.initialize();
            DBConnection db = HalContext.getDB();

            logger.info("Working directory: " + FileUtil.find(".").getAbsolutePath());

            httpExternal = new HalExternalWebDaemon();
            registerDaemon(httpExternal);

            // ------------------------------------
            // Initialize Plugins
            // ------------------------------------

            logger.info("Looking for plugins.");

            // Disable plugins based on settings
            for (PluginData plugin : getAllPlugins()) {
                PluginConfig pluginConfig = PluginConfig.getPluginConfig(db, plugin.getName());

                // If plugin is not found in DB then disable it and create  an entry.
                if (pluginConfig == null) {
                    pluginConfig = new PluginConfig(plugin.getName());
                    pluginConfig.setEnabled(false);
                    pluginConfig.save(db);
                }

                if (!pluginConfig.isEnabled() && !plugin.getName().equals("Hal-Core")) {
                    logger.info("Disabling plugin '" + plugin.getName() + "'.");
                    plugin.setEnabled(false);
                }
            }

            // ------------------------------------
            // Initialize Managers
            // ------------------------------------

            logger.info("Initializing managers.");

            TriggerManager.initialize(pluginManager);

            for (Iterator<HalAbstractControllerManager> it = pluginManager.getSingletonIterator(HalAbstractControllerManager.class); it.hasNext(); ) {
                HalAbstractControllerManager manager = it.next();
                manager.initialize(pluginManager);
                controllerManagers.add(manager);
            }

            // ------------------------------------
            // Init daemons
            // ------------------------------------

            logger.info("Initializing daemons.");

            for (Iterator<HalDaemon> it = pluginManager.getSingletonIterator(HalDaemon.class); it.hasNext(); ) {
                HalDaemon daemon = it.next();
                registerDaemon(daemon);
            }

            // ------------------------------------
            // Init http server
            // ------------------------------------

            logger.info("Initializing HTTP Server.");

            HalWebPage.getRootNav().createSubNav("Sensors");
            HalWebPage.getRootNav().createSubNav("Events").setWeight(100);
            HalWebPage.getRootNav().createSubNav("Settings").setWeight(200);

            HttpFilePage filePage = new HttpFilePage(FileUtil.find(HalContext.RESOURCE_WEB_ROOT));
            filePage.disableCaching(".*\\.m3u8");

            http.setDefaultPage(filePage);
            http.setPage("/", new HttpRedirectPage("/map"));
            http.setPage(HalAlertManager.getInstance().getUrl(), HalAlertManager.getInstance());

            for (Iterator<HalWebPage> it = pluginManager.getSingletonIterator(HalJsonPage.class); it.hasNext(); )
                registerPage(it.next());
            for (Iterator<HalWebPage> it = pluginManager.getSingletonIterator(HalWebPage.class); it.hasNext(); )
                registerPage(it.next());

        } catch (Exception e) {
            logger.log(Level.SEVERE, "Startup failed.", e);
            System.exit(1);
        }
    }


    public static void enablePlugin(String name, boolean enabled) throws SQLException {
        if (name.equals("Hal-Core"))
            throw new IllegalArgumentException("Hal-Core cannot be disabled as it is critical component of Hal.");

        DBConnection db = HalContext.getDB();
        PluginConfig pluginConfig = PluginConfig.getPluginConfig(db, name);

        if (pluginConfig == null)
            pluginConfig = new PluginConfig(name);

        logger.info("Plugin '" + name + "' has been " + (enabled ? "enabled" : "disabled") + ", change will take affect after restart.");

        pluginConfig.setEnabled(enabled);
        pluginManager.getPluginData(name).setEnabled(pluginConfig.isEnabled());
        pluginConfig.save(db);
    }


    public static List<PluginData> getEnabledPlugins() {
        return pluginManager.toArray();
    }

    public static List<PluginData> getAllPlugins() {
        return pluginManager.toArrayAll();
    }

    public static List<HalAbstractControllerManager> getControllerManagers() {
        return controllerManagers;
    }

    /**
     * @param daemon    registers the given daemon and starts execution of the Runnable.
     */
    public static void registerDaemon(HalDaemon daemon){
        try {
            logger.info("Registering daemon: " + daemon.getClass());
            daemons.add(daemon);
            daemon.initiate(daemonExecutor);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to initialize daemon: " + daemon.getClass(), e);
        }
    }


    /**
     * Registers the given page with the intranet Hal web server.
     *
     * @param url  is the web path to the page.
     * @param page is the page to register with the server.
     */
    public static void registerPage(String url, HttpPage page){
        http.setPage(url, page);
    }

    /**
     * Registers the given page with the intranet Hal web server.
     *
     * @param page is the page to register with the server.
     */
    public static void registerPage(HalWebPage page){
        registerPage(page.getId(), page);
    }

    /**
     * @return the HTTP server that handles external web pages.
     */
    public static HalExternalWebDaemon getExternalWebDaemon(){
        return httpExternal;
    }


}
