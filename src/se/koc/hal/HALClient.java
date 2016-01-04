package se.koc.hal;

import se.koc.hal.bot.AliceBot;
import se.koc.hal.intf.HalBot;
import se.koc.hal.intf.HalSpeachToText;
import se.koc.hal.intf.HalTextToSpeach;
import se.koc.hal.struct.SwitchEvent;
import se.koc.hal.stt.ManualSTTClient;
import se.koc.hal.tts.MaryRemoteTTSClient;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created with IntelliJ IDEA.
 * User: ezivkoc
 * Date: 2013-12-17
 * Time: 10:59
 */
public class HalClient {
    private static HashMap<String, SwitchEvent> switches = new HashMap<String, SwitchEvent>();


    public static void main(String[] args){
        System.setProperty("org.apache.commons.logging.log", "org.apache.commons.logging.impl.NoOpLog");
        org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);

        /********************************************************************/
        System.out.println("Initializing STT...");
        HalSpeachToText stt =
                new ManualSTTClient();
        //        new GoogleSTTClient();
        //        new Sphinx4STTClient();
        stt.initSTT();

        /********************************************************************/
        System.out.println("Initializing TTS...");

        final HalTextToSpeach tts =
        //        new JarvisTTSClient();
                new MaryRemoteTTSClient();
        //        new MaryLocalTTSClient();
        tts.initTTS();

        /********************************************************************/
        System.out.println("Initializing BOT...");
        HalBot bot = new AliceBot();
        bot.initialize();

        /********************************************************************/
/*        NexaSelfLearning nexa1 = new NexaSelfLearning();
        nexa1.setHouse(15087918);
        nexa1.setUnit(0);
        switches.put("livingroom", new SwitchEvent("livingroom", nexa1));

        NexaSelfLearning nexa2 = new NexaSelfLearning();
        nexa2.setHouse(15087918);
        nexa2.setUnit(1);
        switches.put("bedroom", new SwitchEvent("bedroom", nexa2));

        NexaSelfLearning nexa3 = new NexaSelfLearning();
        nexa3.setHouse(15087918);
        nexa3.setUnit(3);
        switches.put("kitchen", new SwitchEvent("kitchen", nexa3));

        TellstickSerialComm.getInstance().setListener(new TellstickChangeListener() {
            @Override
            public void stateChange(TellstickProtocol protocol) {
                for(SwitchEvent s : switches.values()) {
                    if(s.equals(protocol)) {
                        String response = s.getName()+" window is "+(((NexaSelfLearning)protocol).isEnabled() ? "open": "closed");
                        System.out.println(">>> " + response);
                        tts.speak(response);
                        return;
                    }
                }
            }
        });
*/

        System.out.println("Listening...");
        while(true){
            // Listen to input
            System.out.print("<<< ");
            String request = stt.listen();
            System.out.println(request);

            String response = doActions(request);

            if(response == null) {
                // Bot answer
                response = bot.respond(request);
            }
            System.out.println(">>> " + response);

            // Cleanup response and Speak
            response = response.replaceAll("\\<.*?>","");
            tts.speak(response);
        }
    }

    private static Pattern pattern_isOn = Pattern.compile("\\bis (\\w*)\\b.*(on|off)");
    private static Pattern pattern_turnOn = Pattern.compile("\\bturn on (\\w*)\\b");
    private static Pattern pattern_turnOff = Pattern.compile("\\bturn off (\\w*)\\b");
    private static String doActions(String request) {
        if(request == null)
            return null;
        Matcher m = pattern_isOn.matcher(request);
        if(m.find()){
            String name = m.group(1);
            if(switches.containsKey(name))
                return "It is " + (switches.get(name).isOn() ? "on" : "off");
        }

        m = pattern_turnOn.matcher(request);
        if(m.find()){
            String name = m.group(1);
            if(name.equals("all")){
                for(SwitchEvent s : switches.values())
                    s.turnOn();
                return "I've turned everything on for you";
            }
            else if(switches.containsKey(name)) {
                switches.get(name).turnOn();
                return "Turned "+name+" on";
            }
        }

        m = pattern_turnOff.matcher(request);
        if(m.find()){
            String name = m.group(1);
            if(name.equals("all")){
                for(SwitchEvent s : switches.values())
                    s.turnOff();
                return "I turned everything off";
            }
            else if(switches.containsKey(name)) {
                switches.get(name).turnOff();
                return "I switched "+name+" off";
            }
        }

        return null;
    }
}
