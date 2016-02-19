package se.hal.page;

import se.hal.HalContext;
import se.hal.deamon.SensorDataAggregatorDaemon.AggregationPeriodLength;
import se.hal.intf.HalHttpPage;
import se.hal.struct.Sensor;
import se.hal.util.AggregateDataListSqlResult;
import se.hal.util.HistoryDataListSqlResult;
import se.hal.util.HistoryDataListSqlResult.HistoryData;
import se.hal.util.UTCTimeUtility;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

public class SensorOverviewHttpPage extends HalHttpPage {
    private static final int HISTORY_LIMIT = 200;
    private static final String OVERVIEW_TEMPLATE = "web-resource/sensor_overview.tmpl";
    private static final String DETAIL_TEMPLATE = "web-resource/sensor_detail.tmpl";


    public SensorOverviewHttpPage(){
        super("Overview", "sensor_overview");
        super.getRootNav().getSubNav("sensors").addSubNav(super.getNav());
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        DBConnection db = HalContext.getDB();
        int id = (request.containsKey("id") ? Integer.parseInt(request.get("id")) : -1);

        // Save new input
        if(id >= 0){
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
            tmpl.set("aggregation", AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.HOUR, UTCTimeUtility.WEEK_IN_MS));
            return tmpl;
        }
        else {
            Templator tmpl = new Templator(FileUtil.find(OVERVIEW_TEMPLATE));
            tmpl.set("sensors", Sensor.getLocalSensors(db));
            return tmpl;
        }
    }

}
