package se.hal.plugin.dummy;

import se.hal.intf.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class DummyController implements HalSensorController, HalEventController, Runnable {
    private List<DummyDevice> registeredDevices = new ArrayList();
    private ScheduledExecutorService executor;
    private HalSensorReportListener sensorListener;
    private HalEventReportListener eventListener;


    public DummyController() {}


    @Override
    public void initialize() {
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(this, 0, 60, TimeUnit.SECONDS);
    }


    @Override
    public void run() {
        if (registeredDevices != null) {
            for (DummyDevice device : registeredDevices) {
                if (sensorListener != null) {
                    HalDeviceData data = device.generateData();

                    if (data instanceof HalSensorData) {
                        sensorListener.reportReceived((HalSensorConfig) device, (HalSensorData) data);
                    } else if (data instanceof HalEventData) {
                        eventListener.reportReceived((HalEventConfig) device, (HalEventData) data);
                    }
                }
            }
        }
    }

    @Override
    public void register(HalSensorConfig sensorConfig) {
        if (sensorConfig instanceof DummyDevice) {
            registeredDevices.add((DummyDevice) sensorConfig);
        }
    }

    @Override
    public void register(HalEventConfig eventConfig) {
        if (eventConfig instanceof DummyDevice) {
            registeredDevices.add((DummyDevice) eventConfig);
        }
    }

    @Override
    public void deregister(HalSensorConfig sensorConfig) {
        registeredDevices.remove(sensorConfig);
    }

    @Override
    public void deregister(HalEventConfig eventConfig) {
        registeredDevices.remove(eventConfig);
    }

    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) {
        // Nothing to do as this is a dummy
    }

    @Override
    public int size() {
        return registeredDevices.size();
    }

    @Override
    public void setListener(HalSensorReportListener listener) { sensorListener = listener; }

    @Override
    public void setListener(HalEventReportListener listener) { eventListener = listener; }

    @Override
    public void close() {
        executor.shutdown();
    }
}
