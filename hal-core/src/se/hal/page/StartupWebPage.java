package se.hal.page;

import se.hal.HalContext;
import zutil.MimeTypeUtil;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class StartupWebPage implements HttpPage {
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/startup.tmpl";
    private static final List<String> ACCEPTED_FILE_LIST = Arrays.asList(
            "/css/lib/bootstrap.min.css",
            "/css/hal.css",
            "/fonts/glyphicons-halflings-regular.ttf",
            "/fonts/glyphicons-halflings-regular.woff",
            "/fonts/glyphicons-halflings-regular.woff2"
    );

    @Override
    public void respond(HttpPrintStream out, HttpHeader headers, Map<String, Object> session, Map<String, String> cookie, Map<String, String> request) throws IOException {
        if (ACCEPTED_FILE_LIST.contains(headers.getRequestURL())) {
            printFileContents(headers.getRequestURL(), out);
        } else {
            Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
            out.print(tmpl.compile());
        }
    }

    private void printFileContents(String path, HttpPrintStream out) throws IOException {
        out.setHeader(HttpHeader.HEADER_CONTENT_TYPE, MimeTypeUtil.getMimeByExtension(FileUtil.getFileExtension(path)).toString());
        out.write(FileUtil.getByteContent(FileUtil.find(HalContext.RESOURCE_WEB_ROOT + path)));
    }
}
