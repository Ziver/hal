package se.hal.plugin.tellstick.protocols;

import se.hal.intf.HalSensorData;
import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.struct.PowerConsumptionSensorData;
import zutil.log.LogUtil;
import zutil.ui.Configurator;

import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-11-19.
 */
public class Oregon0x1A2D extends TellstickProtocol implements PowerConsumptionSensorData {
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable("Address")
    private int address = 0;
    @Configurator.Configurable("Report Interval(ms)")
    private int interval = 60*1000; // default 1 min

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

    @Override
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
    public long getDataInterval() {
        return interval;
    }

    @Override
    public AggregationMethod getAggregationMethod() {
        return AggregationMethod.SUM;
    }

}
