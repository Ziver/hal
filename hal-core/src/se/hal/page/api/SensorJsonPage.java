package se.hal.page.api;

import se.hal.HalContext;
import se.hal.daemon.SensorDataAggregatorDaemon;
import se.hal.intf.HalJsonPage;
import se.hal.struct.Sensor;
import se.hal.util.AggregateDataListSqlResult;
import se.hal.util.UTCTimeUtility;
import zutil.ArrayUtil;
import zutil.ObjectUtil;
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
 * aggr: Aggregation periods, needs to be provided to retrieve data. Possible values: minute,hour,day,week
 * </pre>
 */
public class SensorJsonPage extends HalJsonPage {
    private static final Logger logger = LogUtil.getLogger();


    public SensorJsonPage() {
        super("api/sensor");
    }

    @Override
    public DataNode jsonRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception{

        DBConnection db = HalContext.getDB();
        DataNode root = new DataNode(DataNode.DataType.List);

        // Get sensors
        String[] req_ids = new String[0];
        if (request.get("id") != null)
            req_ids = request.get("id").split(",");
        String req_type = request.get("type");

        List<Sensor> sensors = new ArrayList<>();
        for (Sensor sensor : Sensor.getSensors(db)) {
            if (ArrayUtil.contains(req_ids, "" + sensor.getId())) { // id filtering
                sensors.add(sensor);
            }

            if (!ObjectUtil.isEmpty(req_type) &&
                    sensor.getDeviceConfig().getDeviceDataClass().getSimpleName().contains(req_type)) { // device type filtering
                sensors.add(sensor);
            }

            // no options defined, then add all sensors
            if (ObjectUtil.isEmpty(req_ids, req_type)) {
                sensors.add(sensor);
            }
        }

        // Figure out aggregation period
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

        // Generate DataNode
        for (Sensor sensor : sensors) {
            DataNode deviceNode = sensor.getDataNode();

            if (aggrLength > 0) {
                DataNode aggregateNode = getAggregateDataNode(aggrLength,
                    AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, aggrType, aggrLength));
                deviceNode.set("aggregate", aggregateNode);
            }

            root.add(deviceNode);
        }

        return root;
    }

    private DataNode getAggregateDataNode(long endTime, List<AggregateDataListSqlResult.AggregateData> dataList) {
        DataNode aggregateNode = new DataNode(DataNode.DataType.Map);
        DataNode timestampNode = new DataNode(DataNode.DataType.List);
        DataNode dataNode = new DataNode(DataNode.DataType.List);

        // end timestamp
        if (endTime != UTCTimeUtility.INFINITY) {
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

        aggregateNode.set("timestamps", timestampNode);
        aggregateNode.set("data", dataNode);

        return aggregateNode;
    }

}
