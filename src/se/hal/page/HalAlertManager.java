package se.hal.page;

import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.net.http.HttpHeaderParser;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by ezivkoc on 2016-01-20.
 */
public class HalAlertManager implements HttpPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TMPL_PATH = "web-resource/alerts.tmpl";
    private static final String PAGE_NAME = "alert";
    private static HalAlertManager instance;

    public enum AlertLevel{
        ERROR,
        WARNING,
        SUCCESS,
        INFO
    }

    private List<HalAlert> alerts = new LinkedList<>();


    private HalAlertManager(){}


    public void addAlert(HalAlert alert){
        alerts.add(alert);
    }

    public Templator generateAlerts(){
        try {
            Templator tmpl = new Templator(FileUtil.find(TMPL_PATH));
            tmpl.set("alerts", alerts);
            return tmpl;
        }catch (IOException e){
            logger.log(Level.SEVERE, null, e);
        }
        return null;
    }

    @Override
    public void respond(HttpPrintStream out,
                        HttpHeaderParser client_info,
                        Map<String, Object> session,
                        Map<String, String> cookie,
                        Map<String, String> request) throws IOException {

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

        public HalAlert(AlertLevel level, String msg) {
            this.id = nextId++;
            this.level = level;
            this.msg = msg;
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
    }
}
