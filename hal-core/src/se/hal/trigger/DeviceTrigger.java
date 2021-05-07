package se.hal.trigger;

import se.hal.TriggerManager;
import se.hal.intf.*;
import zutil.ui.conf.Configurator;
import zutil.ui.conf.Configurator.PostConfigurationActionListener;
import zutil.ui.conf.Configurator.PreConfigurationActionListener;

/**
 * An abstract class that implements generic device data logic
 */
public abstract class DeviceTrigger implements HalTrigger,
        PreConfigurationActionListener,
        PostConfigurationActionListener,
        HalDeviceReportListener<HalDeviceConfig,HalDeviceData> {

    @Configurator.Configurable("Trigger only on change")
    protected boolean triggerOnChange = true;
    @Configurator.Configurable("Data to compare to")
    protected double expectedData;

    private transient HalDeviceData receivedData;



    @Override
    public void preConfigurationAction(Configurator configurator, Object obj) {
        HalAbstractDevice device = getDevice();
        if (device != null)
            device.removeReportListener(this);
        reset();
    }
    @Override
    public void postConfigurationAction(Configurator configurator, Object obj) {
        HalAbstractDevice device = getDevice();
        if (device != null)
            device.addReportListener(this);
    }

    @Override
    public void reportReceived(HalDeviceConfig deviceConfig, HalDeviceData deviceData) {
        receivedData = deviceData;
        // Instant trigger evaluation
        if (triggerOnChange)
            TriggerManager.getInstance().evaluateAndExecute();
    }


    @Override
    public boolean evaluate() {
        if (receivedData != null)
            return expectedData == receivedData.getData();
        return false;
    }

    @Override
    public void reset() {
        if (triggerOnChange) // only reset if we want to trigger on change
            receivedData = null;
    }



    protected abstract HalAbstractDevice getDevice();
}
