package se.hal.intf;

import se.hal.HalContext;
import se.hal.page.HalAlertManager;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.DataNode;
import zutil.parser.Templator;
import zutil.parser.json.JSONWriter;
import zutil.ui.Navigation;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by Ziver on 2015-12-10.
 */
public abstract class HalHttpPage implements HttpPage{
    private static final String TEMPLATE = "resource/web/main_index.tmpl";
    private static Navigation rootNav = Navigation.createRootNav();
    private static Navigation userNav = Navigation.createRootNav();

    private String pageId;

    public HalHttpPage(String id){
        this.pageId = id;
    }

    public String getId(){
        return pageId;
    }


    @Override
    public void respond(HttpPrintStream out, HttpHeader header,
                        Map<String, Object> session, Map<String, String> cookie,
                        Map<String, String> request) throws IOException {

        try {
            if(this instanceof HalJsonPage &&
                    (("application/json").equals(header.getHeader("ContentType")) ||
                    request.containsKey("json"))){
                out.setHeader("Content-Type", "application/json");
                JSONWriter writer = new JSONWriter(out);
                writer.write(((HalJsonPage)this).jsonResponse(session,cookie, request));
                writer.close();
            }
            else{
                DBConnection db = HalContext.getDB();

                Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
                tmpl.set("user", User.getLocalUser(db));
                List<Navigation> breadcrumb = Navigation.getBreadcrumb(Navigation.getPagedNavigation(header));
                if(!breadcrumb.isEmpty())
                    tmpl.set("subNav", breadcrumb.get(1).createPagedNavInstance(header).getSubNavs());
                tmpl.set("rootNav", rootNav.createPagedNavInstance(header).getSubNavs());
                tmpl.set("userNav", userNav.createPagedNavInstance(header).getSubNavs());
                tmpl.set("alerts", HalAlertManager.getInstance().generateAlerts());
                tmpl.set("content", httpRespond(session, cookie, request));
                out.print(tmpl.compile());
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
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


    public interface HalJsonPage{
        DataNode jsonResponse(
                Map<String, Object> session,
                Map<String, String> cookie,
                Map<String, String> request)
                    throws Exception;
    }
}