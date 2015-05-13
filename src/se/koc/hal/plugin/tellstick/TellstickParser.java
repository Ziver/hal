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

package se.koc.hal.plugin.tellstick;

import se.koc.hal.plugin.tellstick.protocols.NexaSelfLearning;
import zutil.converters.Converter;

import java.util.HashMap;

/**
 * Created by Ziver on 2015-02-18.
 */
public class TellstickParser {
    private static HashMap<String, Class<? extends TellstickProtocol>> protocolMap;

    static {
        registerProtocol(NexaSelfLearning.class);
    }


    private TellstickProtocol previus;

    public TellstickProtocol decode(String data) {
        if (data.startsWith("+W")) {
            data = data.substring(2);
            HashMap<String, String> map = new HashMap<String, String>();
            String[] parameters = data.split(";");
            for (String parameter : parameters) {
                String[] keyValue = parameter.split(":");
                map.put(keyValue[0], keyValue[1]);
            }

            Class<? extends TellstickProtocol> protClass =
                    getProtocolClass(map.get("protocol"), map.get("model"));
            if (protClass != null) {
                try {
                    TellstickProtocol protocol = protClass.newInstance();
                    String binData = map.get("data");
                    protocol.decode(Converter.hexToByte(binData));
                    if(!protocol.equals(previus)) {
                        previus = protocol;
                        System.out.println("Decoded: " + protocol);
                        return protocol;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("Unknown protocol: " + data);
            }
        } else if (data.startsWith("+S") || data.startsWith("+T")) {
            // This is confirmation of send commands
        }else {
            System.out.println("Unknown prefix: " + data);
        }

        return null;
    }



    public static void registerProtocol(Class<? extends TellstickProtocol> protClass) {
        try {
            if (protocolMap == null)
                protocolMap = new HashMap<String, Class<? extends TellstickProtocol>>();
            TellstickProtocol tmp = protClass.newInstance();
            protocolMap.put(
                    tmp.getProtocolName() + "-" + tmp.getModelName(),
                    protClass);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Class<? extends TellstickProtocol> getProtocolClass(String protocol, String model) {
        return protocolMap.get(protocol + "-" + model);
    }
}
