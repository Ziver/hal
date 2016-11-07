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
        DataNode root = new DataNode(DataNode.DataType.List);

        DBConnection db = HalContext.getDB();
        List<Sensor> sensors = getSensorList(db);

        if (request.containsKey("data")) {
            AggregationPeriodLength aggrType;
            long aggrLength = UTCTimeUtility.INFINITY;

            switch(request.get("data")){
                case "minute":
                default:
                    aggrType = AggregationPeriodLength.FIVE_MINUTES;
                    aggrLength = UTCTimeUtility.DAY_IN_MS;
                    break;
                case "hour":
                    aggrType = AggregationPeriodLength.HOUR;
                    aggrLength = UTCTimeUtility.WEEK_IN_MS;
                    break;
                case "day":
                    aggrType = AggregationPeriodLength.DAY;
                    break;
                case "week":
                    aggrType = AggregationPeriodLength.WEEK;
                    break;
            }

            for (Sensor sensor : sensors) {
                addAggregateDataToDataNode(root, sensor, aggrLength,
                        AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, aggrType, aggrLength));
            }
        }

        return root;
    }

    private void addAggregateDataToDataNode(DataNode root, Sensor sensor, long endTime, List<AggregateData> dataList) {
        DataNode timestampNode = new DataNode(DataNode.DataType.List);
        DataNode dataNode = new DataNode(DataNode.DataType.List);
        // end timestamp
        if (endTime != UTCTimeUtility.INFINITY){
            timestampNode.add(System.currentTimeMillis() - endTime);
            dataNode.add((String)null);
        }
        // actual data
        for (AggregateDataListSqlResult.AggregateData data : dataList) {
            timestampNode.add(data.timestamp);
            if (data.data == null || Float.isNaN(data.data))
                dataNode.add((String)null);
            else
                dataNode.add(data.data);
        }
        // start timestamp
        timestampNode.add(System.currentTimeMillis());
        dataNode.add((String)null);

        DataNode deviceNode = new DataNode(DataNode.DataType.Map);
        deviceNode.set("name", sensor.getName());
        deviceNode.set("timestamps", timestampNode);
        deviceNode.set("data", dataNode);
        root.add(deviceNode);
    }

    /**
     * @return a List of all PowerConsumption sensors
     */
    private List<Sensor> getSensorList(DBConnection db) throws SQLException {
        List<Sensor> sensors = new ArrayList<>();
        for (Sensor sensor : Sensor.getSensors(db)) {
            if (sensor.getDeviceConfig() != null) // Show all sensors for now
//            if (sensor.getDeviceConfig() != null &&
//                    sensor.getDeviceConfig().getSensorDataClass() == PowerConsumptionSensorData.class)
                sensors.add(sensor);
        }
        return sensors;
    }
}
