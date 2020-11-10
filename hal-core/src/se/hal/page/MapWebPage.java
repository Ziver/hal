package se.hal.page;

import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPrintStream;
import zutil.net.http.multipart.MultipartField;
import zutil.net.http.multipart.MultipartFileField;
import zutil.net.http.multipart.MultipartParser;
import zutil.parser.Base64Decoder;
import zutil.parser.Base64Encoder;
import zutil.parser.Templator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

public class MapWebPage extends HalWebPage {
    private static final String TEMPLATE = "resource/web/map.tmpl";

    private String bgType;
    private byte[] bgImage;


    public MapWebPage() {
        super("map");
        super.getRootNav().createSubNav(this.getId(), "Map").setWeight(-100);
        super.showSubNav(false);
    }

    @Override
    public void respond(HttpPrintStream out, HttpHeader header,
                        Map<String, Object> session, Map<String, String> cookie,
                        Map<String, String> request) throws IOException {

        if ("POST".equals(header.getRequestType())) {
            for (MultipartField field : new MultipartParser(header)) {
                if (field instanceof MultipartFileField) {
                    MultipartFileField file = (MultipartFileField) field;
                    String ext = FileUtil.getFileExtension(file.getFilename());
                    if (ext.equals("jpg") || ext.equals("png") || ext.equals("svg") || ext.equals("gif")) {
                        try {
                            saveBgImage(ext, file.getContent());
                            out.println("Upload successful: " + file.getFilename());
                        } catch (SQLException e) {
                            e.printStackTrace();
                            out.println("Upload error: " + e.getMessage());
                        }
                        bgImage = null; // reload image from db
                    }
                }
            }
        } else if (request.containsKey("bgimage")) {
            if (bgImage == null)
                loadBgImage();
            if (bgImage == null)
                out.setResponseStatusCode(404);
            else {
                out.setHeader("Content-Type", "image/" + bgType);
                out.setHeader("Content-Length", "" + bgImage.length);
                out.write(bgImage);
            }
        } else { // Run default Hal behaviour
            super.respond(out, header, session, cookie, request);
        }
    }

    @Override
    public Templator httpRespond(Map<String, Object> session,
                                 Map<String, String> cookie,
                                 Map<String, String> request) throws Exception {

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        return tmpl;
    }


    private void loadBgImage() {
        String property = HalContext.getStringProperty(HalContext.PROPERTY_MAP_BACKGROUND_IMAGE);
        if (property != null) {
            String[] split = property.split(",", 2);
            bgType = split[0];
            bgImage = Base64Decoder.decodeToByte(split[1]);
        }
    }

    private void saveBgImage(String type, byte[] data) throws SQLException {
        HalContext.setProperty(HalContext.PROPERTY_MAP_BACKGROUND_IMAGE, type + "," + Base64Encoder.encode(data));
    }
}
