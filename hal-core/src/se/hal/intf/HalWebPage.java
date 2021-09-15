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
import zutil.ui.Navigation;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public abstract class HalWebPage implements HttpPage{
    private static final String TEMPLATE_MAIN = HalContext.RESOURCE_WEB_ROOT + "/main_index.tmpl";
    private static final String TEMPLATE_NAVIGATION = HalContext.RESOURCE_WEB_ROOT + "/main_nav.tmpl";
    private static final String TEMPLATE_SIDE_NAVIGATION = HalContext.RESOURCE_WEB_ROOT + "/main_nav_side.tmpl";

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

            // Prepare common template data

            Map<String,Object> data = new HashMap<>();
            data.put("page", Navigation.getPagedNavigation(header));
            data.put("user", User.getLocalUser(db));
            data.put("rootNav", rootNav.createPagedNavInstance(header).getSubNavs());
            data.put("userNav", userNav.createPagedNavInstance(header).getSubNavs());

            List<Navigation> breadcrumb = Navigation.getBreadcrumb(Navigation.getPagedNavigation(header));
            if (!breadcrumb.isEmpty()) {
                data.put("breadcrumb", breadcrumb);
                data.put("subNav", breadcrumb.get(1).createPagedNavInstance(header).getSubNavs());
            }

            // Create templates

            Templator navigationTemplate = new Templator(FileUtil.find(TEMPLATE_NAVIGATION));
            navigationTemplate.setAll(data);

            Templator subNavigationTemplate = null;
            if (showSubNav) {
                subNavigationTemplate = new Templator(FileUtil.find(TEMPLATE_SIDE_NAVIGATION));
                subNavigationTemplate.setAll(data);
            }

            Templator main = new Templator(FileUtil.find(TEMPLATE_MAIN));
            main.setAll(data);
            main.set("navigation", navigationTemplate);
            main.set("side_navigation", subNavigationTemplate);
            main.set("content", httpRespond(session, cookie, request));
            main.set("alerts", HalAlertManager.getInstance().generateAlerts()); // do last so we don't miss any alerts

            out.print(main.compile());
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Defines if the sub-navigation should be shown on the page
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