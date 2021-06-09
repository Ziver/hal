package se.hal.intf;

/**
 * A interface that indicates that the implementing
 * controller can be auto started when HalServer starts up.
 */
public interface HalAutostartController {

    /**
     * Indicates if the controller has all the configuration
     * data and resources needed to be able to initialize correctly
     */
    boolean isAvailable();
}
