package se.hal.page;

import se.hal.HalContext;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

import java.io.IOException;
import java.util.Map;


public class StartupWebPage implements HttpPage {
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/startup.tmpl";


    @Override
    public void respond(HttpPrintStream out, HttpHeader headers, Map<String, Object> session, Map<String, String> cookie, Map<String, String> request) throws IOException {
        if (headers.getRequestURL().endsWith("bootstrap.min.css")) {
            printFileContents(HalContext.RESOURCE_WEB_ROOT + "/css/bootstrap.min.css", out);
        } else if (headers.getRequestURL().endsWith("hal.css")) {
            printFileContents(HalContext.RESOURCE_WEB_ROOT + "/css/hal.css", out);
        } else {
            Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
            out.print(tmpl.compile());
        }
    }

    private void printFileContents(String path, HttpPrintStream out) throws IOException {
        out.setHeader(HttpHeader.HEADER_CONTENT_TYPE, "text/css");
        out.print(FileUtil.getContent(HalContext.RESOURCE_WEB_ROOT + path));
    }
}
