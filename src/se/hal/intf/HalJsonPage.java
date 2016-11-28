package se.hal.intf;

import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.DataNode;
import zutil.parser.Templator;
import zutil.parser.json.JSONWriter;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/**
 * A interface defining a Hal json endpoint
 */
public abstract class HalJsonPage extends HalHttpPage{


    public HalJsonPage(String id) {
        super(id);
    }


    @Override
    public void respond(HttpPrintStream out,
                        HttpHeader headers,
                        Map<String, Object> session,
                        Map<String, String> cookie,
                        Map<String, String> request) throws IOException {



        out.setHeader("Content-Type", "application/json");
        JSONWriter writer = new JSONWriter(out);
        try{
            writer.write(jsonRespond(session, cookie, request));
        } catch (Exception e){
            DataNode root = new DataNode(DataNode.DataType.Map);
            root.set("error", e.getMessage());
            writer.write(root);
        }
        writer.close();
    }
    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception {
        return null;
    }


    protected abstract DataNode jsonRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception;
}