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

package se.hal.struct.devicedata;

import se.hal.intf.HalEventData;

public class SwitchEventData extends HalEventData {

    private boolean enabled;


    public SwitchEventData() { }
    public SwitchEventData(boolean enabled, long timestamp) {
        this.enabled = enabled;
        this.setTimestamp(timestamp);
    }

    public void turnOn(){
        enabled = true;
    }
    public void turnOff(){
        enabled = false;
    }

    public boolean isOn(){
        return enabled;
    }

    @Override
    public double getData() {
        return (enabled ? 1.0 : 0.0);
    }
    @Override
    public void setData(double enabled) {
        this.enabled = enabled > 0;
    }

    @Override
    public String toString(){
        return enabled ? "ON" : "OFF";
    }
}