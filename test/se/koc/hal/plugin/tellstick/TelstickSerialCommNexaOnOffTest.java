package se.koc.hal.plugin.tellstick;

import se.koc.hal.plugin.tellstick.protocols.NexaSelfLearning;

/**
 * Created by Ziver on 2015-11-19.
 */
public class TelstickSerialCommNexaOnOffTest {

    public static void main(String[] args) {
        try {
            System.out.println("Connecting to db...");
            TellstickSerialComm comm = new TellstickSerialComm();
            // http://developer.telldus.com/doxygen/TellStick.html
            System.out.println("Connecting to com port...");
            //comm.connect("COM5");
            comm.connect("/dev/ttyUSB1");

            Thread.sleep(1000);

            NexaSelfLearning nexa = new NexaSelfLearning();
            //nexa.setHouse(11772006);
            nexa.setHouse(15087918);
            nexa.setGroup(0);
            nexa.setUnit(0);

            System.out.println("Up and Running");
            while(true) {
                Thread.sleep(2000);
                nexa.turnOn();
                nexa.setUnit(0);
                comm.write(nexa);
                Thread.sleep(2000);
                nexa.setUnit(1);
                comm.write(nexa);
                Thread.sleep(2000);


                nexa.turnOff();
                nexa.setUnit(0);
                comm.write(nexa);
                Thread.sleep(2000);
                nexa.setUnit(1);
                comm.write(nexa);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
