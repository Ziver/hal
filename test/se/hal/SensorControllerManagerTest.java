package se.hal;

import org.junit.Test;
import se.hal.intf.HalSensorConfig;
import se.hal.intf.HalSensorController;
import se.hal.intf.HalSensorData;
import se.hal.intf.HalSensorReportListener;
import se.hal.struct.Sensor;
import se.hal.struct.devicedata.HumiditySensorData;
import se.hal.struct.devicedata.TemperatureSensorData;

import java.util.Collections;

import static org.junit.Assert.*;


public class SensorControllerManagerTest {

    private ControllerManager manager = new ControllerManager();


    @Test
    public void addAvailableSensor(){
        assertEquals(Collections.EMPTY_LIST, manager.getAvailableSensors());

        manager.addAvailableSensor(TestSensor1.class);
        assertEquals(1, manager.getAvailableSensors().size());
        assertTrue(manager.getAvailableSensors().contains(TestSensor1.class));

        manager.addAvailableSensor(TestSensor2.class);
        assertEquals(2, manager.getAvailableSensors().size());
        assertTrue(manager.getAvailableSensors().contains(TestSensor1.class));
        assertTrue(manager.getAvailableSensors().contains(TestSensor2.class));

        // Add duplicate sensor
        manager.addAvailableSensor(TestSensor1.class);
        assertEquals("No duplicate check",2, manager.getAvailableSensors().size());
    }


    @Test
    public void registerUnavailableSensor(){
        assertEquals(Collections.EMPTY_LIST, manager.getAvailableSensors());

        Sensor sensor = new Sensor();
        sensor.setDeviceConfig(new TestSensor1());
        manager.register(sensor);
        assertEquals("No Sensor registered", Collections.EMPTY_LIST, manager.getRegisteredSensors());
    }


    @Test
    public void registerOneSensor() {
        Sensor sensor1 = registerSensor(new TestSensor1());
        assertEquals(1, manager.getRegisteredSensors().size());
        assertTrue(manager.getRegisteredSensors().contains(sensor1));
    }
    @Test
    public void registerTwoSensors(){
        Sensor sensor1 = registerSensor(new TestSensor1());
        Sensor sensor2 = registerSensor(new TestSensor2());
        assertEquals(2, manager.getRegisteredSensors().size());
        assertTrue(manager.getRegisteredSensors().contains(sensor1));
        assertTrue(manager.getRegisteredSensors().contains(sensor2));
    }


    @Test
    public void deregisterSensor(){
        Sensor sensor1 = registerSensor(new TestSensor1());
        manager.deregister(sensor1);
        assertEquals(Collections.EMPTY_LIST, manager.getRegisteredEvents());
    }


    // TODO: TC for reportReceived


    //////////////////////////////////////////////////////////
    private Sensor registerSensor(HalSensorConfig config){
        Sensor sensor = new Sensor();
        sensor.setDeviceConfig(config);
        manager.addAvailableSensor(config.getClass());
        manager.register(sensor);
        return sensor;
    }


    public static class TestSensor1 implements HalSensorConfig {

        @Override
        public long getDataInterval() {
            return 0;
        }

        @Override
        public AggregationMethod getAggregationMethod() {
            return AggregationMethod.AVERAGE;
        }

        @Override
        public Class<? extends HalSensorController> getSensorControllerClass() {
            return TestController.class;
        }

        @Override
        public Class<? extends HalSensorData> getSensorDataClass() {
            return TemperatureSensorData.class;
        }
    }

    public static class TestSensor2 implements HalSensorConfig {

        @Override
        public long getDataInterval() {
            return 0;
        }

        @Override
        public AggregationMethod getAggregationMethod() {
            return AggregationMethod.SUM;
        }

        @Override
        public Class<? extends HalSensorController> getSensorControllerClass() {
            return TestController.class;
        }

        @Override
        public Class<? extends HalSensorData> getSensorDataClass() {
            return HumiditySensorData.class;
        }
    }

    public static class TestController implements HalSensorController{
        int size;

        @Override
        public void initialize() throws Exception { }

        @Override
        public void register(HalSensorConfig sensor) {
            size++;
        }
        @Override
        public void deregister(HalSensorConfig sensor) {
            size--;
        }
        @Override
        public int size() {
            return size;
        }

        @Override
        public void setListener(HalSensorReportListener listener) { }

        @Override
        public void close() { }
    }
}