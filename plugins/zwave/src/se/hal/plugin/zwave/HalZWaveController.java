package se.hal.plugin.zwave;

import com.whizzosoftware.wzwave.controller.ZWaveController;
import com.whizzosoftware.wzwave.controller.ZWaveControllerListener;
import com.whizzosoftware.wzwave.controller.netty.NettyZWaveController;
import com.whizzosoftware.wzwave.node.NodeInfo;
import com.whizzosoftware.wzwave.node.ZWaveEndpoint;
import se.hal.HalContext;
import se.hal.intf.*;
import zutil.log.LogUtil;

import java.util.logging.Logger;

/**
 *
 */
public class HalZWaveController implements HalSensorController, HalEventController,
        HalAutoScannableController, ZWaveControllerListener {
    public static final Logger logger = LogUtil.getLogger();
    private ZWaveController controller;


    public static void main(String[] args) {
        NettyZWaveController zwave = new NettyZWaveController("COM3", new HashMapPersistentStore());
        zwave.setListener(new ZWaveControllerListener(){
            @Override
            public void onZWaveNodeAdded(ZWaveEndpoint node) {
                System.out.println("onZWaveNodeAdded: "+ node);
            }

            @Override
            public void onZWaveNodeUpdated(ZWaveEndpoint node) {
                System.out.println("onZWaveNodeUpdated: "+ node);
            }

            @Override
            public void onZWaveConnectionFailure(Throwable t) {
                System.out.println("onZWaveConnectionFailure: "+ t);
            }

            @Override
            public void onZWaveControllerInfo(String libraryVersion, Integer homeId, Byte nodeId) {
                System.out.println("onZWaveControllerInfo: "+ libraryVersion+" "+homeId+" "+nodeId);
            }

            @Override
            public void onZWaveInclusionStarted() {
                System.out.println("onZWaveInclusionStarted");
            }

            @Override
            public void onZWaveInclusion(NodeInfo nodeInfo, boolean success) {
                System.out.println("onZWaveInclusion: "+ nodeInfo + " "+success);
            }

            @Override
            public void onZWaveInclusionStopped() {
                System.out.println("onZWaveInclusionStopped");
            }

            @Override
            public void onZWaveExclusionStarted() {
                System.out.println("onZWaveExclusionStarted");
            }

            @Override
            public void onZWaveExclusion(NodeInfo nodeInfo, boolean success) {
                System.out.println("onZWaveExclusion: "+ nodeInfo + " "+success);
            }

            @Override
            public void onZWaveExclusionStopped() {
                System.out.println("onZWaveExclusionStopped");
            }
        });
        zwave.start();
    }


    @Override
    public boolean isAvailable() {
        return HalContext.getStringProperty("zwave.com_port") != null;
    }
    @Override
    public void initialize() {
        controller = new NettyZWaveController(
                HalContext.getStringProperty("zwave.com_port"),
                new HashMapPersistentStore());
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
    public void onZWaveNodeAdded(ZWaveEndpoint node) {
        logger.finest("onZWaveNodeAdded: "+ node);
    }

    @Override
    public void onZWaveNodeUpdated(ZWaveEndpoint node) {
        logger.finest("onZWaveNodeUpdated: "+ node);
    }

    @Override
    public void onZWaveConnectionFailure(Throwable t) {
        logger.finest("onZWaveConnectionFailure: "+ t);
    }

    @Override
    public void onZWaveControllerInfo(String libraryVersion, Integer homeId, Byte nodeId) {
        logger.finest("onZWaveControllerInfo: "+ libraryVersion+" "+homeId+" "+nodeId);
    }

    @Override
    public void onZWaveInclusionStarted() {
        logger.finest("onZWaveInclusionStarted");
    }

    @Override
    public void onZWaveInclusion(NodeInfo nodeInfo, boolean success) {
        logger.finest("onZWaveInclusion: "+ nodeInfo + " "+success);
    }

    @Override
    public void onZWaveInclusionStopped() {
        logger.finest("onZWaveInclusionStopped");
    }

    @Override
    public void onZWaveExclusionStarted() {
        logger.finest("onZWaveExclusionStarted");
    }

    @Override
    public void onZWaveExclusion(NodeInfo nodeInfo, boolean success) {
        logger.finest("onZWaveExclusion: "+ nodeInfo + " "+success);
    }

    @Override
    public void onZWaveExclusionStopped() {
        logger.finest("onZWaveExclusionStopped");
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
