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
import zutil.ColorUtil;
import zutil.converter.Converter;

import java.awt.*;
import java.awt.color.ColorSpace;

/**
 * Represents a color based event.
 */
public class ColorEventData extends HalEventData {
    private static final ColorSpace cieXYZ = ColorSpace.getInstance(ColorSpace.CS_CIEXYZ);

    private int red;
    private int green;
    private int blue;


    public ColorEventData() { }
    /**
     * Create object based on sRGB.
     */
    public ColorEventData(String hex, long timestamp) {
        int[] rgb = ColorUtil.getRgbFromHexString(hex);
        this.red = rgb[0];
        this.green = rgb[1];
        this.blue = rgb[2];
        this.setTimestamp(timestamp);
    }
    /**
     * Create object based on sRGB.
     */
    public ColorEventData(int red, int green, int blue, long timestamp) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.setTimestamp(timestamp);
    }
    /**
     * Create object based on CIE XYZ.
     */
    public static ColorEventData createFromCieXYZ(float x, float y, float z, long timestamp) {
        float[] rgb = cieXYZ.toRGB(new float[]{x, y, z});
        return new ColorEventData((int) rgb[0], (int) rgb[0], (int) rgb[0], timestamp);
    }
    /**
     * Create object based on HLS.
     */
    public static ColorEventData createFromHLS(float h, float l, float s, long timestamp) {
        return null;
    }


    public int getRed() {
        return red;
    }
    public int getGreen() {
        return green;
    }
    public int getBlue() {
        return blue;
    }

    public double getHue() {
        return ColorUtil.getHue(red, green, blue);
    }
    public double getSaturation() {
        return ColorUtil.getSaturation(red, green, blue);
    }
    public double getLightness() {
        return ColorUtil.getLightness(red, green, blue);
    }

    public float[] getCieXYZ() {
        return cieXYZ.fromRGB(new float[]{red, green, blue});
    }

    public String getHex() {
        return ColorUtil.getHexString(red, green, blue);
    }

    @Override
    public String toString(){
        return getHex(); //return "Red: " +  red + ", Green: " + green + ", Blue: " + blue;
    }

    // ----------------------------------------
    // Storage methods
    // ----------------------------------------

    @Override
    public double getData() {
        int rgb = 0;
        rgb |= 0xFF0000 & (red << 16);
        rgb |= 0x00FF00 & (green << 8);
        rgb |= 0x0000FF & (blue);
        return rgb;
    }

    @Override
    public void setData(double data) {
        int rgb = (int) data;
        red   = 0xFF & (rgb >> 16);
        green = 0xFF & (rgb >> 8);
        blue  = 0xFF & (rgb);
    }
}
