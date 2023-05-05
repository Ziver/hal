package se.hal.struct.devicedata;

import org.junit.Test;

import static org.junit.Assert.*;

public class ColorEventDataTest {

    @Test
    public void getData() {
        assertEquals(0.0, new ColorEventData(0, 0, 0, 0).getData(), 0);
        assertEquals(6553600.0, new ColorEventData(100, 0, 0, 0).getData(), 0);
        assertEquals(25600.0, new ColorEventData(0, 100, 0, 0).getData(), 0);
        assertEquals(100.0, new ColorEventData(0, 0, 100, 0).getData(), 0);
        assertEquals(16777215.0, new ColorEventData(255, 255, 255, 0).getData(), 0);
    }

    @Test
    public void setData() {
        ColorEventData colorData = new ColorEventData();
        colorData.setData(0);
        assertEquals(0, colorData.getRed());
        assertEquals(0, colorData.getGreen());
        assertEquals(0, colorData.getBlue());

        colorData.setData(6553600.0);
        assertEquals(100, colorData.getRed());
        assertEquals(0, colorData.getGreen());
        assertEquals(0, colorData.getBlue());

        colorData.setData(25600.0);
        assertEquals(0, colorData.getRed());
        assertEquals(100, colorData.getGreen());
        assertEquals(0, colorData.getBlue());

        colorData.setData(100.0);
        assertEquals(0, colorData.getRed());
        assertEquals(0, colorData.getGreen());
        assertEquals(100, colorData.getBlue());

        colorData.setData(16777215.0);
        assertEquals(255, colorData.getRed());
        assertEquals(255, colorData.getGreen());
        assertEquals(255, colorData.getBlue());
    }
}