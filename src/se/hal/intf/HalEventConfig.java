package se.hal.intf;

/**
 * Interface representing event type specific configuration data.
 *
 * Created by Ziver on 2015-12-23.
 */
public interface HalEventConfig {

    Class<? extends HalEventController> getEventControllerClass();

    /**
     * @return the class that should be instantiated and used for data received from this event
     */
    Class<? extends HalEventData> getEventDataClass();

    /**
     * This method needs to be implemented.
     * NOTE: it should not compare data and timestamp, only static or unique data for the event type.
     */
    boolean equals(Object obj);

}
