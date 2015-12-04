package se.koc.hal;


import se.koc.hal.deamon.DataAggregatorDaemon;
import se.koc.hal.deamon.HalDaemon;
import zutil.db.DBConnection;
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
	
	public static DBConnection db;
	
    private static HalDaemon[] daemons = new HalDaemon[]{
            new DataAggregatorDaemon()
    };

    public static void main(String[] args) throws Exception {

    	// init logging
    	LogUtil.setGlobalLevel(Level.ALL);
    	LogUtil.setGlobalFormatter(new CompactLogFormatter());
    	
        // init Database
        db = new DBConnection(DBConnection.DBMS.SQLite, "hal.db");

        // init daemons
        Timer daemonTimer = new Timer();
        for(HalDaemon daemon : daemons){
            daemon.initiate(daemonTimer);
        }
        
        HttpServer http = new HttpServer(8080);
        http.setDefaultPage(new HttpFilePage(FileUtil.find("web-resource/")));
        http.setPage("/", new PowerChallengeHttpPage());
        http.start();
    }
}
