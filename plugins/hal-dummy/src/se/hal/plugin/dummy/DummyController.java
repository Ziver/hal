package se.hal.plugin.dummy;

import se.hal.EventControllerManager;
import se.hal.HalServer;
import se.hal.SensorControllerManager;
import se.hal.intf.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class DummyController implements HalSensorController, HalEventController, Runnable, HalDaemon {
    private List<DummyDevice> registeredDevices = new ArrayList();
    private HalDeviceReportListener sensorListener;
    private HalDeviceReportListener eventListener;


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
    public synchronized void register(HalDeviceConfig deviceConfig) {
        if (deviceConfig instanceof DummyDevice)
            registeredDevices.add((DummyDevice) deviceConfig);
    }

    @Override
    public synchronized void deregister(HalDeviceConfig deviceConfig) {
        registeredDevices.remove(deviceConfig);
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
    public void setListener(HalDeviceReportListener listener) {
        if (listener instanceof SensorControllerManager)
            sensorListener = listener;
        else if (listener instanceof EventControllerManager)
            eventListener = listener;
    }

    @Override
    public synchronized void close() {
        registeredDevices = new ArrayList();
    }
}
