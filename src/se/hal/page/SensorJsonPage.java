package se.hal.page;

import se.hal.HalContext;
import se.hal.deamon.SensorDataAggregatorDaemon;
import se.hal.intf.HalJsonPage;
import se.hal.struct.Sensor;
import se.hal.util.AggregateDataListSqlResult;
import se.hal.util.UTCTimeUtility;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.parser.DataNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Request parameters:
 * aggr: Aggrigation periods. Possible values: minute,hour,day,week
 */
public class SensorJsonPage extends HalJsonPage {
    private static final Logger logger = LogUtil.getLogger();

    public SensorJsonPage() {
        super("data/sensor");
    }


    @Override
    public DataNode jsonRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception{

        DBConnection db = HalContext.getDB();
        DataNode root = new DataNode(DataNode.DataType.List);

        //// Get sensors
        List<Sensor> sensors = new ArrayList<>();
        for (Sensor sensor : Sensor.getSensors(db)) {
            if (sensor.getDeviceConfig() != null) // Show all sensors for now
//            if (sensor.getDeviceConfig() != null &&
//                    sensor.getDeviceConfig().getSensorDataClass() == PowerConsumptionSensorData.class)
                sensors.add(sensor);
        }

        //// Figure out aggregation period
        SensorDataAggregatorDaemon.AggregationPeriodLength aggrType;
        long aggrLength = UTCTimeUtility.INFINITY;
        switch (request.get("aggr")) {
            case "minute":
            default:
                aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.FIVE_MINUTES;
                aggrLength = UTCTimeUtility.DAY_IN_MS;
                break;
            case "hour":
                aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.HOUR;
                aggrLength = UTCTimeUtility.WEEK_IN_MS;
                break;
            case "day":
                aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.DAY;
                break;
            case "week":
                aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.WEEK;
                break;
        }

        /// Generate DataNode
        for (Sensor sensor : sensors) {
            addAggregateDataToDataNode(root, sensor, aggrLength,
                    AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, aggrType, aggrLength));
        }

        return root;
    }

    private void addAggregateDataToDataNode(DataNode root, Sensor sensor, long endTime, List<AggregateDataListSqlResult.AggregateData> dataList) {
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
        deviceNode.set("user", sensor.getUser().getUsername());
        deviceNode.set("type", sensor.getDeviceConfig().getSensorDataClass().getSimpleName());
        deviceNode.set("timestamps", timestampNode);
        deviceNode.set("data", dataNode);
        root.add(deviceNode);
    }

}
