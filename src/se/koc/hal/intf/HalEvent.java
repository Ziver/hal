package se.koc.hal.intf;

import zutil.parser.DataNode;

/**
 * Created by Ziver on 2015-12-23.
 */
public interface HalEvent {

    public Class<? extends HalEventController> getController();

}
