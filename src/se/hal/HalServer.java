package se.hal;


import se.hal.deamon.PCDataSynchronizationClient;
import se.hal.deamon.PCDataSynchronizationDaemon;
import se.hal.deamon.SensorDataAggregatorDaemon;
import se.hal.deamon.SensorDataCleanupDaemon;
import se.hal.intf.HalDaemon;
import se.hal.intf.HalHttpPage;
import se.hal.page.*;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
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
 * Created by Ziver on 2015-12-03.
 */
public class HalServer {
	
    private static List<HalDaemon> daemons;
    private static List<HalHttpPage> pages;


    public static void main(String[] args) throws Exception {
        // init logging
        LogUtil.readConfiguration("logging.properties");

        // init Managers
        PluginManager pluginManager = new PluginManager("./");
        HalContext.initialize();
        ControllerManager.initialize(pluginManager);
        HalAlertManager.initialize();


        // init DB and other configurations
        DBConnection db = HalContext.getDB();

        // Init sensors,events and controllers
        for(Sensor sensor : Sensor.getLocalSensors(db)){
            ControllerManager.getInstance().register(sensor);
        }
        for(Event event : Event.getLocalEvents(db)){
            ControllerManager.getInstance().register(event);
        }


        // init daemons
        daemons = new ArrayList<>();
        daemons.addAll(Arrays.asList(new HalDaemon[]{
                new SensorDataAggregatorDaemon(),
                new SensorDataCleanupDaemon(),
        }));
        for (Iterator<HalDaemon> it=pluginManager.getObjectIterator(HalDaemon.class);
                it.hasNext(); ){
            daemons.add(it.next());
        }
        // We set only one thread for easier troubleshooting
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        for(HalDaemon daemon : daemons){
            daemon.initiate(executor);
        }


        // init http server
        HalHttpPage.getRootNav().createSubNav("Sensors");
        HalHttpPage.getRootNav().createSubNav("Events").setWeight(100);
        pages = new ArrayList<>();
        pages.addAll(Arrays.asList(new HalHttpPage[]{
                new SensorOverviewHttpPage(),
                new SensorConfigHttpPage(),

                new EventOverviewHttpPage(),
                new EventConfigHttpPage(),
                new UserConfigHttpPage(),
        }));
        for (Iterator<HalHttpPage> it=pluginManager.getObjectIterator(HalHttpPage.class);
                it.hasNext(); ){
            pages.add(it.next());
        }

        HttpServer http = new HttpServer(HalContext.getIntegerProperty("http_port"));
        http.setDefaultPage(new HttpFilePage(FileUtil.find("resource/web/")));
        http.setPage("/", new HttpRedirectPage("/"+pages.get(0).getId()));
        http.setPage(HalAlertManager.getInstance().getUrl(), HalAlertManager.getInstance());
        for(HalHttpPage page : pages){
            http.setPage(page.getId(), page);
        }
        http.start();
    }
}
