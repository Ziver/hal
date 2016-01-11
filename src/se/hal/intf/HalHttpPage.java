package se.hal.intf;

import se.hal.HalContext;
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

    private static ArrayList<HalHttpPage> pages = new ArrayList<>();

    private final String name;
    private final String id;

    public HalHttpPage(String name, String id){
        this.name = name;
        this.id = id;
        pages.add(this);
    }

    public String getName(){
        return name;
    }
    public String getId(){
        return id;
    }
    public String getURL(){
        return "/" + this.id;
    }


    @Override
    public void respond(HttpPrintStream out, HttpHeaderParser client_info,
                        Map<String, Object> session, Map<String, String> cookie,
                        Map<String, String> request) throws IOException {

        try {
            DBConnection db = HalContext.getDB();

            Templator tmpl = new Templator(FileUtil.find("web-resource/index.tmpl"));
            tmpl.set("user", User.getLocalUser(db));
            tmpl.set("navigation", pages);
            tmpl.set("content", httpRespond(session, cookie, request));
            out.print(tmpl.compile());

        } catch (Exception e) {
            throw new IOException(e);
        }
    }


    public abstract Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
                throws Exception;
}
