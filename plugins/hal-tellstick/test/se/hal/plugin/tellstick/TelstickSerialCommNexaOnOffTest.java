package se.hal.plugin.tellstick;

import se.hal.plugin.tellstick.device.NexaSelfLearning;
import se.hal.struct.devicedata.OnOffEventData;

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

            NexaSelfLearning nexaDevice = new NexaSelfLearning();
            //nexa.setHouse(11772006);
            nexaDevice.setHouse(14160770);
            nexaDevice.setGroup(false);
            nexaDevice.setUnit(1);

            OnOffEventData nexaData = new OnOffEventData();

            System.out.println("Up and Running");
            while(true) {
                Thread.sleep(2000);
                nexaData.turnOn();
                nexaDevice.setUnit(0);
                comm.send(nexaDevice, nexaData);
                Thread.sleep(2000);
                nexaDevice.setUnit(1);
                comm.send(nexaDevice, nexaData);
                Thread.sleep(2000);


                nexaData.turnOff();
                nexaDevice.setUnit(0);
                comm.send(nexaDevice, nexaData);
                Thread.sleep(2000);
                nexaDevice.setUnit(1);
                comm.send(nexaDevice, nexaData);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
