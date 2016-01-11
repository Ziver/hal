package se.hal.stt;

import com.darkprograms.speech.microphone.MicrophoneAnalyzer;
import com.darkprograms.speech.recognizer.FlacEncoder;
import com.darkprograms.speech.recognizer.GSpeechDuplex;
import com.darkprograms.speech.recognizer.GSpeechResponseListener;
import com.darkprograms.speech.recognizer.GoogleResponse;
import se.hal.intf.HalSpeachToText;
import zutil.io.file.FileUtil;

import javax.sound.sampled.AudioFileFormat;
import java.io.File;

public class GoogleSTTClient implements HalSpeachToText, GSpeechResponseListener {
	private MicrophoneAnalyzer mic;
	private File audioFile;
    private File flacFile;
    private int ambientVolume;
    private GSpeechDuplex google;
    private String response;
	
	@Override
	public void initSTT() {
        try{
            this.mic = new MicrophoneAnalyzer(AudioFileFormat.Type.WAVE);
		    this.audioFile = File.createTempFile("input", "wav");
            this.flacFile = File.createTempFile("input", "flac");

            this.google = new GSpeechDuplex("AIzaSyBGAQ29aMvts9MObj739_HYa-tvNeEI0X8");
            this.google.addResponseListener(this);

            /*System.out.println("Messuring ambient noise...");
            mic.captureAudioToFile(audioFile);
            Thread.sleep(300);
            ambientVolume = mic.getAudioVolume();
            Thread.sleep(300);
            mic.close();*/
        }catch(Exception e){
            e.printStackTrace();
        }
	}

	@Override
	public synchronized String listen() {
        try {
            String request = null;
            while(request == null){

                /*mic.captureAudioToFile(audioFile);
                try{Thread.sleep(2000);}catch(Exception e){}
                mic.close();*/

                FlacEncoder flacEncoder = new FlacEncoder();
                //flacEncoder.convertWaveToFlac(audioFile, flacFile);
                flacEncoder.convertWaveToFlac(FileUtil.find("edu/cmu/sphinx/demo/speakerid/test.wav"), flacFile);

                response = null;
                google.recognize(flacFile, 8000);
                this.wait();

                flacFile.delete();
                audioFile.delete();
            }

		}catch(Exception e){
			e.printStackTrace();
		}
        return response;
	}


    public String thresholdListen(){
        try{
            boolean speaking = false;
            while(!speaking){
                mic.captureAudioToFile(audioFile);
                System.out.print("_");

                final int THRESHOLD = 10; //YOUR THRESHOLD VALUE.
                mic.open();

                int speakingVolume = -2;

                do{
                    int volume = mic.getAudioVolume();
                    if(volume>ambientVolume+THRESHOLD){
                        speakingVolume = volume;
                        speaking = true;
                        System.out.print(".");
                        Thread.sleep(1000);
                    }
                    if(speaking && volume+THRESHOLD<speakingVolume){
                        break;
                    }
                    Thread.sleep(100);
                }while(speaking);
                mic.close();
            }


            mic.close();

            FlacEncoder flacEncoder = new FlacEncoder();
            flacEncoder.convertWaveToFlac(audioFile, flacFile);

            response = null;
            google.recognize(flacFile, 8000);
            this.wait();

            flacFile.delete();
        }catch(Exception e){
            e.printStackTrace();
        }
        return response;
    }


    @Override
    public void onResponse(GoogleResponse googleResponse) {
        response = googleResponse.getResponse();
        this.notifyAll();
    }
}
