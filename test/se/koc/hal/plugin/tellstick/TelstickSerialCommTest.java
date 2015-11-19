package se.koc.hal.plugin.tellstick;

import se.koc.hal.plugin.tellstick.TellstickSerialComm;
import se.koc.hal.plugin.tellstick.protocols.NexaSelfLearning;
import se.koc.hal.plugin.tellstick.protocols.Oregon0x1A2D;
import zutil.db.DBConnection;

import java.sql.SQLException;

/**
 * Created by Ziver on 2015-11-19.
 */
public class TelstickSerialCommTest {

    public static void main(String[] args) {
        try {
            final DBConnection db = new DBConnection(DBConnection.DBMS.SQLite, "hal.db");
            // http://developer.telldus.com/doxygen/TellStick.html
            TellstickSerialComm comm = new TellstickSerialComm();
            comm.setListener(new TellstickChangeListener() {
                boolean toggle = false;
                @Override
                public void stateChange(TellstickProtocol protocol) {
                    if(toggle) {
                        toggle = false;
                        return;
                    }
                    toggle = true;

                    if(protocol instanceof Oregon0x1A2D){
                        System.out.println("Oregon0x1A2D= Temperature: "+((Oregon0x1A2D)protocol).getTemperature() +
                                "Humidity: "+((Oregon0x1A2D)protocol).getHumidity());
                        try {
                            db.exec("INSERT INTO power_meter (timestamp, pulses) VALUES("+System.currentTimeMillis()+","+(int)((Oregon0x1A2D)protocol).getTemperature()+")");
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            //comm.connect("COM5");
            comm.connect("/dev/ttyUSB1");

            Thread.sleep(1000);

            NexaSelfLearning nexa = new NexaSelfLearning();
            //nexa.setHouse(11772006);
            nexa.setHouse(15087918);
            nexa.setGroup(0);
            nexa.setUnit(0);


            while(true) {
                Thread.sleep(1000);
                /*nexa.setEnable(true);
                nexa.setUnit(0);
                comm.write(nexa);
                Thread.sleep(2000);
                nexa.setUnit(1);
                comm.write(nexa);
                Thread.sleep(2000);


                nexa.setEnable(false);
                nexa.setUnit(0);
                comm.write(nexa);
                Thread.sleep(2000);
                nexa.setUnit(1);
                comm.write(nexa);
                Thread.sleep(2000);*/
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
