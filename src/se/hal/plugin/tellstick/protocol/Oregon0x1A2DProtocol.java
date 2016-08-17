package se.hal.plugin.tellstick.protocol;

import se.hal.intf.HalSensorConfig;
import se.hal.plugin.tellstick.TellstickProtocol;
import zutil.log.LogUtil;
import zutil.ui.Configurator;

import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-11-19.
 */
public class Oregon0x1A2DProtocol extends TellstickProtocol {
    private static final Logger logger = LogUtil.getLogger();



    public Oregon0x1A2DProtocol(){
        super("oregon", "0x1A2D");
    }


    @Override
    public void decode(byte[] data) {
        //class:sensor;protocol:oregon;model:0x1A2D;data:20BA000000002700;

        // int channel = (data[0] >> 4) & 0x7; // channel not used
        address = data[1] & 0xFF;
        int temp3 = (data[2] >> 4) & 0xF;
        int temp1 = (data[3] >> 4) & 0xF;
        int temp2 = data[3] & 0xF;
        int hum2 = (data[4] >> 4) & 0xF;
        boolean negative = (data[4] & (1 << 3)) > 0;
        int hum1 = data[5] & 0xF;
        int checksum = data[6];

        int calcChecksum = ((data[5] >> 4) & 0xF) + (data[5] & 0xF);
        calcChecksum += ((data[4] >> 4) & 0xF) + (data[4] & 0xF);
        calcChecksum += ((data[3] >> 4) & 0xF) + (data[3] & 0xF);
        calcChecksum += ((data[2] >> 4) & 0xF) + (data[2] & 0xF);
        calcChecksum += ((data[1] >> 4) & 0xF) + (data[1] & 0xF);
        calcChecksum += ((data[0] >> 4) & 0xF) + (data[0] & 0xF);
        calcChecksum += 0x1 + 0xA + 0x2 + 0xD - 0xA;

        if (calcChecksum != checksum) {
            logger.fine("Checksum failed, address: "+address);
            return;
        }

        temperature = ((temp1 * 100) + (temp2 * 10) + temp3)/10.0;
        if (negative)
            temperature = -temperature;
        humidity = (hum1 * 10.0) + hum2;

    }

}
