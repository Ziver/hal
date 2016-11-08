package se.hal.page;

import se.hal.HalContext;
import se.hal.intf.HalHttpPage;
import se.hal.struct.AbstractDevice;
import se.hal.struct.Event;
import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeader;
import zutil.net.http.HttpPrintStream;
import zutil.net.http.multipart.MultipartField;
import zutil.net.http.multipart.MultipartFileField;
import zutil.net.http.multipart.MultipartParser;
import zutil.parser.Base64Decoder;
import zutil.parser.Base64Encoder;
import zutil.parser.DataNode;
import zutil.parser.Templator;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

/**
 * Created by Ziver on 2016-06-23.
 */
public class MapHttpPage extends HalHttpPage implements HalHttpPage.HalJsonPage {
    private static final String TEMPLATE = "resource/web/map.tmpl";

    private String bgType;
    private byte[] bgImage;


    public MapHttpPage() {
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
                out.setStatusCode(404);
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


    @Override
    public DataNode jsonResponse(Map<String, Object> session,
                                 Map<String, String> cookie,
                                 Map<String, String> request) throws Exception {
        DBConnection db = HalContext.getDB();
        DataNode root = new DataNode(DataNode.DataType.Map);
        if ("getdata".equals(request.get("action"))) {
            getDeviceNode(db, root);
        } else if ("save".equals(request.get("action"))) {
            int id = Integer.parseInt(request.get("id"));
            AbstractDevice device = null;
            if ("sensor".equals(request.get("type")))
                device = Sensor.getSensor(db, id);
            else if ("event".equals(request.get("type")))
                device = Event.getEvent(db, id);

            device.setX(Float.parseFloat(request.get("x")));
            device.setY(Float.parseFloat(request.get("y")));
            device.save(db);
        }
        return root;
    }


    private void getDeviceNode(DBConnection db, DataNode root) throws SQLException {
        DataNode sensorsNode = new DataNode(DataNode.DataType.List);
        for (Sensor sensor : Sensor.getLocalSensors(db)) {
            DataNode sensorNode = getDeviceNode(sensor);
            sensorNode.set("data", ""+sensor.getDeviceData());
            sensorsNode.add(sensorNode);
        }
        root.set("sensors", sensorsNode);

        DataNode eventsNode = new DataNode(DataNode.DataType.List);
        for (Event event : Event.getLocalEvents(db)) {
            DataNode eventNode = getDeviceNode(event);
            eventNode.set("data", ""+event.getDeviceData());
            eventsNode.add(eventNode);
        }
        root.set("events", eventsNode);
    }

    private DataNode getDeviceNode(AbstractDevice device) {
        DataNode deviceNode = new DataNode(DataNode.DataType.Map);
        deviceNode.set("id", device.getId());
        deviceNode.set("name", device.getName());
        deviceNode.set("x", device.getX());
        deviceNode.set("y", device.getY());
        return deviceNode;
    }


    private void loadBgImage() {
        String property = HalContext.getStringProperty("map_bgimage");
        if (property != null) {
            String[] split = property.split(",", 2);
            bgType = split[0];
            bgImage = Base64Decoder.decodeToByte(split[1]);
        }
    }

    private void saveBgImage(String type, byte[] data) throws SQLException {
        HalContext.setProperty("map_bgimage", type + "," + Base64Encoder.encode(data));
    }
}