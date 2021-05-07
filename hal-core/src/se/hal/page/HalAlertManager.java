package se.hal.page;

import se.hal.HalContext;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;
import zutil.ui.UserMessageManager;
import zutil.ui.UserMessageManager.UserMessage;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HalAlertManager implements HttpPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/main_alerts.tmpl";
    private static final String PAGE_NAME = "alert";
    private static HalAlertManager instance;

    private UserMessageManager messageManager = new UserMessageManager();


    private HalAlertManager() {}


    public String getUrl() {
        return "/" + PAGE_NAME;
    }

    public void addAlert(UserMessage alert) {
        messageManager.add(alert);
    }


    public Templator generateAlerts() {
        try {
            List<UserMessage> messages = messageManager.getMessages();
            for (UserMessage msg : messages) {
                msg.decreaseTTL();
            }

            Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
            tmpl.set("serviceUrl", getUrl());
            tmpl.set("alerts", messages);
            return tmpl;
        } catch (IOException e) {
            logger.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public void respond(HttpPrintStream out,
                        HttpHeader headers,
                        Map<String, Object> session,
                        Map<String, String> cookie,
                        Map<String, String> request) throws IOException {

        if (request.containsKey("action")) {
            if (request.get("action").equals("dismiss")) {
                // parse alert id
                int id = Integer.parseInt(request.get("id"));
                //  Find alert
                UserMessage msg = messageManager.get(id);
                if (msg != null)
                    msg.dismiss();
            }
        }
    }



    public static void initialize() {
        instance = new HalAlertManager();
    }
    public static HalAlertManager getInstance() {
        return instance;
    }
}
