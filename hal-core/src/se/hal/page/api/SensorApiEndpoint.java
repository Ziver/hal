package se.hal.page.api;

import se.hal.HalContext;
import se.hal.daemon.SensorDataAggregatorDaemon;
import se.hal.intf.HalApiEndpoint;
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
 * RESTish API for accessing and managing Sensors.
 * For web interface definition see the OpenApi definition hal-core/resources/web/api/doc.html
 */
public class SensorApiEndpoint extends HalApiEndpoint {
    private static final Logger logger = LogUtil.getLogger();


    public SensorApiEndpoint() {
        super("api/sensor");
    }

    @Override
    public DataNode jsonRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request) throws Exception{

        DBConnection db = HalContext.getDB();
        DataNode root = new DataNode(DataNode.DataType.List);

        // --------------------------------------
        // Get Action
        // --------------------------------------

        String[] reqIds = new String[0];
        if (request.get("id") != null)
            reqIds = request.get("id").split(",");

        String reqTypeConfig = request.get("typeConfig");
        String reqTypeData = request.get("typeData");


        List<Sensor> sensors = new ArrayList<>();
        for (Sensor sensor : Sensor.getSensors(db)) {
            boolean filter_match = true;

            // id filtering
            if (!ObjectUtil.isEmpty((Object) reqIds) && !ArrayUtil.contains(reqIds, "" + sensor.getId())) {
                filter_match = false;
            }

            // device type filtering
            if (!ObjectUtil.isEmpty(reqTypeConfig) &&
                    !sensor.getDeviceConfig().getClass().getSimpleName().equals(reqTypeConfig)) {
                filter_match = false;
            }

            // data type filtering
            if (!ObjectUtil.isEmpty(reqTypeData) &&
                    !sensor.getDeviceConfig().getDeviceDataClass().getSimpleName().equals(reqTypeData)) {
                filter_match = false;
            }

            // Check the filter
            if (filter_match) {
                sensors.add(sensor);
            }
        }

        // --------------------------------------
        // Was aggregated data requested
        // --------------------------------------

        SensorDataAggregatorDaemon.AggregationPeriodLength aggrType = null;
        long aggregationLength = -1;

        if (request.get("aggregation") != null) {
            switch (request.get("aggregation")) {
                case "minute":
                    aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.FIVE_MINUTES;
                    aggregationLength = UTCTimeUtility.DAY_IN_MS;
                    break;
                case "hour":
                    aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.HOUR;
                    aggregationLength = UTCTimeUtility.WEEK_IN_MS;
                    break;
                case "day":
                    aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.DAY;
                    aggregationLength = UTCTimeUtility.INFINITY;
                    break;
                case "week":
                    aggrType = SensorDataAggregatorDaemon.AggregationPeriodLength.WEEK;
                    aggregationLength = UTCTimeUtility.INFINITY;
                    break;
            }
        }

        // --------------------------------------
        // Generate DataNode
        // --------------------------------------

        for (Sensor sensor : sensors) {
            DataNode deviceNode = sensor.getDataNode();

            if (aggregationLength > 0) {
                DataNode aggregateNode = getAggregateDataNode(aggregationLength,
                    AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, aggrType, aggregationLength));
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
