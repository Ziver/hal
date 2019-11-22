package se.hal.plugin.zwave;

import org.zwave4j.*;
import se.hal.HalContext;
import se.hal.intf.*;
import se.hal.struct.AbstractDevice;
import zutil.log.CompactLogFormatter;
import zutil.log.LogUtil;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author zagumennikov
 */
public class HalZWaveControllerTest {

    public static void main(String[] args) throws IOException {
        LogUtil.setGlobalFormatter(new CompactLogFormatter());
        LogUtil.setGlobalLevel(Level.ALL);

        HalZWaveController controller = new HalZWaveController();
        controller.initialize(
                "/dev/serial/by-id/usb-0658_0200-if00",
                "./plugins/hal-zwave/config");

        System.out.println("Press ENTER to exit application.");
        System.in.read();

        controller.close();
    }
}
