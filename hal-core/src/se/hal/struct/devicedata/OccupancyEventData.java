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


public class OccupancyEventData extends HalEventData {

    private boolean isOccupied;


    public OccupancyEventData() { }
    public OccupancyEventData(boolean isOccupied, long timestamp) {
        this.isOccupied = isOccupied;
        this.setTimestamp(timestamp);
    }

    public void setOccupied(){
        this.isOccupied = true;
    }
    public void setOccupied(boolean isOccupied){
        this.isOccupied = isOccupied;
    }
    public void setUnoccupied(){
        this.isOccupied = false;
    }
    public void toggle(){
        isOccupied = !isOccupied;
    }

    public boolean isOccupied(){
        return isOccupied;
    }

    @Override
    public String toString(){
        return isOccupied ? "Occupied" : "Unoccupied";
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

    @Override
    public double getData() {
        return (isOccupied ? 1.0 : 0.0);
    }

    @Override
    public void setData(double data) {
        this.isOccupied = data > 0;
    }
}
