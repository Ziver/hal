package se.hal;


import se.hal.deamon.SensorDataAggregatorDaemon;
import se.hal.deamon.SensorDataCleanupDaemon;
import se.hal.deamon.PCDataSynchronizationClient;
import se.hal.deamon.PCDataSynchronizationDaemon;
import se.hal.intf.HalDaemon;
import se.hal.intf.HalHttpPage;
import se.hal.page.SensorConfigHttpPage;
import se.hal.page.PCHeatMapHttpPage;
import se.hal.page.PCOverviewHttpPage;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
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
        CompactLogFormatter formatter = new CompactLogFormatter();
        LogUtil.setLevel("se.hal", Level.FINEST);
        LogUtil.setFormatter("se.hal", formatter);
        LogUtil.setLevel("zutil", Level.FINEST);
        LogUtil.setFormatter("zutil", formatter);
        LogUtil.setGlobalFormatter(formatter);


        // init DB and other configurations
        HalContext.initialize();
        DBConnection db = HalContext.getDB();

        // Init sensors,events and controllers
        ControllerManager.initialize();
        for(Sensor sensor : Sensor.getLocalSensors(db)){
            ControllerManager.getInstance().register(sensor);
        }
        for(Event event : Event.getEvents(db)){
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
        pages = new HalHttpPage[]{
                new PCOverviewHttpPage(),
                new PCHeatMapHttpPage(),
                new SensorConfigHttpPage()
        };
        HttpServer http = new HttpServer(HalContext.getIntegerProperty("http_port"));
        http.setDefaultPage(new HttpFilePage(FileUtil.find("web-resource/")));
        http.setPage("/", pages[0]);
        for(HalHttpPage page : pages){
            http.setPage(page.getURL(), page);
        }
        http.start();
    }
}
