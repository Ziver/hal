package se.hal.page;

import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import se.hal.struct.Sensor;
import se.hal.util.DeviceNameComparator;
import se.hal.util.HistoryDataListSqlResult;
import se.hal.util.HistoryDataListSqlResult.HistoryData;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.sql.PreparedStatement;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SensorOverviewWebPage extends HalWebPage {
    private static final int HISTORY_LIMIT = 200;
    private static final String OVERVIEW_TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/sensor_overview.tmpl";
    private static final String DETAIL_TEMPLATE   = HalContext.RESOURCE_WEB_ROOT + "/sensor_detail.tmpl";


    public SensorOverviewWebPage() {
        super("sensor_overview");
        super.getRootNav().createSubNav("Sensors").createSubNav(this.getId(), "Overview");
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        DBConnection db = HalContext.getDB();
        int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));

        // Save new input
        if (id >= 0) {
            Sensor sensor = Sensor.getSensor(db, id);

            // get history data
            PreparedStatement stmt = db.getPreparedStatement(
                    "SELECT * FROM sensor_data_raw WHERE sensor_id == ? ORDER BY timestamp DESC LIMIT ?");
            stmt.setLong(1, sensor.getId());
            stmt.setLong(2, HISTORY_LIMIT);
            List<HistoryData> history = DBConnection.exec(stmt, new HistoryDataListSqlResult());

            Templator tmpl = new Templator(FileUtil.find(DETAIL_TEMPLATE));
            tmpl.set("sensor", sensor);
            tmpl.set("history", history);
            return tmpl;
        } else {
            List<Sensor> sensors = Sensor.getLocalSensors(db);
            Collections.sort(sensors, DeviceNameComparator.getInstance());

            Templator tmpl = new Templator(FileUtil.find(OVERVIEW_TEMPLATE));
            tmpl.set("sensors", sensors);
            return tmpl;
        }
    }

}
