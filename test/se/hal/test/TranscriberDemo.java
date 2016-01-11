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

package se.hal.test;

import com.darkprograms.speech.microphone.MicrophoneAnalyzer;
import edu.cmu.sphinx.api.Configuration;
import edu.cmu.sphinx.api.SpeechResult;
import edu.cmu.sphinx.api.StreamSpeechRecognizer;
import edu.cmu.sphinx.result.WordResult;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;
import java.io.FileInputStream;

/**
 * A simple example that shows how to transcribe a continuous audio file that
 * has multiple utterances in it.
 */
public class TranscriberDemo {

    public static void main(String[] args) throws Exception {
        System.out.println("Loading models...");

        Configuration configuration = new Configuration();

        // Load model from the jar
        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");

        // You can also load model from folder
        // configuration.setAcousticModelPath("file:en-us");
        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.dmp");

        MicrophoneAnalyzer mic = new MicrophoneAnalyzer(AudioFileFormat.Type.WAVE);
        StreamSpeechRecognizer recognizer = new StreamSpeechRecognizer(configuration);
        File audioFile = File.createTempFile("input", "wav");

        while(true) {
            System.out.println("Listening...");
            mic.captureAudioToFile(audioFile);
            try {
                Thread.sleep(3000);
            } catch (Exception e) {}
            mic.close();
            System.out.println("Done");

            // Simple recognition with generic model
            recognizer.startRecognition(new FileInputStream(audioFile));
            SpeechResult result;
            while ((result = recognizer.getResult()) != null) {
                System.out.format("Hypothesis: %s\n", result.getHypothesis());

                System.out.println("List of recognized words and their times:");
                for (WordResult r : result.getWords()) {
                    System.out.println(r);
                }

                System.out.println("Best 3 hypothesis:");
                for (String s : result.getNbest(3))
                    System.out.println(s);

            }
            recognizer.stopRecognition();
        }
    }
}
