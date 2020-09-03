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

package se.hal.plugin.tellstick;

import se.hal.plugin.tellstick.TellstickProtocol.TellstickDecodedEntry;
import se.hal.plugin.tellstick.protocol.NexaSelfLearningProtocol;
import se.hal.plugin.tellstick.protocol.Oregon0x1A2DProtocol;
import zutil.converter.Converter;
import zutil.log.LogUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Protocol Specification: http://developer.telldus.com/doxygen/TellStick.html
 */
public class TellstickParser {
    private static final Logger logger = LogUtil.getLogger();
    private static HashMap<String, TellstickProtocol> protocolMap;

    static {
        registerProtocol(NexaSelfLearningProtocol.class);
        registerProtocol(Oregon0x1A2DProtocol.class);
    }

    private int firmwareVersion = -1;


    /**
     * Will decode the given data and return a list with device and data objects
     *
     * @param data
     * @return a list with decoded objects or empty list if there was an error
     */
    public List<TellstickDecodedEntry> decode(String data) {
        if (data.startsWith("+W")) {
            data = data.substring(2);
            HashMap<String, String> map = new HashMap<String, String>();
            String[] parameters = data.split(";");
            for (String parameter : parameters) {
                String[] keyValue = parameter.split(":");
                map.put(keyValue[0], keyValue[1]);
            }

            TellstickProtocol protocol =
                    getProtocolInstance(map.get("protocol"), map.get("model"));
            if (protocol != null) {
                String binData = map.get("data");

                logger.finest("Decoding: " + protocol);
                List<TellstickDecodedEntry> list = protocol.decode(Converter.hexToByte(binData));
                if (list == null)
                    list = Collections.EMPTY_LIST;
                return list;
            } else {
                logger.warning("Unknown protocol: " + data);
            }
        } else if (data.startsWith("+S") || data.startsWith("+T")) {
            // This is confirmation of send commands
            synchronized (this) {
                this.notifyAll();
            }
        } else if (data.startsWith("+V")) {
            if (data.length() > 2)
                firmwareVersion = Integer.parseInt(data.substring(2));
        }else {
            logger.severe("Unknown prefix: " + data);
        }

        return Collections.EMPTY_LIST;
    }

    /**
     * This method blocks until a send command confirmation is received.
     */
    public synchronized void waitSendConformation(){
        try {
            this.wait();
        } catch (InterruptedException e) {
            logger.log(Level.SEVERE, null, e);
        }
    }


    public int getFirmwareVersion() {
        return firmwareVersion;
    }



    public static void registerProtocol(Class<? extends TellstickProtocol> protClass) {
        try {
            registerProtocol(protClass.newInstance());
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }
    public static void registerProtocol(TellstickProtocol protObj) {
        if (protocolMap == null)
            protocolMap = new HashMap<>();
        protocolMap.put(
                protObj.getProtocolName() + "-" + protObj.getModelName(),
                protObj);
    }

    public static TellstickProtocol getProtocolInstance(String protocol, String model) {
        return protocolMap.get(protocol + "-" + model);
    }
}
