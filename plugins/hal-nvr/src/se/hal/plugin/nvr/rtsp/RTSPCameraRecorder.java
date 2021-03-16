package se.hal.plugin.nvr.rtsp;

import zutil.log.LogUtil;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A instance of this class will manage a RTSP stream from a specified source.
 */
public class RTSPCameraRecorder implements Runnable {
    private static final Logger logger = LogUtil.getLogger();

    private RTSPCameraConfig camera;


    public RTSPCameraRecorder(RTSPCameraConfig camera) {
        this.camera = camera;
    }


    @Override
    public void run() {
        logger.info("Starting up RTSP Stream recording thread for: " + camera.getRtspUrl());

        try {

        } catch (Exception e) {
            logger.log(Level.SEVERE, "RTSP Stream recording thread has crashed  for: " + camera.getRtspUrl(), e);
        } finally {
            logger.info("Shutting down RTSP Stream recording thread for: " + camera.getRtspUrl());
        }
    }

    public void close() {
        camera = null;
    }
}
