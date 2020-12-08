package se.hal.plugin.dummy;

import se.hal.HalServer;
import se.hal.intf.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class DummyController implements HalSensorController, HalEventController, Runnable, HalDaemon {
    private List<DummyDevice> registeredDevices = new ArrayList();
    private HalSensorReportListener sensorListener;
    private HalEventReportListener eventListener;


    public DummyController() {}


    @Override
    public void initialize() {
        HalServer.registerDaemon(this);
    }

    @Override
    public void initiate(ScheduledExecutorService executor) {
        executor.scheduleAtFixedRate(this, 0, 60, TimeUnit.SECONDS);
    }

    @Override
    public synchronized void run() {
        try {
            for (DummyDevice device : registeredDevices) {
                HalDeviceData data = device.generateData();

                if (sensorListener != null && data instanceof HalSensorData) {
                    sensorListener.reportReceived((HalSensorConfig) device, (HalSensorData) data);
                } else if (eventListener != null && data instanceof HalEventData) {
                    eventListener.reportReceived((HalEventConfig) device, (HalEventData) data);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void register(HalSensorConfig sensorConfig) {
        if (sensorConfig instanceof DummyDevice) {
            registeredDevices.add((DummyDevice) sensorConfig);
        }
    }

    @Override
    public synchronized void register(HalEventConfig eventConfig) {
        if (eventConfig instanceof DummyDevice) {
            registeredDevices.add((DummyDevice) eventConfig);
        }
    }

    @Override
    public synchronized void deregister(HalSensorConfig sensorConfig) {
        registeredDevices.remove(sensorConfig);
    }

    @Override
    public synchronized void deregister(HalEventConfig eventConfig) {
        registeredDevices.remove(eventConfig);
    }

    @Override
    public synchronized void send(HalEventConfig eventConfig, HalEventData eventData) {
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
    public synchronized void close() {
        registeredDevices = new ArrayList();
    }
}
