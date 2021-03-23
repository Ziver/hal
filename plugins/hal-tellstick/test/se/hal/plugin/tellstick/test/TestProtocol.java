package se.hal.plugin.tellstick.test;

import se.hal.plugin.tellstick.TellstickProtocol;
import se.hal.struct.devicedata.DimmerEventData;
import se.hal.struct.devicedata.TemperatureSensorData;
import zutil.converter.Converter;

import java.util.ArrayList;
import java.util.List;

public class TestProtocol extends TellstickProtocol {

    public TestProtocol() {
        super("test-prot", "test-model");
    }

    @Override
    public List<TellstickDecodedEntry> decode(byte[] data) {
        ArrayList<TellstickDecodedEntry> list = new ArrayList<>();
        int parsedData = Converter.toInt(data);

        if (parsedData > 5000) {
            TestEventDevice device = new TestEventDevice();
            device.testData = Converter.toInt(data);

            list.add(new TellstickDecodedEntry(
                    device, new DimmerEventData(device.testData, System.currentTimeMillis())
            ));
        } else  {
            TestSensorDevice device = new TestSensorDevice();
            device.testData = Converter.toInt(data);

            list.add(new TellstickDecodedEntry(
                    device, new TemperatureSensorData(device.testData, System.currentTimeMillis())
            ));
        }
        return list;
    }
}
