package se.hal.action;

import se.hal.HalContext;
import se.hal.intf.HalAction;
import se.hal.struct.Room;
import se.hal.util.RoomValueProvider;
import zutil.log.LogUtil;
import zutil.ui.UserMessageManager.MessageTTL;
import zutil.ui.conf.Configurator;

import java.util.logging.Logger;

import static zutil.ui.UserMessageManager.MessageLevel;
import static zutil.ui.UserMessageManager.UserMessage;

/**
 * Action that will alert users with a message
 */
public class DismissRoomAlertAction implements HalAction {
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable(value = "Target Room", valueProvider = RoomValueProvider.class)
    private Room room;


    @Override
    public void execute() {
        if (room != null) {
            room.clearRoomAlert();
        } else {
            HalContext.getUserMessageManager().add(new UserMessage(MessageLevel.WARNING, "Room not defined for dismissing alert.", MessageTTL.ONE_VIEW));
        }
    }


    public String toString(){
        if (room != null) {
            return "Dismiss alert for room: " + room.getName();
        }
        return "No room defined.";
    }
}
