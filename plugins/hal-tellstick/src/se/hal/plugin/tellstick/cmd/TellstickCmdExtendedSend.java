package se.hal.plugin.tellstick.cmd;

import java.nio.charset.StandardCharsets;

/**
 * Created by Ziver on 2016-08-29.
 */
public class TellstickCmdExtendedSend implements TellstickCmd{
    private static int OFFSET_TIMINGS = 1;
    private static int OFFSET_PULSE_LENGTH = 5;
    private static int OFFSET_PULSES = 6;

    private byte[] cmd = new byte[79];
    private int length = 0;


    /**
     * @param   timing    set first timing in us
     * @return an instance of itself
     */
    public TellstickCmdExtendedSend setPulls0Timing(int timing) {
        setPullsTiming(0, timing); return this;
    }
    /**
     * @param   timing    set second timing in us
     * @return an instance of itself
     */
    public TellstickCmdExtendedSend setPulls1Timing(int timing) {
        setPullsTiming(1, timing); return this;
    }
    /**
     * @param   timing    set third timing in us
     * @return an instance of itself
     */
    public TellstickCmdExtendedSend setPulls2Timing(int timing) {
        setPullsTiming(2, timing); return this;
    }
    /**
     * @param   timing    set fourth timing in us
     * @return an instance of itself
     */
    public TellstickCmdExtendedSend setPulls3Timing(int timing) {
        setPullsTiming(3, timing); return this;
    }
    /**
     * @param   i         an index from 0 to 3
     * @param   timing    set first pulls length in us between 0-2550
     * @return an instance of itself
     */
    private void setPullsTiming(int i, int timing) { // TODO: should probably have high and low timing to be called pulls
        if (0 > timing || timing > 2550)
            throw new IllegalArgumentException("Invalid pulls "+timing+" must be between 0-2550" );
        cmd[OFFSET_TIMINGS + i] = (byte)(timing/10);
    }


    public TellstickCmdExtendedSend addPulls0() {
        addPulls(0); return this;
    }
    public TellstickCmdExtendedSend addPulls1() {
        addPulls(1); return this;
    }
    public TellstickCmdExtendedSend addPulls2() {
        addPulls(2); return this;
    }
    public TellstickCmdExtendedSend addPulls3() {
        addPulls(3); return this;
    }
    private void addPulls(int i) {
        if (OFFSET_PULSES+(length/4) > cmd.length)
            throw new IndexOutOfBoundsException("Maximum length "+cmd.length+" reached");
        switch (length % 4) {
            case 0:
                cmd[OFFSET_PULSES+ length/4] |= 0b1100_0000 & (i << 6); break;
            case 1:
                cmd[OFFSET_PULSES+ length/4] |= 0b0011_0000 & (i << 4); break;
            case 2:
                cmd[OFFSET_PULSES+ length/4] |= 0b0000_1100 & (i << 2); break;
            case 3:
                cmd[OFFSET_PULSES+ length/4] |= 0b0000_0011 & (i); break;
        }
        length++;
    }


    public String getTransmissionString() {
        cmd[0] = 'T';
        cmd[OFFSET_PULSE_LENGTH] = (byte)length;
        cmd[OFFSET_PULSES+(int)Math.ceil(length/4.0)] = '+';
        return new String(cmd, 0, OFFSET_PULSES+(int)Math.ceil(length/4.0)+1, StandardCharsets.ISO_8859_1);
    }
}
