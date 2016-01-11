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

import se.hal.intf.HalEventController;
import se.hal.intf.HalSensorController;

/**
 * Created by Ziver on 2015-02-18.
 */
public abstract class TellstickProtocol {

    private String protocol;
    private String model;
    private long timestamp = -1;


    public TellstickProtocol(String protocol, String model){
        this.protocol = protocol;
        this.model = model;
    }


    public String getProtocolName(){
        return protocol;
    }
    public String getModelName(){
        return model;
    }

    public long getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    public Class<? extends HalEventController> getEventController() {
        return TellstickSerialComm.class;
    }
    public Class<? extends HalSensorController> getSensorController() {
        return TellstickSerialComm.class;
    }

    public abstract String encode();
    public abstract void decode(byte[] data);
}
