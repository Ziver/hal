package se.hal.plugin.tellstick.protocol;

import se.hal.intf.HalSensorData;
import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.plugin.tellstick.TellstickSerialComm;
import se.hal.plugin.tellstick.device.Oregon0x1A2D;
import se.hal.plugin.tellstick.device.Oregon0x1A2D.OregonSensorType;
import se.hal.struct.devicedata.HumiditySensorData;
import se.hal.struct.devicedata.LightSensorData;
import se.hal.struct.devicedata.PowerConsumptionSensorData;
import se.hal.struct.devicedata.TemperatureSensorData;
import zutil.converter.Converter;
import zutil.log.LogUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-11-19.
 * @see <a href="https://github.com/telldus/telldus/blob/master/telldus-core/service/ProtocolOregon.cpp">ProtocolOregon.cpp Tellstick Reference</a>
 */
public class Oregon0x1A2DProtocol extends TellstickProtocol {
    private static final Logger logger = LogUtil.getLogger();
    public static final String PROTOCOL = "oregon";
    public static final String MODEL = "0x1A2D";



    public Oregon0x1A2DProtocol() {
        super(PROTOCOL, MODEL);
    }


    @Override
    public List<TellstickDecodedEntry> decode(byte[] data) {
        /*
        Nibble(s)           Details
        0..3            Sensor ID This 16-bit value is unique to each sensor, or sometimes a group of sensors.
        4               Channel Some sensors use the coding 1 << (ch – 1), where ch is 1, 2 or 3.
        5..6            Rolling Code Value changes randomly every time the sensor is reset
        7               Flags 1 Bit value 0x4 is the battery low flag
        8..[n-5]        Sensor-specific Data Usually in BCD format
        [n-3]..[n-4]    Checksum The 8-bit sum of nibbles 0..[n-5]
        */
        //Example: class:sensor;protocol:oregon;model:0x1A2D;data:20BA000000002700;

        // int channel = (data[0] >> 4) & 0x7; // channel not used
        int address = data[1] & 0xFF;
        int temp3 = (data[2] >> 4) & 0xF;
        int temp1 = (data[3] >> 4) & 0xF;
        int temp2 = data[3] & 0xF;
        int hum2 = (data[4] >> 4) & 0xF;
        boolean negative = (data[4] & (1 << 3)) > 0;
        int hum1 = data[5] & 0xF;
        int checksum = data[6];

        int calcChecksum = 0;
        for (int i = 0; i < 6; i++) {
            calcChecksum += ((data[i] >> 4) & 0xF) + (data[i] & 0xF);
        }
        calcChecksum += 0x1 + 0xA + 0x2 + 0xD - 0xA;

        if (calcChecksum != checksum) {
            logger.fine("Checksum failed, address: "+address+", data: "+ Converter.toHexString(data));
            return null;
        }

        double temperature = ((temp1 * 100) + (temp2 * 10) + temp3)/10.0;
        if (negative)
            temperature = -temperature;
        double humidity = (hum1 * 10.0) + hum2;



        // Create return objects
        long timestamp = System.currentTimeMillis();
        boolean humidityFound=false, temperatureFound=false;
        ArrayList<TellstickDecodedEntry> list = new ArrayList<>();
        for (Oregon0x1A2D device : TellstickSerialComm.getInstance().getRegisteredDevices(Oregon0x1A2D.class)) {
            if (device.getAddress() != address)
                continue;
            HalSensorData dataObj;
            OregonSensorType sensorType = device.getSensorType();
            if (sensorType == null)
                sensorType = OregonSensorType.POWER;
            switch (sensorType) {
                case HUMIDITY:
                    dataObj = new HumiditySensorData(humidity, timestamp);
                    humidityFound = true;
                    break;
                case LIGHT:
                    dataObj = new LightSensorData(temperature, timestamp);
                    temperatureFound = true;
                    break;
                case TEMPERATURE:
                    dataObj = new TemperatureSensorData(temperature, timestamp);
                    temperatureFound = true;
                    break;
                default:
                case POWER:
                    dataObj = new PowerConsumptionSensorData(temperature, timestamp);
                    temperatureFound = true;
                    break;

            }
            list.add(new TellstickDecodedEntry(device, dataObj));
        }
        // Add new sensors if we did not find a registered one
        if (!temperatureFound)
            list.add(new TellstickDecodedEntry(
                    new Oregon0x1A2D(address, OregonSensorType.TEMPERATURE),
                    new TemperatureSensorData(temperature, timestamp)));
        if (!humidityFound && humidity>0.0)
            list.add(new TellstickDecodedEntry(
                    new Oregon0x1A2D(address, OregonSensorType.HUMIDITY),
                    new HumiditySensorData(humidity, timestamp)));

        return list;
    }
}
