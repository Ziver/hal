package se.hal.plugin.nvr.rtsp;

import se.hal.HalContext;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.osal.OSALBinaryManager;
import zutil.osal.app.ffmpeg.FFmpeg;
import zutil.osal.app.ffmpeg.FFmpegConstants;
import zutil.osal.app.ffmpeg.FFmpegInput;
import zutil.osal.app.ffmpeg.FFmpegOutput;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A instance of this class will manage a RTSP stream from a specified source.
 */
public class RTSPCameraRecorder implements Runnable {
    private static final Logger logger = LogUtil.getLogger();

    private static final File FFMPEG_BINARY_PATH = FileUtil.find(HalContext.RESOURCE_ROOT + "/resource/bin/");

    private RTSPCameraConfig camera;
    private String storagePath;
    private Process process;


    public RTSPCameraRecorder(RTSPCameraConfig camera, String storagePath) {
        this.camera = camera;
        this.storagePath = storagePath;
    }


    public RTSPCameraConfig getCamera() {
        return camera;
    }


    @Override
    public void run() {
        logger.info("Starting up RTSP Stream recording thread for: " + camera.getRtspUrl());

        while (camera != null) {
            try {
                new File(storagePath).mkdirs();

                // ----------------------------------
                // Setup commandline
                // ----------------------------------

                FFmpegInput ffmpegInput = new FFmpegInput(camera.getRtspUrl());

                FFmpegOutput ffmpegOutput = new FFmpegOutput(new File(storagePath, "stream_%v/stream.m3u8").getPath());
                /*ffmpegOutput.addAdditionalArg("-filter_complex \"[0:v]split=3[v1][v2][v3]; [v1]copy[v1out]; [v2]scale=w=1280:h=720[v2out]; [v3]scale=w=640:h=360[v3out]\"",
                        "-map [v1out] -c:v:0 libx264 -x264-params \"nal-hrd=cbr:force-cfr=1\" -b:v:0 5M -maxrate:v:0 5M -minrate:v:0 5M -bufsize:v:0 10M -preset veryfast -g 25 -sc_threshold 0",
                        "-map [v2out] -c:v:1 libx264 -x264-params \"nal-hrd=cbr:force-cfr=1\" -b:v:1 3M -maxrate:v:1 3M -minrate:v:1 3M -bufsize:v:1 3M -preset veryfast -g 25 -sc_threshold 0",
                        "-map [v3out] -c:v:2 libx264 -x264-params \"nal-hrd=cbr:force-cfr=1\" -b:v:2 1M -maxrate:v:2 1M -minrate:v:2 1M -bufsize:v:2 1M -preset veryfast -g 25 -sc_threshold 0",
                        "-map a:0 -c:a:0 aac -b:a:0 96k -ac 2",
                        "-map a:0 -c:a:1 aac -b:a:1 96k -ac 2",
                        "-map a:0 -c:a:2 aac -b:a:2 48k -ac 2",
                        "-var_stream_map \"v:0,a:0,name:Source v:1,a:1,name:720p v:2,a:2,name:360p\""
                );*/
                ffmpegOutput.addAdditionalArg(
                        "-c:v:0 libx264 -x264-params \"nal-hrd=cbr:force-cfr=1\" -b:v:0 5M -maxrate:v:0 5M -minrate:v:0 5M -bufsize:v:0 10M -preset veryfast -g 25 -sc_threshold 0"
                );
                ffmpegOutput.addAdditionalArg("-f hls",
                        "-hls_time 2", // segment length in seconds
                        //"-hls_playlist_type event", // Do not delete old segments
                        "-hls_flags independent_segments+delete_segments",
                        "-hls_segment_type mpegts",
                        "-hls_segment_filename \"" + new File(storagePath, "stream_%v/data%02d.ts").getPath() + "\"",
                        "-master_pl_name \"playlist.m3u8\""
                );

                FFmpeg ffmpeg = new FFmpeg();
                ffmpeg.setLogLevel(FFmpegConstants.FFmpegLogLevel.ERROR);
                ffmpeg.addInput(ffmpegInput);
                ffmpeg.addOutput(ffmpegOutput);
                String cmdParams = ffmpeg.buildCommand();

                // ----------------------------------
                // Execute command
                // ----------------------------------

                File cmdPath = OSALBinaryManager.getPath(FFMPEG_BINARY_PATH, "ffmpeg");

                String cmd = cmdPath.getParent() + File.separator + cmdParams;
                logger.finest("Executing ffmpeg: " + cmd);

                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        if (process != null) process.destroyForcibly();
                    }
                });

                process = Runtime.getRuntime().exec(cmd);
                BufferedReader output = new BufferedReader(new InputStreamReader(process.getErrorStream()));

                while (process.isAlive()) {
                    String line;
                    while ((line = output.readLine()) != null) {
                        logger.finest("[Cam: " + camera.getRtspUrl() + "] " + line);
                    }

                    Thread.sleep(1000);
                }
                output.close();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "RTSP Stream recording thread has crashed for: " + camera.getRtspUrl(), e);
            } finally {
                logger.info("Shutting down RTSP Stream recording thread for: " + camera.getRtspUrl());
            }

            if (camera != null) {
                try {
                    logger.info("Restarting RTSP thread in 3 seconds.");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {}
            }
        }
    }

    public void close() {
        if (process != null) {
            logger.info("Killing ffmpeg instance.");
            camera = null;
            process.destroy();
        }
    }
}
