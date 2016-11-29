package se.hal.page;

import se.hal.HalContext;
import se.hal.daemon.SensorDataAggregatorDaemon;
import se.hal.intf.HalJsonPage;
import se.hal.struct.Sensor;
import se.hal.util.AggregateDataListSqlResult;
import se.hal.util.UTCTimeUtility;
import zutil.ArrayUtil;
import zutil.db.DBConnection;
import zutil.log.LogUtil;
import zutil.parser.DataNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Available HTTP Get Request parameters:
 * <pre>
 * Sensor filtering parameters:
 * id: comma separated numeric id for specific sensors
 * type: sensor data type name
 *
 * Data filtering parameters:
 * aggr: Aggrigation periods, needs to be provided to retrieve data. Possible values: minute,hour,day,week
 * </pre>
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
        String[] req_ids = null;
        if (request.get("id") != null)
            req_ids = request.get("id").split(",");
        String req_type = request.get("type");

        List<Sensor> sensors = new ArrayList<>();
        for (Sensor sensor : Sensor.getSensors(db)) {
            if (sensor.getDeviceConfig() == null) // Show all sensors for now
                continue;

            if (req_ids == null && req_type==null) // no options defined, then add all sensors
                sensors.add(sensor);
            else if (req_ids != null && ArrayUtil.contains(req_ids, ""+sensor.getId())) // id filtering
                sensors.add(sensor);
            else if (req_type != null && !req_type.isEmpty() &&
                    sensor.getDeviceConfig().getSensorDataClass().getSimpleName().contains(req_type)) // device type filtering
                sensors.add(sensor);
        }

        //// Figure out aggregation period
        SensorDataAggregatorDaemon.AggregationPeriodLength aggrType = null;
        long aggrLength = -1;
        if (request.get("aggr") != null) {
            switch (request.get("aggr")) {
                case "minute":
                    aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.FIVE_MINUTES;
                    aggrLength = UTCTimeUtility.DAY_IN_MS;
                    break;
                case "hour":
                    aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.HOUR;
                    aggrLength = UTCTimeUtility.WEEK_IN_MS;
                    break;
                case "day":
                    aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.DAY;
                    aggrLength = UTCTimeUtility.INFINITY;
                    break;
                case "week":
                    aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.WEEK;
                    aggrLength = UTCTimeUtility.INFINITY;
                    break;
            }
        }

        /// Generate DataNode
        for (Sensor sensor : sensors) {
            DataNode deviceNode = new DataNode(DataNode.DataType.Map);
            deviceNode.set("id", sensor.getId());
            deviceNode.set("name", sensor.getName());
            deviceNode.set("user", sensor.getUser().getUsername());
            deviceNode.set("type", sensor.getDeviceConfig().getSensorDataClass().getSimpleName());
            deviceNode.set("x", sensor.getX());
            deviceNode.set("y", sensor.getY());

            if (aggrLength > 0) {
                addAggregateDataToDataNode(deviceNode, aggrLength,
                    AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, aggrType, aggrLength));
            }
            root.add(deviceNode);
        }

        return root;
    }

    private void addAggregateDataToDataNode(DataNode deviceNode, long endTime, List<AggregateDataListSqlResult.AggregateData> dataList) {
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

        deviceNode.set("timestamps", timestampNode);
        deviceNode.set("data", dataNode);
    }

}
