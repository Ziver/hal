package se.koc.hal;


import se.koc.hal.deamon.DataAggregatorDaemon;
import se.koc.hal.deamon.DataSynchronizationClient;
import se.koc.hal.deamon.DataSynchronizationDaemon;
import se.koc.hal.deamon.HalDaemon;
import se.koc.hal.page.PCConfigureHttpPage;
import se.koc.hal.page.PCHeatMapHttpPage;
import se.koc.hal.page.PCOverviewHttpPage;
import zutil.io.file.FileUtil;
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;
import zutil.net.http.HttpServer;
import zutil.net.http.pages.HttpFilePage;

import java.util.Timer;
import java.util.logging.Level;

/**
 * Created by Ziver on 2015-12-03.
 */
public class PowerChallenge {

	
    private static HalDaemon[] daemons;

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

        // init daemons
        daemons = new HalDaemon[]{
                new DataAggregatorDaemon(),
                new DataSynchronizationDaemon(),
                new DataSynchronizationClient()
        };
        Timer daemonTimer = new Timer();
        for(HalDaemon daemon : daemons){
            daemon.initiate(daemonTimer);
        }
        
        HttpServer http = new HttpServer(HalContext.getIntegerProperty("http_port"));
        http.setDefaultPage(new HttpFilePage(FileUtil.find("web-resource/")));
        http.setPage("/", new PCOverviewHttpPage());
        http.setPage("/configure", new PCConfigureHttpPage());
        http.setPage("/heatmap", new PCHeatMapHttpPage());
        http.start();
    }
}
