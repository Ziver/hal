package se.koc.hal;


import se.koc.hal.deamon.DataAggregatorDaemon;
import se.koc.hal.deamon.HalDaemon;
import zutil.db.DBConnection;

import java.util.Timer;

/**
 * Created by Ziver on 2015-12-03.
 */
public class PowerChallenge {
    private static HalDaemon[] daemons = new HalDaemon[]{
            new DataAggregatorDaemon()
    };

    public static void main(String[] args) throws Exception {

        // init Database
        final DBConnection db = new DBConnection(DBConnection.DBMS.SQLite, "hal.db");

        // init daemons
        Timer daemonTimer = new Timer();
        for(HalDaemon daemon : daemons){
            daemon.initiate(daemonTimer);
        }
    }
}
