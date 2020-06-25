package se.hal;


import se.hal.intf.HalDaemon;
import se.hal.intf.HalWebPage;
import se.hal.intf.HalJsonPage;
import se.hal.page.*;
import se.hal.struct.Event;
import se.hal.struct.PluginConfig;
import se.hal.struct.Sensor;
import se.hal.struct.TriggerFlow;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpServer;
import zutil.net.http.page.HttpFilePage;
import zutil.net.http.page.HttpRedirectPage;
import zutil.plugin.PluginData;
import zutil.plugin.PluginManager;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Logger;

/**
 * Main class for Hal
 */
public class HalServer {
    private static final Logger logger = LogUtil.getLogger();

    private static ScheduledExecutorService daemonExecutor;
    private static List<HalDaemon> daemons = new ArrayList<>();

    private static HttpServer http;
    private static List<HalWebPage> pages = new ArrayList<>();

    private static PluginManager pluginManager;



    public static void main(String[] args) throws Exception {
        // init logging
        LogUtil.readConfiguration("logging.properties");

        // init DB and other configurations
        HalContext.initialize();
        DBConnection db = HalContext.getDB();

        // ------------------------------------
        // Init Plugins
        // ------------------------------------

        logger.info("Looking for plugins.");
        pluginManager = new PluginManager();

        // Disable plugins based on settings
        for (PluginData plugin : getPlugins()) {
            PluginConfig pluginConfig = PluginConfig.getPluginConfig(db, plugin.getName());

            if (pluginConfig != null && !pluginConfig.isEnabled()){
                logger.info("Disabling plugin '" + plugin.getName() + "'.");
                plugin.setEnabled(false);
            }
        }

        // ------------------------------------
        // Init Managers
        // ------------------------------------

        logger.info("Initializing managers.");

        HalAlertManager.initialize();
        ControllerManager.initialize(pluginManager);
        TriggerManager.initialize(pluginManager);

        // ------------------------------------
        // Import sensors,events and triggers
        // ------------------------------------

        logger.info("Initializing Sensors and Events.");

        for(Sensor sensor : Sensor.getLocalSensors(db)){
            ControllerManager.getInstance().register(sensor);
        }
        for(Event event : Event.getLocalEvents(db)){
            ControllerManager.getInstance().register(event);
        }
        // Import triggers
        for(TriggerFlow flow : TriggerFlow.getTriggerFlows(db)){
            TriggerManager.getInstance().register(flow);
        }


        // ------------------------------------
        // Init daemons
        // ------------------------------------

        logger.info("Initializing daemons.");

        // We set only one thread for easier troubleshooting
        daemonExecutor = Executors.newScheduledThreadPool(1);
        for (Iterator<HalDaemon> it = pluginManager.getSingletonIterator(HalDaemon.class); it.hasNext(); )
            registerDaemon(it.next());

        // ------------------------------------
        // Init http server
        // ------------------------------------

        logger.info("Initializing HTTP Server.");

        HalWebPage.getRootNav().createSubNav("Sensors");
        HalWebPage.getRootNav().createSubNav("Events").setWeight(100);
        HalWebPage.getRootNav().createSubNav("Settings").setWeight(200);

        http = new HttpServer(HalContext.getIntegerProperty("http_port"));
        http.setDefaultPage(new HttpFilePage(FileUtil.find("resource/web/")));
        http.setPage("/", new HttpRedirectPage("/map"));
        http.setPage(HalAlertManager.getInstance().getUrl(), HalAlertManager.getInstance());
        for (Iterator<HalWebPage> it = pluginManager.getSingletonIterator(HalJsonPage.class); it.hasNext(); )
            registerPage(it.next());
        for (Iterator<HalWebPage> it = pluginManager.getSingletonIterator(HalWebPage.class); it.hasNext(); )
            registerPage(it.next());
        http.start();
    }


    public static void setPluginEnabled(String name, boolean enabled) throws SQLException {
        DBConnection db = HalContext.getDB();
        PluginConfig pluginConfig = PluginConfig.getPluginConfig(db, name);

        if (pluginConfig == null)
            pluginConfig = new PluginConfig(name);

        logger.info("Plugin '" + name + "' has been " + (enabled ? "enabled" : "disabled") + ", change will take affect after restart.");
        pluginManager.getPluginData(name).setEnabled(enabled);

        pluginConfig.setEnabled(enabled);
        pluginConfig.save(db);
    }

    public static List<PluginData> getPlugins() {
        return pluginManager.toArray();
    }


    public static void registerDaemon(HalDaemon daemon){
        daemons.add(daemon);
        daemon.initiate(daemonExecutor);
    }

    public static void registerPage(HalWebPage page){
        pages.add(page);
        http.setPage(page.getId(), page);
    }
}
