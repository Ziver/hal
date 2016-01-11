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
import com.darkprograms.speech.recognizer.FlacEncoder;
import com.darkprograms.speech.recognizer.GoogleResponse;

import javax.sound.sampled.AudioFileFormat;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarvisRecognizerTest {
	
	public static void main(String[] args){
		try {
			new JarvisRecognizerTest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JarvisRecognizerTest() throws Exception{
		MicrophoneAnalyzer mic = new MicrophoneAnalyzer(AudioFileFormat.Type.WAVE);
		
		File audioFile = new File("bin/tmp.wav");
		
		boolean speaking = false;
		while(!speaking){
			mic.captureAudioToFile(audioFile);
			
			final int THRESHOLD = 10;//YOUR THRESHOLD VALUE.
			mic.open();
			int ambientVolume = mic.getAudioVolume();//
			int speakingVolume = -2;

			do{
				int volume = mic.getAudioVolume();
				System.out.println(volume);
				if(volume>ambientVolume+THRESHOLD){
					speakingVolume = volume;
					speaking = true;
					Thread.sleep(1000);
					System.out.println("SPEAKING");
				}
				if(speaking && volume+THRESHOLD<speakingVolume){
					break;
				}
			}while(speaking);
			mic.close();
		}
		
		
		
		
		
		Thread.sleep(100);
		mic.close();

		
		FlacEncoder flacEncoder = new FlacEncoder();
        File flacFile = new File(audioFile + ".flac");
        flacEncoder.convertWaveToFlac(audioFile, flacFile);
        audioFile.delete();
		
		Path path = Paths.get("bin/tmp.wav.flac");
		
		byte[] data = Files.readAllBytes(path);

		String request = "https://www.google.com/"+
				"speech-api/v1/recognize?"+
				"xjerr=1&client=speech2text&lang=en-US&maxresults=1";
		URL url = new URL(request);
		Proxy proxy =new Proxy(Proxy.Type.HTTP, new InetSocketAddress("www-proxy.ericsson.se", 8080));
		HttpURLConnection connection = (HttpURLConnection) url.openConnection(proxy);          
		connection.setDoOutput(true);
		connection.setDoInput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "audio/x-flac; rate=8000");
		connection.setRequestProperty("User-Agent", "speech2text");
		connection.setConnectTimeout(60000);
		connection.setUseCaches (false);

		DataOutputStream wr = new DataOutputStream(connection.getOutputStream ());
		wr.write(data);
		wr.flush();
		wr.close();
		connection.disconnect();
		flacFile.delete();

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String decodedString = in.readLine();
		GoogleResponse googleResponse = new GoogleResponse();
        parseResponse(decodedString, googleResponse);
		
        System.out.println(googleResponse);
        
	}
	
	private void parseResponse(String rawResponse, GoogleResponse googleResponse) {
        if(rawResponse == null)
        	return;
		if (!rawResponse.contains("utterance"))
            return;

        String array = substringBetween(rawResponse, "[", "]");
        String[] parts = array.split("}");
        
        boolean first = true;
        for( String s : parts ) {
            if( first ) {
                first = false;
                String utterancePart = s.split(",")[0];
                String confidencePart = s.split(",")[1];

                String utterance = utterancePart.split(":")[1];
                String confidence = confidencePart.split(":")[1];

                utterance = stripQuotes(utterance);
                confidence = stripQuotes(confidence);

                if( utterance.equals("null") ) {
                    utterance = null;
                }
                if( confidence.equals("null") ) {
                    confidence = null;
                }

                //googleResponse.setResponse(utterance);
                //googleResponse.setConfidence(confidence);
            } else {
                String utterance = s.split(":")[1];
                utterance = stripQuotes(utterance);
                if( utterance.equals("null") ) {
                    utterance = null;
                }
                googleResponse.getOtherPossibleResponses().add(utterance);
            }
        }
    }
	
	private String substringBetween(String s, String part1, String part2) {
        String sub = null;

        int i = s.indexOf(part1);
        int j = s.indexOf(part2, i + part1.length());

        if (i != -1 && j != -1) {
            int nStart = i + part1.length();
            sub = s.substring(nStart, j);
        }

        return sub;
    }
	
	private String stripQuotes(String s) {
        int start = 0;
        if( s.startsWith("\"") ) {
            start = 1;
        }
        int end = s.length();
        if( s.endsWith("\"") ) {
            end = s.length() - 1;
        }
        return s.substring(start, end);
    }
	
}
