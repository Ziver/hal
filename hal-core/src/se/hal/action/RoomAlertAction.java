package se.hal.action;

import se.hal.HalContext;
import se.hal.intf.HalAction;
import se.hal.struct.Room;
import se.hal.util.ConfigSensorValueProvider;
import se.hal.util.RoomValueProvider;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.ui.UserMessageManager.MessageTTL;
import zutil.ui.conf.Configurator;

import java.util.logging.Logger;

import static zutil.ui.UserMessageManager.MessageLevel;
import static zutil.ui.UserMessageManager.UserMessage;

/**
 * Action that will alert users with a message
 */
public class RoomAlertAction implements HalAction {
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable(value = "Target Room", valueProvider = RoomValueProvider.class)
    private Room room;
    @Configurator.Configurable("Alert Severity")
    private MessageLevel severity = MessageLevel.INFO;
    @Configurator.Configurable("Alert Title")
    private String title = "";


    @Override
    public void execute() {
        if (room != null) {
            room.setRoomAlert(new UserMessage(severity, title, MessageTTL.DISMISSED));
        } else {
            HalContext.getUserMessageManager().add(new UserMessage(MessageLevel.WARNING, "Room not defined for room alert.", MessageTTL.ONE_VIEW));
        }
    }


    public String toString(){
        return "Generate Room Alert: " + severity + ": " + title;
    }
}
