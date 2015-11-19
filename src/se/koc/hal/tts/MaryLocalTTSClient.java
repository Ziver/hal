/*
 * Copyright (c) 2015 Ziver
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

package se.koc.hal.tts;

import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.client.RemoteMaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;
import se.koc.hal.intf.HalTextToSpeach;

import javax.sound.sampled.AudioInputStream;
import java.io.IOException;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ezivkoc
 * Date: 2013-12-17
 * Time: 14:36
 */
public class MaryLocalTTSClient implements HalTextToSpeach {
    private MaryInterface marytts;


    @Override
    public void initTTS() {
        try {
            marytts = new LocalMaryInterface();
            Set<String> voices = marytts.getAvailableVoices();
            marytts.setVoice(voices.iterator().next());
        } catch (MaryConfigurationException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void speak(String msg) {
        try {
            AudioInputStream audio = marytts.generateAudio(msg);
            AudioPlayer player = new AudioPlayer(audio);
            player.start();
            player.join();

        } catch (SynthesisException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
