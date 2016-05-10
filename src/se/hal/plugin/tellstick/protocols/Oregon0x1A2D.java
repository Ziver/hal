package se.hal.plugin.tellstick.protocols;

import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.struct.PowerConsumptionSensorData;
import zutil.ui.Configurator;

/**
 * Created by Ziver on 2015-11-19.
 */
public class Oregon0x1A2D extends TellstickProtocol implements PowerConsumptionSensorData {

    @Configurator.Configurable("Address")
    private int address = 0;

    private double temperature = 0;
    private double humidity = 0;


    public Oregon0x1A2D(){
        super("oregon", "0x1A2D");
    }

    @Override
    public String encode() {
        return null;
    }

    @Override
    public void decode(byte[] data) {
        //class:sensor;protocol:oregon;model:0x1A2D;data:20BA000000002700;

        // checksum2 not used yet
        // int checksum2 = data[0];
        int checksum1 = data[1];

        int checksum = ((data[2] >> 4) & 0xF) + (data[2] & 0xF);
        int hum1 = data[2] & 0xF;

        checksum += ((data[3] >> 4) & 0xF) + (data[3] & 0xF);
        boolean negative = (data[3] & (1 << 3)) > 0;
        int hum2 = (data[3] >> 4) & 0xF;

        checksum += ((data[4] >> 4) & 0xF) + (data[4] & 0xF);
        int temp2 = data[4] & 0xF;
        int temp1 = (data[4] >> 4) & 0xF;

        checksum += ((data[5] >> 4) & 0xF) + (data[5] & 0xF);
        int temp3 = (data[5] >> 4) & 0xF;

        checksum += ((data[6] >> 4) & 0xF) + (data[6] & 0xF);
        address = data[6] & 0xFF;

        checksum += ((data[7] >> 4) & 0xF) + (data[7] & 0xF);
        // channel not used
        // uint8_t channel = (data[7] >> 4) & 0x7;

        checksum += 0x1 + 0xA + 0x2 + 0xD - 0xA;

        if (checksum != checksum1) {
            return;
        }

        temperature = ((temp1 * 100) + (temp2 * 10) + temp3)/10.0;
        if (negative)
            temperature = -temperature;
        humidity = (hum1 * 10.0) + hum2;

    }

    public boolean equals(Object obj){
        if(! (obj instanceof Oregon0x1A2D))
            return false;
        return ((Oregon0x1A2D)obj).address == this.address;
    }

    public String toString(){
        return "address:"+address+
                ", temperature:"+temperature+
                ", humidity:"+humidity;
    }


    public double getTemperature(){
        return temperature;
    }

    public double getHumidity(){
        return humidity;
    }


    @Override
    public double getData() {
        return temperature;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.SUM;
    }

}
