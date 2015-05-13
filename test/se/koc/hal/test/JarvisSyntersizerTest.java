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

import com.darkprograms.speech.synthesiser.Synthesiser;
import javafx.application.Application;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;

@SuppressWarnings("restriction")
public class JarvisSyntersizerTest extends Application {

	public static void main(String[] args){
		try {
			Application a = new JarvisSyntersizerTest();
			a.launch();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public JarvisSyntersizerTest(){
		
	}

	/**
	* Returns the URL to the given file
	* 
	* @param path is the path to the file (no / if not absolute path)
	* @return A URL object for the file
	*/
	public static URL findURL(String path){
		return Thread.currentThread().getContextClassLoader().getResource(path);
	}

	@Override
	public void start(Stage arg0) throws Exception {
		Synthesiser synthesiser = new Synthesiser("auto");
		String language = synthesiser.detectLanguage("hi what is your name?");
		System.out.println(language);

		InputStream is = synthesiser.getMP3Data("hi what is your name?");
		BufferedInputStream buff = new BufferedInputStream(is);
		DataInputStream di = new DataInputStream(buff);

		File f = new File("bin\\tmp.mp3");
		FileOutputStream fos = new FileOutputStream(f);
		while(di.available() != 0){
			fos.write(di.readByte());
		}
		fos.close();

		URL resource = findURL("tmp.mp3");
		Media hit = new Media(resource.toURI().toString());
		MediaPlayer mediaPlayer = new MediaPlayer(hit);
		mediaPlayer.play();
		
	} 


}
