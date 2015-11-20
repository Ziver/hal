package se.koc.hal.plugin.tellstick;

import se.koc.hal.plugin.tellstick.TellstickSerialComm;
import se.koc.hal.plugin.tellstick.protocols.NexaSelfLearning;
import se.koc.hal.plugin.tellstick.protocols.Oregon0x1A2D;
import zutil.db.DBConnection;
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-11-19.
 */
public class TelstickSerialCommTest {
    private static final Logger logger = LogUtil.getLogger();

    public static void main(String[] args) {
        try {
            LogUtil.setGlobalFormatter(new CompactLogFormatter());
            LogUtil.setGlobalLevel(Level.FINEST);

            logger.info("Connecting to db...");
            final DBConnection db = new DBConnection(DBConnection.DBMS.SQLite, "hal.db");

            logger.info("Setting up Tellstick listeners...");
            TellstickSerialComm comm = new TellstickSerialComm();
            comm.setListener(new TellstickChangeListener() {
                @Override
                public void stateChange(TellstickProtocol protocol) {
                    if(protocol instanceof Oregon0x1A2D){
                        logger.info("Power used: "+ ((Oregon0x1A2D)protocol).getTemperature() +" pulses");
                        try {
                            db.exec("INSERT INTO power_meter (timestamp, pulses) VALUES("+System.currentTimeMillis()+","+(int)((Oregon0x1A2D)protocol).getTemperature()+")");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            logger.info("Connecting to com port...");
            //comm.connect("COM5");
            comm.setDaemon(false);
            comm.connect("/dev/ttyUSB1");

            logger.info("Up and Running");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
