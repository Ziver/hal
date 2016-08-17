package se.hal.plugin.tellstick;

import se.hal.plugin.tellstick.protocol.NexaSelfLearningProtocol;

/**
 * Created by Ziver on 2015-11-19.
 */
public class TelstickSerialCommNexaOnOffTest {

    public static void main(String[] args) {
        try {
            System.out.println("Connecting to db...");
            TellstickSerialComm comm = new TellstickSerialComm();
            // http://developer.telldus.com/doxygen/TellStick.html
            comm.initialize("COM8");
            //comm.connect("/dev/ttyUSB1");

            Thread.sleep(1000);

            NexaSelfLearningProtocol nexa = new NexaSelfLearningProtocol();
            //nexa.setHouse(11772006);
            nexa.setHouse(14160770);
            nexa.setGroup(false);
            nexa.setUnit(1);

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
