package se.hal;


import se.hal.deamon.SensorDataAggregatorDaemon;
import se.hal.deamon.SensorDataCleanupDaemon;
import se.hal.deamon.PCDataSynchronizationClient;
import se.hal.deamon.PCDataSynchronizationDaemon;
import se.hal.intf.HalDaemon;
import se.hal.intf.HalHttpPage;
import se.hal.page.*;
import se.hal.page.HalAlertManager.*;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.io.file.FileUtil;
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;
import zutil.net.http.HttpServer;
import zutil.net.http.pages.HttpFilePage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.logging.Level;

/**
 * Created by Ziver on 2015-12-03.
 */
public class HalServer {
	
    private static HalDaemon[] daemons;
    private static HalHttpPage[] pages;


    public static void main(String[] args) throws Exception {
        // init logging
        LogUtil.readConfiguration("logging.properties");
        /*CompactLogFormatter formatter = new CompactLogFormatter();
        LogUtil.setLevel("se.hal", Level.FINEST);
        LogUtil.setFormatter("se.hal", formatter);
        LogUtil.setLevel("zutil.db.bean", Level.INFO);
        LogUtil.setLevel("zutil.net.http.pages", Level.INFO);
        LogUtil.setLevel("zutil", Level.FINEST);
        LogUtil.setFormatter("zutil", formatter);
        LogUtil.setGlobalFormatter(formatter);*/

        // init Managers
        HalContext.initialize();
        ControllerManager.initialize();
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
        daemons = new HalDaemon[]{
                new SensorDataAggregatorDaemon(),
                new PCDataSynchronizationDaemon(),
                new PCDataSynchronizationClient(),
                new SensorDataCleanupDaemon()
        };
        // We set only one thread for easier troubleshooting
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        for(HalDaemon daemon : daemons){
            daemon.initiate(executor);
        }


        // init http server
        HalHttpPage.getRootNav().addSubNav(new HalNavigation("sensors", "Sensors"));
        HalHttpPage.getRootNav().addSubNav(new HalNavigation("events", "Events"));
        pages = new HalHttpPage[]{
                new SensorOverviewHttpPage(),
                new PCOverviewHttpPage(),
                new PCHeatMapHttpPage(),
                new SensorConfigHttpPage(),

                new EventOverviewHttpPage(),
                new EventConfigHttpPage(),
                new UserConfigHttpPage(),
        };
        HttpServer http = new HttpServer(HalContext.getIntegerProperty("http_port"));
        http.setDefaultPage(new HttpFilePage(FileUtil.find("web-resource/")));
        http.setPage("/", pages[0]);
        http.setPage(HalAlertManager.getInstance().getUrl(), HalAlertManager.getInstance());
        for(HalHttpPage page : pages){
            http.setPage(page.getId(), page);
        }
        http.start();
    }
}
