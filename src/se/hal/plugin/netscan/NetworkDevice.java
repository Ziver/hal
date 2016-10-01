package se.hal.plugin.netscan;

import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventController;
import se.hal.intf.HalEventData;
import se.hal.struct.devicedata.SwitchEventData;
import zutil.ui.Configurator;

import java.net.InetAddress;

/**
 * Created by Ziver on 2016-10-02.
 */
public class NetworkDevice implements HalEventConfig {

    @Configurator.Configurable("IP Address")
    private String ip;



    public NetworkDevice() { }
    public NetworkDevice(String hostAddress) {
        this.ip = hostAddress;
    }


    public String getIp() {
        return ip;
    }

    @Override
    public String toString(){
        return "IP: "+ip;
    }
    @Override
    public boolean equals(Object obj){
        if (obj instanceof NetworkDevice)
            return ip != null && ip.equals(((NetworkDevice) obj).ip);
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
