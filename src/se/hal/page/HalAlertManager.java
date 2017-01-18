package se.hal.page;

import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ezivkoc on 2016-01-20.
 */
public class HalAlertManager implements HttpPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TMPL_PATH = "resource/web/main_alerts.tmpl";
    private static final String PAGE_NAME = "alert";
    private static HalAlertManager instance;

    public enum AlertLevel{
        ERROR,
        WARNING,
        SUCCESS,
        INFO
    }
    public enum AlertTTL{
        ONE_VIEW,
        DISMISSED
    }

    private List<HalAlert> alerts = new LinkedList<>();


    private HalAlertManager(){}

    public String getUrl(){
        return "/"+PAGE_NAME;
    }

    public void addAlert(HalAlert alert){
        alerts.remove(alert); // We don't want to flood the user with duplicate alerts
        alerts.add(alert);
    }

    public Templator generateAlerts(){
        try {
            // clone alert list and update ttl of alerts
            List<HalAlert> alertsClone = new ArrayList<>(alerts.size());
            for(Iterator<HalAlert> it = alerts.iterator(); it.hasNext(); ){
                HalAlert alert = it.next();
                alertsClone.add(alert);
                alert.ttl--;
                if(alert.ttl <= 0) { // if alert is to old, remove it
                    logger.fine("Alert dismissed with end of life, alert id: "+ alert.id);
                    it.remove();
                }
            }

            Templator tmpl = new Templator(FileUtil.find(TMPL_PATH));
            tmpl.set("url", getUrl());
            tmpl.set("alerts", alertsClone);
            return tmpl;
        }catch (IOException e){
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

        if (request.containsKey("action")){
            if (request.get("action").equals("dismiss")){
                // parse alert id
                int id = Integer.parseInt(request.get("id"));
                //  Find alert
                for(Iterator<HalAlert> it = alerts.iterator(); it.hasNext(); ){
                    HalAlert alert = it.next();
                    if(alert.getId() == id) {
                        logger.fine("User dismissed alert id: "+ id);
                        it.remove();
                        break;
                    }
                }
            }
        }
    }



    public static void initialize(){
        instance = new HalAlertManager();
    }
    public static HalAlertManager getInstance(){
        return instance;
    }


    public static class HalAlert{
        private static int nextId = 0;

        private int id;
        private AlertLevel level;
        private String msg;
        protected int ttl;

        public HalAlert(AlertLevel level, String msg, AlertTTL ttl) {
            this.id = nextId++;
            this.level = level;
            this.msg = msg;
            switch (ttl){
                case ONE_VIEW:  this.ttl = 1; break;
                case DISMISSED: this.ttl = Integer.MAX_VALUE; break;
            }
        }


        public int getId() {
            return id;
        }

        public AlertLevel getLevel() {
            return level;
        }
        public boolean isError(){   return level == AlertLevel.ERROR; }
        public boolean isWarning(){ return level == AlertLevel.WARNING; }
        public boolean isSuccess(){ return level == AlertLevel.SUCCESS; }
        public boolean isInfo(){    return level == AlertLevel.INFO; }

        public String getMessage() {
            return msg;
        }

        @Override
        public boolean equals(Object obj){
            if (obj instanceof HalAlert)
                return level == ((HalAlert) obj).level &&
                        msg.equals(((HalAlert) obj).msg);
            return false;
        }
    }
}
