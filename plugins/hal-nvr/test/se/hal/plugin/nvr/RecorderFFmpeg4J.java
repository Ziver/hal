/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2021 Ziver Koc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package se.hal.plugin.nvr;


import com.github.manevolent.ffmpeg4j.FFmpeg;
import com.github.manevolent.ffmpeg4j.FFmpegException;
import com.github.manevolent.ffmpeg4j.FFmpegIO;
import com.github.manevolent.ffmpeg4j.FFmpegInput;
import com.github.manevolent.ffmpeg4j.filter.audio.AudioFilter;
import com.github.manevolent.ffmpeg4j.filter.audio.FFmpegAudioResampleFilter;
import com.github.manevolent.ffmpeg4j.filter.video.FFmpegVideoRescaleFilter;
import com.github.manevolent.ffmpeg4j.filter.video.VideoFilter;
import com.github.manevolent.ffmpeg4j.source.AudioSourceSubstream;
import com.github.manevolent.ffmpeg4j.source.VideoSourceSubstream;
import com.github.manevolent.ffmpeg4j.stream.output.FFmpegTargetStream;
import com.github.manevolent.ffmpeg4j.stream.source.FFmpegSourceStream;
import com.github.manevolent.ffmpeg4j.transcoder.Transcoder;
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;

import java.io.FileOutputStream;
import java.util.logging.Level;


public class RecorderFFmpeg4J {

    public static void main(String[] args) throws Exception {
        LogUtil.setGlobalLevel(Level.ALL);
        LogUtil.setGlobalFormatter(new CompactLogFormatter());

        FFmpegIO input = FFmpegIO.openNativeUrlInput("rtsp://admin:TCZRTY@192.168.10.223:554/H.264");
        FFmpegIO output = FFmpegIO.openOutputStream(new FileOutputStream("./video.mp4"), FFmpegIO.DEFAULT_BUFFER_SIZE);

        // Open input
        FFmpegSourceStream sourceStream = new FFmpegInput(input).open(FFmpeg.getInputFormatByName("h264"));
        sourceStream.registerStreams(); // Read the file header, and register substreams in FFmpeg4j

        FFmpegTargetStream targetStream = new FFmpegTargetStream(
                "h264", // Output format
                output,
                new FFmpegTargetStream.FFmpegNativeOutput()
        );

        // Audio
        AudioSourceSubstream inoutAudioSubstream =
                (AudioSourceSubstream)
                        sourceStream.getSubstreams().stream().filter(x -> x instanceof AudioSourceSubstream)
                                .findFirst().orElse(null);

        AudioFilter audioFilter = new FFmpegAudioResampleFilter(
                inoutAudioSubstream.getFormat(),
                null,
                FFmpegAudioResampleFilter.DEFAULT_BUFFER_SIZE
        );

        // Video
        VideoSourceSubstream inputVideoSubstream =
                (VideoSourceSubstream)
                        sourceStream.getSubstreams().stream().filter(x -> x instanceof VideoSourceSubstream)
                                .findFirst().orElse(null);

        VideoFilter videoFilter = new FFmpegVideoRescaleFilter(
                inputVideoSubstream.getFormat(),
                null,
                sourceStream.getPixelFormat()
        );

        if (targetStream.getSubstreams().size() <= 0)
            throw new FFmpegException("No substreams to record");

        Transcoder.convert(sourceStream, targetStream, audioFilter, videoFilter, 2D);
    }
}