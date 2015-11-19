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

package se.koc.hal.test;

import marytts.MaryInterface;
import marytts.client.RemoteMaryInterface;
import marytts.util.data.audio.AudioPlayer;

import javax.sound.sampled.AudioInputStream;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: ezivkoc
 * Date: 2013-12-17
 * Time: 12:39
 */
public class MaryTTS {
    public static void main(String[] args) throws Exception {
        MaryInterface marytts = new RemoteMaryInterface("127.0.0.1", 59125);

        Set<String> voices = marytts.getAvailableVoices();
        marytts.setVoice(voices.iterator().next());
        AudioInputStream audio = marytts.generateAudio("Hello world.");
        AudioPlayer player = new AudioPlayer(audio);
        player.start();
        player.join();
        System.exit(0);
    }
}
