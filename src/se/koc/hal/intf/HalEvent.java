package se.koc.hal.intf;

/**
 * Created by Ziver on 2015-12-23.
 */
public interface HalEvent {

    public Class<? extends HalEventController> getController();


}
