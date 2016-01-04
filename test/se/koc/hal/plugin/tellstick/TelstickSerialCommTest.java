package se.koc.hal.plugin.tellstick;

import se.koc.hal.intf.HalSensor;
import se.koc.hal.intf.HalSensorController;
import se.koc.hal.intf.HalSensorReportListener;
import se.koc.hal.plugin.tellstick.protocols.Oregon0x1A2D;
import zutil.db.DBConnection;
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;

import java.sql.PreparedStatement;
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
            comm.setListener(new HalSensorReportListener() {
                @Override
                public void reportReceived(HalSensor s) {
                    if(s instanceof Oregon0x1A2D){
                        logger.info("Power used: "+ ((Oregon0x1A2D)s).getTemperature() +" pulses");
                        try {
                            PreparedStatement stmt =
                                    db.getPreparedStatement("INSERT INTO sensor_data_raw (timestamp, event_id, data) VALUES(?, ?, ?)");
                            stmt.setLong(1, s.getTimestamp());
                            stmt.setLong(2, 1);
                            stmt.setDouble(3, ((Oregon0x1A2D)s).getTemperature());
                            db.exec(stmt);
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            logger.info("Connecting to com port...");
            comm.connect("/dev/ttyUSB1");

            logger.info("Up and Running");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
