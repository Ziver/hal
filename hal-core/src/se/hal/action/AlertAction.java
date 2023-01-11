package se.hal.action;

import se.hal.HalContext;
import se.hal.intf.HalAction;
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
    @Configurator.Configurable("Alert Title")
    private String title = "";
    @Configurator.Configurable("Alert Description")
    private String description = "";


    @Override
    public void execute() {
        HalContext.getUserMessageManager().add(new UserMessage(severity, title, description, ttl));
    }


    public String toString(){
        return "Generate Alert: " + severity + ": " + title;
    }
}
