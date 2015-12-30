package se.koc.hal;


import se.koc.hal.deamon.DataAggregatorDaemon;
import se.koc.hal.deamon.DataCleanupDaemon;
import se.koc.hal.deamon.DataSynchronizationClient;
import se.koc.hal.deamon.DataSynchronizationDaemon;
import se.koc.hal.intf.HalDaemon;
import se.koc.hal.intf.HalHttpPage;
import se.koc.hal.page.PCConfigureHttpPage;
import se.koc.hal.page.PCHeatMapHttpPage;
import se.koc.hal.page.PCOverviewHttpPage;
import se.koc.hal.struct.Sensor;
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
public class PowerChallenge {

	
    private static HalDaemon[] daemons;
    private static HalHttpPage[] pages;

    public static void main(String[] args) throws Exception {
        // init logging
        CompactLogFormatter formatter = new CompactLogFormatter();
        LogUtil.setLevel("se.koc.hal", Level.FINEST);
        LogUtil.setFormatter("se.koc.hal", formatter);
        LogUtil.setLevel("zutil", Level.FINEST);
        LogUtil.setFormatter("zutil", formatter);
        LogUtil.setGlobalFormatter(formatter);

        // init DB and other configurations
        HalContext.initialize();
        DBConnection db = HalContext.getDB();

        // Init sensors and controllers
        ControllerManager.initialize();
        for(Sensor sensor : Sensor.getLocalSensors(db)){
            ControllerManager.getInstance().register(sensor);
        }

        // init daemons
        daemons = new HalDaemon[]{
                new DataAggregatorDaemon(),
                new DataSynchronizationDaemon(),
                new DataSynchronizationClient(),
                new DataCleanupDaemon()
        };
        // We set only one thread for easier troubleshooting
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        for(HalDaemon daemon : daemons){
            daemon.initiate(executor);
        }

        pages = new HalHttpPage[]{
                new PCOverviewHttpPage(),
                new PCHeatMapHttpPage(),
                new PCConfigureHttpPage()
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
