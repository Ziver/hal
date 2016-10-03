package se.hal.plugin.netscan;

import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventController;
import se.hal.intf.HalEventData;
import se.hal.struct.devicedata.SwitchEventData;
import zutil.ui.Configurator;

/**
 * Created by Ziver on 2016-10-02.
 */
public class LocalNetworkDevice implements HalEventConfig {

    @Configurator.Configurable("IP Address")
    private String host;



    public LocalNetworkDevice() { }
    public LocalNetworkDevice(String hostAddress) {
        this.host = hostAddress;
    }


    public String getHost() {
        return host;
    }

    @Override
    public String toString(){
        return "Host: "+ host;
    }
    @Override
    public boolean equals(Object obj){
        if (obj instanceof LocalNetworkDevice)
            return host != null && host.equals(((LocalNetworkDevice) obj).host);
        return false;
    }

    @Override
    public Class<? extends HalEventController> getEventControllerClass() {
        return NetScanController.class;
    }
    @Override
    public Class<? extends HalEventData> getEventDataClass() {
        return SwitchEventData.class;
    }
}
