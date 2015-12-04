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

package se.koc.hal.struct;

import se.koc.hal.plugin.tellstick.TellstickSerialComm;
import se.koc.hal.plugin.tellstick.protocols.NexaSelfLearning;

/**
 * Created by Ziver on 2015-05-07.
 */
public class Switch {
    private String name;
    private NexaSelfLearning nexa;


    public Switch(String name, NexaSelfLearning nexa){
        this.name = name;
        this.nexa = nexa;
    }

    public String getName() {
        return name;
    }


    public boolean isOn(){
        return nexa.isEnabled();
    }
    public void turnOn(){
        nexa.setEnable(true);
        TellstickSerialComm.getInstance().write(nexa);
    }
    public void turnOff(){
        nexa.setEnable(false);
        TellstickSerialComm.getInstance().write(nexa);
    }

    public boolean equals(Object obj){
        if(obj instanceof String)
            return name.equals(obj);
        if(obj instanceof NexaSelfLearning)
            return nexa.equals(obj);
        return this == obj;
    }
}