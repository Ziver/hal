package se.hal.intf;

import se.hal.HalContext;
import se.hal.page.HalAlertManager;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;
import zutil.parser.json.JSONWriter;
import zutil.ui.Navigation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public abstract class HalWebPage implements HttpPage{
    private static final String TEMPLATE = "resource/web/main_index.tmpl";
    private static Navigation rootNav = Navigation.createRootNav();
    private static Navigation userNav = Navigation.createRootNav();

    private String pageId;
    private boolean showSubNav;

    public HalWebPage(String id){
        this.pageId = id;
        this.showSubNav = true;
    }

    public String getId(){
        return pageId;
    }


    @Override
    public void respond(HttpPrintStream out, HttpHeader header,
                        Map<String, Object> session, Map<String, String> cookie,
                        Map<String, String> request) throws IOException {

        try {
            DBConnection db = HalContext.getDB();

            Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
            tmpl.set("user", User.getLocalUser(db));
            tmpl.set("showSubNav", showSubNav);
            if (showSubNav) {
                List<Navigation> breadcrumb = Navigation.getBreadcrumb(Navigation.getPagedNavigation(header));
                if (!breadcrumb.isEmpty())
                    tmpl.set("subNav", breadcrumb.get(1).createPagedNavInstance(header).getSubNavs());
            }
            tmpl.set("rootNav", rootNav.createPagedNavInstance(header).getSubNavs());
            tmpl.set("userNav", userNav.createPagedNavInstance(header).getSubNavs());
            tmpl.set("content", httpRespond(session, cookie, request));
            tmpl.set("alerts", HalAlertManager.getInstance().generateAlerts()); // do last so we don't miss any alerts
            out.print(tmpl.compile());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Sets if the subnavigation should be shown on the page
     */
    protected void showSubNav(boolean show) {
        this.showSubNav = show;
    }

    public static Navigation getRootNav(){
        return rootNav;
    }
    public static Navigation getUserNav(){
        return userNav;
    }


    public abstract Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
                throws Exception;

}