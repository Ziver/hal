package se.hal.action;

import se.hal.intf.HalAction;
import se.hal.page.HalAlertManager;
import zutil.log.LogUtil;
import zutil.ui.UserMessageManager.MessageTTL;
import zutil.ui.conf.Configurator;

import java.util.logging.Logger;

import static zutil.ui.UserMessageManager.*;

/**
 * Action that will alert users with a message
 */
public class AlertAction implements HalAction {
    private static final Logger logger = LogUtil.getLogger();

    @Configurator.Configurable("Alert Severity")
    private MessageLevel severity = MessageLevel.INFO;
    @Configurator.Configurable("Alert Message")
    private MessageTTL ttl = MessageTTL.ONE_VIEW;
    @Configurator.Configurable("Alert Message")
    private String message = "";


    @Override
    public void execute() {
        HalAlertManager.getInstance().addAlert(new UserMessage(severity, message, ttl));
    }


    public String toString(){
        return "Send Alert: " + severity + ": " + message;
    }
}
