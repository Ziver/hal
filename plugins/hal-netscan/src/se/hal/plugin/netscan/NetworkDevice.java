package se.hal.plugin.netscan;

import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventController;
import se.hal.intf.HalEventData;
import se.hal.struct.devicedata.OnOffEventData;
import zutil.ui.conf.Configurator;

public class NetworkDevice implements HalEventConfig {

    @Configurator.Configurable("IP Address")
    private String host;



    public NetworkDevice() { }
    public NetworkDevice(String hostAddress) {
        this.host = hostAddress;
    }


    public String getHost() {
        return host;
    }


    @Override
    public Class<? extends HalEventController> getDeviceControllerClass() {
        return NetScanController.class;
    }
    @Override
    public Class<? extends HalEventData> getDeviceDataClass() {
        return OnOffEventData.class;
    }


    @Override
    public boolean equals(Object obj){
        if (obj instanceof NetworkDevice)
            return host != null && host.equals(((NetworkDevice) obj).host);
        return false;
    }
    @Override
    public String toString(){
        return "Host: "+ host;
    }
}
