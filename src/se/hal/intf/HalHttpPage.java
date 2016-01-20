package se.hal.intf;

import se.hal.HalContext;
import se.hal.page.HalAlertManager;
import se.hal.page.HalNavigation;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeaderParser;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Ziver on 2015-12-10.
 */
public abstract class HalHttpPage implements HttpPage{
    private static final String TEMPLATE = "web-resource/index.tmpl";
    private static HalNavigation rootNav = new HalNavigation();
    private static HalNavigation userNav = new HalNavigation();

    private HalNavigation nav;


    public HalHttpPage(String name, String id){
        this.nav = new HalNavigation(id, name);
    }

    public String getName(){
        return nav.getName();
    }
    public String getId(){
        return nav.getId();
    }
    public HalNavigation getNav(){
        return nav;
    }


    @Override
    public void respond(HttpPrintStream out, HttpHeaderParser client_info,
                        Map<String, Object> session, Map<String, String> cookie,
                        Map<String, String> request) throws IOException {

        try {
            DBConnection db = HalContext.getDB();

            Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
            tmpl.set("user", User.getLocalUser(db));
            tmpl.set("nav", nav.getNavBreadcrumb().get(1));
            tmpl.set("rootNav", rootNav);
            tmpl.set("userNav", userNav);
            tmpl.set("alerts", HalAlertManager.getInstance().generateAlerts());
            tmpl.set("content", httpRespond(session, cookie, request));
            out.print(tmpl.compile());

        } catch (Exception e) {
            throw new IOException(e);
        }
    }


    public static HalNavigation getRootNav(){
        return rootNav;
    }
    public static HalNavigation getUserNav(){
        return userNav;
    }


    public abstract Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
                throws Exception;
}
