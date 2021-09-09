package se.hal.plugin.dummy;

import se.hal.HalServer;
import se.hal.intf.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class DummyController implements HalSensorController, HalEventController, Runnable, HalDaemon {
    private List<DummyDevice> registeredDevices = new ArrayList();
    private List<HalDeviceReportListener> deviceListeners = new CopyOnWriteArrayList<>();


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

                for (HalDeviceReportListener deviceListener : deviceListeners) {
                    deviceListener.reportReceived(device, data);
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

    @SuppressWarnings("SuspiciousMethodCalls")
    @Override
    public synchronized void deregister(HalDeviceConfig deviceConfig) {
        registeredDevices.remove(deviceConfig);
    }

    @Override
    public synchronized void send(HalEventConfig eventConfig, HalEventData eventData) {
        // Nothing to do as this is a dummy controller
    }

    @Override
    public int size() {
        return registeredDevices.size();
    }

    @Override
    public void addListener(HalDeviceReportListener listener) {
        if (!deviceListeners.contains(listener))
            deviceListeners.add(listener);
    }

    @Override
    public synchronized void close() {
        registeredDevices = new ArrayList<>();
    }
}
