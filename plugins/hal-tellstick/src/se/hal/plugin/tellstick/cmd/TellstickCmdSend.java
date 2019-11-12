package se.hal.plugin.tellstick.cmd;

import java.nio.charset.StandardCharsets;

/**
 * Created by Ziver on 2016-08-29.
 */
public class TellstickCmdSend implements TellstickCmd{
    private static int OFFSET_PULSES = 1;

    private byte[] cmd = new byte[79];
    private int length = 0;



    /**
     * @param   timing    adds a pulls timing in us between 0-2550
     * @return an instance of itself
     */
    private void addPulls(int timing) { // TODO: should probably have high and low timing to be called pulls
        if (OFFSET_PULSES+length > cmd.length)
            throw new IndexOutOfBoundsException("Maximum length "+cmd.length+" reached");
        if (0 > timing || timing > 2550)
            throw new IllegalArgumentException("Invalid pulls "+timing+" must be between 0-2550" );
        cmd[OFFSET_PULSES+length] = (byte)(timing/10);
        length++;
    }


    public String getTransmissionString(){
        cmd[0] = 'S';
        cmd[OFFSET_PULSES+length] = '+';
        return new String(cmd, 0, OFFSET_PULSES+length+1, StandardCharsets.ISO_8859_1);
    }
}
