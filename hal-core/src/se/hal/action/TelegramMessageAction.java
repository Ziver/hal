package se.hal.action;

import se.hal.intf.HalAction;
import zutil.net.ws.app.TelegramBot;
import zutil.ui.conf.Configurator;

/**
 * Action that will send a telegram message to a user.
 */
public class TelegramMessageAction implements HalAction {
    @Configurator.Configurable("Bot Token")
    private String token = "";
    @Configurator.Configurable("Chat ID")
    private long chatId = 0;
    @Configurator.Configurable("Message")
    private String message = "";


    @Override
    public void execute() {
        TelegramBot bot = new TelegramBot(token);
        bot.sendMessage(chatId, message);
    }


    public String toString(){
        return "tgram://" + token + "/" + chatId;
    }
}
