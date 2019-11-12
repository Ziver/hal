package se.hal.plugin.raspberry;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

public class RPiUtility {

    public static Pin getPin(int gpioPin){
        switch(gpioPin){
        case 0:
            return RaspiPin.GPIO_00;
        case 1:
            return RaspiPin.GPIO_01;
        case 2:
            return RaspiPin.GPIO_02;
        case 3:
            return RaspiPin.GPIO_03;
        case 4:
            return RaspiPin.GPIO_04;
        case 5:
            return RaspiPin.GPIO_05;
        case 6:
            return RaspiPin.GPIO_06;
        case 7:
            //used by 1-wire divices
        case 8:
            //used by I2C devices
        case 9:
            //used by I2C devices
        case 10:
            //used by SPI devices
        case 11:
            //used by SPI devices
        case 12:
            //used by SPI devices
        case 13:
            //used by SPI devices
        case 14:
            //used by SPI devices
        case 15:
            //used by Serial devices
        case 16:
            //used by Serial devices
        case 17:
            //reserved for future use
        case 18:
            //reserved for future use
        case 19:
            //reserved for future use
        case 20:
            //reserved for future use
        case 21:
            //reserved for future use
        case 22:
            //reserved for future use
        case 23:
            //reserved for future use
        case 24:
            //reserved for future use
        case 25:
            //reserved for future use
        case 26:
            //reserved for future use
        case 27:
            //reserved for future use
        case 28:
            //reserved for future use
        case 29:
            //reserved for future use
        default:
            return null;
    }
    }

}
