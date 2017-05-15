package se.hal.plugin.zwave;

import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.controller.netty.NettyZWaveController;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import se.hal.HalContext;
import se.hal.intf.*;

/**
 *
 */
public class ZWaveSerialComm implements HalSensorController, HalEventController, HalAutoScannableController, ZWaveControllerListener {

    private ZWaveController controller;


    @Override
    public boolean isAvailable() {
        return HalContext.getStringProperty("zwave.com_port") != null;
    }
    @Override
    public void initialize() throws Exception {
        controller = new NettyZWaveController(HalContext.getStringProperty("zwave.com_port"));
        controller.setListener(this);
        controller.start();
    }

    @Override
    public void close() {
        controller.stop();
        controller = null;
    }

    ////////////// Z-WAVE CODE ////////////////////////

    @Override
    public void onZWaveNodeAdded(ZWaveEndpoint zWaveEndpoint) {

    }

    @Override
    public void onZWaveNodeUpdated(ZWaveEndpoint zWaveEndpoint) {

    }

    @Override
    public void onZWaveConnectionFailure(Throwable throwable) {

    }


    ////////////// HAL CODE ////////////////////////

    @Override
    public void register(HalSensorConfig sensor) {

    }
    @Override
    public void register(HalEventConfig event) {

    }

    @Override
    public void deregister(HalSensorConfig sensor) {

    }
    @Override
    public void deregister(HalEventConfig event) {

    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void setListener(HalEventReportListener listener) {

    }
    @Override
    public void setListener(HalSensorReportListener listener) {

    }


    @Override
    public void send(HalEventConfig eventConfig, HalEventData eventData) {

    }
}
