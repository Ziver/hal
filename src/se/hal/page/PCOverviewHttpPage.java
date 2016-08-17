package se.hal.page;

import se.hal.HalContext;
import se.hal.deamon.SensorDataAggregatorDaemon.AggregationPeriodLength;
import se.hal.intf.HalHttpPage;
import se.hal.struct.devicedata.PowerConsumptionSensorData;
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
        super("pc_overview");
        super.getRootNav().createSubNav("Sensors").createSubNav(this.getId(), "Power;Challenge").setWeight(50);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        DBConnection db = HalContext.getDB();

        List<User> users = User.getUsers(db);
        List<Sensor> sensors = getSensorList(db);

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("users", User.getUsers(db));
        tmpl.set("sensors", sensors);

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

        if (request.containsKey("data")) {
            DataNode node = new DataNode(DataNode.DataType.List);
            if (request.get("data").equals("minute")) {
                DataNode nowTime = new DataNode(DataNode.DataType.Map);
                nowTime.set("time", System.currentTimeMillis()-24*60*60*1000);
                node.add(nowTime);
                for (Sensor sensor : sensors)
                    addAggregateDataToDataNode(node,
                            AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.FIVE_MINUTES, UTCTimeUtility.DAY_IN_MS));
            }
            else if (request.get("data").equals("hour")) {
                DataNode nowTime = new DataNode(DataNode.DataType.Map);
                nowTime.set("time", System.currentTimeMillis()-7*24*60*60*1000);
                node.add(nowTime);
                for (Sensor sensor : sensors)
                    addAggregateDataToDataNode(node,
                            AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.HOUR, UTCTimeUtility.WEEK_IN_MS));
            }
            else if (request.get("data").equals("day")) {
                for (Sensor sensor : sensors)
                    addAggregateDataToDataNode(node,
                            AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.DAY, UTCTimeUtility.INFINITY));
            }
            else if (request.get("data").equals("week")) {
                for (Sensor sensor : sensors)
                    addAggregateDataToDataNode(node,
                            AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.WEEK, UTCTimeUtility.INFINITY));
            }

            DataNode nowTime = new DataNode(DataNode.DataType.Map);
            nowTime.set("time", System.currentTimeMillis());
            node.add(nowTime);

            root.set("data", node);
        }

        return root;
    }

    private void addAggregateDataToDataNode(DataNode root, List<AggregateData> dataList) {
        for (AggregateDataListSqlResult.AggregateData data : dataList) {
            DataNode dataNode = new DataNode(DataNode.DataType.Map);
            dataNode.set("time", data.timestamp);
            if (data.data == null || Float.isNaN(data.data))
                dataNode.set("" + data.id, (String)null);
            else
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
            if (sensor.getDeviceConfig() != null &&
                    sensor.getDeviceConfig() instanceof PowerConsumptionSensorData)
                sensors.add(sensor);
        }
        return sensors;
    }
}
