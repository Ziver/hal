/*
 * Copyright (c) 2013 ezivkoc
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

import com.darkprograms.speech.synthesiser.SynthesiserV2;
import javafx.embed.swing.JFXPanel;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import se.koc.hal.intf.HalTextToSpeach;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * Created with IntelliJ IDEA.
 * User: ezivkoc
 * Date: 2013-12-17
 * Time: 13:45
 */
public class GoogleTTSClient implements HalTextToSpeach {
    private static final String API_KEY = "AIzaSyBGAQ29aMvts9MObj739_HYa-tvNeEI0X8";

    private SynthesiserV2 synthesiser;
    private File tmpAudioFile;


    public void initTTS() {
        try {
            // JavaFX should be initialized
            JFXPanel fxPanel = new JFXPanel();

            synthesiser = new SynthesiserV2(API_KEY);
            tmpAudioFile = File.createTempFile("tts", "tmp");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void speak(String msg) {
        try{
            String language = synthesiser.detectLanguage(msg);

            InputStream is = synthesiser.getMP3Data(msg);
            BufferedInputStream buff = new BufferedInputStream(is);
            DataInputStream di = new DataInputStream(buff);


            FileOutputStream fos = new FileOutputStream(tmpAudioFile);
            while(di.available() != 0){
                fos.write(di.readByte());
            }
            fos.close();

            URL resource = tmpAudioFile.toURI().toURL();
            Media media = new Media(resource.toURI().toString());
            final MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.play();

            mediaPlayer.setOnEndOfMedia(new Runnable() {
                @Override public void run() {
                    mediaPlayer.stop();
                }
            });
            while(mediaPlayer.getStatus() != MediaPlayer.Status.STOPPED){
                try{Thread.sleep(200);}catch(Exception e){}
            }

        }catch(URISyntaxException e){
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
