package se.hal;


import se.hal.intf.HalDaemon;
import se.hal.intf.HalHttpPage;
import se.hal.intf.HalJsonPage;
import se.hal.page.*;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import se.hal.struct.TriggerFlow;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpServer;
import zutil.net.http.page.HttpFilePage;
import zutil.net.http.page.HttpRedirectPage;
import zutil.plugin.PluginManager;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Main class for Hal
 */
public class HalServer {

    private static ScheduledExecutorService daemonExecutor;
    private static List<HalDaemon> daemons = new ArrayList<>();

    private static HttpServer http;
    private static List<HalHttpPage> pages = new ArrayList<>();



    public static void main(String[] args) throws Exception {
        // init logging
        LogUtil.readConfiguration("logging.properties");

        // init DB and other configurations
        HalContext.initialize();
        DBConnection db = HalContext.getDB();
        PluginManager pluginManager = new PluginManager("./");

        // init Managers
        HalAlertManager.initialize();
        ControllerManager.initialize(pluginManager);
        TriggerManager.initialize(pluginManager);


        // Import sensors,events and controllers
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


        // Init daemons
        // We set only one thread for easier troubleshooting
        daemonExecutor = Executors.newScheduledThreadPool(1);
        for (Iterator<HalDaemon> it=pluginManager.getObjectIterator(HalDaemon.class); it.hasNext(); )
            registerDaemon(it.next());


        // init http server
        HalHttpPage.getRootNav().createSubNav("Sensors");
        HalHttpPage.getRootNav().createSubNav("Events").setWeight(100);

        http = new HttpServer(HalContext.getIntegerProperty("http_port"));
        http.setDefaultPage(new HttpFilePage(FileUtil.find("resource/web/")));
        http.setPage("/", new HttpRedirectPage("/map"));
        http.setPage(HalAlertManager.getInstance().getUrl(), HalAlertManager.getInstance());
        for (Iterator<HalHttpPage> it = pluginManager.getObjectIterator(HalJsonPage.class); it.hasNext(); )
            registerPage(it.next());
        for (Iterator<HalHttpPage> it=pluginManager.getObjectIterator(HalHttpPage.class); it.hasNext(); )
            registerPage(it.next());
        http.start();
    }


    public static void registerDaemon(HalDaemon daemon){
        daemons.add(daemon);
        daemon.initiate(daemonExecutor);
    }
    public static void registerPage(HalHttpPage page){
        pages.add(page);
        http.setPage(page.getId(), page);
    }
}
