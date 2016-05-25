package se.hal.intf;

/**
 * Created by ezivkoc on 2016-05-25.
 */
public interface HalAutoScannableController {

    /**
     * Indicates if the controller has all the configuration
     * data and resources to be able to initialize
     */
    public boolean isAvailable();
}
