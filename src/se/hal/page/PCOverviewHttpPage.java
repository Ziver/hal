package se.hal.page;

import se.hal.HalContext;
import se.hal.deamon.SensorDataAggregatorDaemon.AggregationPeriodLength;
import se.hal.intf.HalHttpPage;
import se.hal.struct.PowerConsumptionSensorData;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import se.hal.util.AggregateDataListSqlResult;
import se.hal.util.AggregateDataListSqlResult.AggregateData;
import se.hal.util.UTCTimeUtility;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.DataNode;
import zutil.parser.Templator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PCOverviewHttpPage extends HalHttpPage implements HalHttpPage.HalJsonPage {
    private static final String TEMPLATE = "resource/web/pc_overview.tmpl";

    public PCOverviewHttpPage() {
        super("Power;Challenge", "pc_overview");
        super.getRootNav().getSubNav("sensors").addSubNav(super.getNav());
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        DBConnection db = HalContext.getDB();

        List<User> users = User.getUsers(db);
        ArrayList<AggregateData> minDataList = new ArrayList<>();
        ArrayList<AggregateData> hourDataList = new ArrayList<>();
        ArrayList<AggregateData> dayDataList = new ArrayList<>();
        ArrayList<AggregateData> weekDataList = new ArrayList<>();

        List<Sensor> sensors = getSensorList(db);
        for (Sensor sensor : sensors) {
            minDataList.addAll(AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.FIVE_MINUTES, UTCTimeUtility.DAY_IN_MS));

            hourDataList.addAll(AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.HOUR, UTCTimeUtility.WEEK_IN_MS));

            dayDataList.addAll(AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.DAY, UTCTimeUtility.INFINITY));

            weekDataList.addAll(AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.WEEK, UTCTimeUtility.INFINITY));
        }


        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("users", User.getUsers(db));
        tmpl.set("sensors", sensors);
        tmpl.set("minData", minDataList);
        tmpl.set("hourData", hourDataList);
        tmpl.set("dayData", dayDataList);
        tmpl.set("weekData", weekDataList);

        return tmpl;
    }

    @Override
    public DataNode jsonResponse(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception {
        DataNode root = new DataNode(DataNode.DataType.Map);

        DBConnection db = HalContext.getDB();
        List<Sensor> sensors = getSensorList(db);

        if (request.get("data").contains("minute")) {
            DataNode node = new DataNode(DataNode.DataType.List);
            for (Sensor sensor : sensors)
                addAggregateDataToDataNode(node,
                        AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.FIVE_MINUTES, UTCTimeUtility.DAY_IN_MS));
            root.set("minuteData", node);
        }
        if (request.get("data").contains("hour")) {
            DataNode node = new DataNode(DataNode.DataType.List);
            for (Sensor sensor : sensors)
                addAggregateDataToDataNode(node,
                        AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.HOUR, UTCTimeUtility.WEEK_IN_MS));
            root.set("minuteData", node);
        }
        if (request.get("data").contains("day")) {
            DataNode node = new DataNode(DataNode.DataType.List);
            for (Sensor sensor : sensors)
                addAggregateDataToDataNode(node,
                        AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.DAY, UTCTimeUtility.INFINITY));
            root.set("minuteData", node);
        }
        if (request.get("data").contains("week")) {
            DataNode node = new DataNode(DataNode.DataType.List);
            for (Sensor sensor : sensors)
                addAggregateDataToDataNode(node,
                        AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.WEEK, UTCTimeUtility.INFINITY));
            root.set("minuteData", node);
        }

        return root;
    }

    private void addAggregateDataToDataNode(DataNode root, List<AggregateData> dataList) {
        for (AggregateDataListSqlResult.AggregateData data : dataList) {
            DataNode dataNode = new DataNode(DataNode.DataType.Map);
            dataNode.set("time", data.timestamp);
            dataNode.set("" + data.id, data.data);
            root.add(dataNode);
        }
    }

    /**
     * @return a List of all PowerConsumption sensors
     */
    private List<Sensor> getSensorList(DBConnection db) throws SQLException {
        List<Sensor> sensors = new ArrayList<>();
        for (Sensor sensor : Sensor.getSensors(db)) {
            if (sensor instanceof PowerConsumptionSensorData)
                sensors.add(sensor);
        }
        return sensors;
    }
}
