package se.hal.util;

import se.hal.struct.Sensor;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class AggregateDataListSqlResult implements SQLResultHandler<ArrayList<AggregateDataListSqlResult.AggregateData>> {

	public static class AggregateData {
		public long timestamp;
		public String data;
		public String username;
		public AggregateData(long time, String data, String uname) {
			this.timestamp = time;
			this.data = data;
			this.username = uname;
		}
	}


    public static List<AggregateData> getFiveMinuteAggregateData(DBConnection db, Sensor sensor) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement(
                "SELECT user.username as username,"
                        + " sensor_data_aggr.timestamp_start as timestamp_start,"
                        + " sensor_data_aggr.timestamp_end as timestamp_end,"
                        + " sensor_data_aggr.data as data,"
                        + " sensor_data_aggr.confidence as confidence,"
                        + TimeUtility.FIVE_MINUTES_IN_MS + " as period_length"
                        + " FROM sensor_data_aggr, user, sensor"
                        + " WHERE sensor.id = sensor_data_aggr.sensor_id"
                        + " AND sensor.id = ?"
                        + " AND user.id = sensor.user_id"
                        + " AND user.id = ?"
                        + " AND timestamp_end-timestamp_start == ?"
                        + " AND timestamp_start > ?"
                        + " ORDER BY timestamp_start ASC");
        stmt.setLong(1, sensor.getId());
        stmt.setLong(2, sensor.getUser().getId());
        stmt.setLong(3, TimeUtility.FIVE_MINUTES_IN_MS-1);
        stmt.setLong(4, (System.currentTimeMillis() - TimeUtility.DAY_IN_MS) );
        return DBConnection.exec(stmt , new AggregateDataListSqlResult());
    }

    public static List<AggregateData> getHourAggregateData(DBConnection db, Sensor sensor) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement(
                "SELECT user.username as username,"
                        + " sensor_data_aggr.timestamp_start as timestamp_start,"
                        + " sensor_data_aggr.timestamp_end as timestamp_end,"
                        + " sensor_data_aggr.data as data,"
                        + " sensor_data_aggr.confidence as confidence,"
                        + TimeUtility.HOUR_IN_MS + " as period_length"
                        + " FROM sensor_data_aggr, user, sensor"
                        + " WHERE sensor.id = sensor_data_aggr.sensor_id"
                        + " AND sensor.id = ?"
                        + " AND user.id = sensor.user_id"
                        + " AND user.id = ?"
                        + " AND timestamp_end-timestamp_start == ?"
                        + " AND timestamp_start > ?"
                        + " ORDER BY timestamp_start ASC");
        stmt.setLong(1, sensor.getId());
        stmt.setLong(2, sensor.getUser().getId());
        stmt.setLong(3, TimeUtility.HOUR_IN_MS - 1);
        stmt.setLong(4, (System.currentTimeMillis() - TimeUtility.WEEK_IN_MS));
        return DBConnection.exec(stmt, new AggregateDataListSqlResult());
    }

    public static List<AggregateData> getDayAggregateData(DBConnection db, Sensor sensor) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement(
                "SELECT user.username as username,"
                        + " sensor_data_aggr.timestamp_start as timestamp_start,"
                        + " sensor_data_aggr.timestamp_end as timestamp_end,"
                        + " sensor_data_aggr.data as data,"
                        + " sensor_data_aggr.confidence as confidence,"
                        + TimeUtility.DAY_IN_MS + " as period_length"
                        + " FROM sensor_data_aggr, user, sensor"
                        + " WHERE sensor.id = sensor_data_aggr.sensor_id"
                        + " AND sensor.id = ?"
                        + " AND user.id = sensor.user_id"
                        + " AND user.id = ?"
                        + " AND timestamp_end-timestamp_start == ?"
                        + " ORDER BY timestamp_start ASC");
        stmt.setLong(1, sensor.getId());
        stmt.setLong(2, sensor.getUser().getId());
        stmt.setLong(3, TimeUtility.DAY_IN_MS-1);
        return DBConnection.exec(stmt, new AggregateDataListSqlResult());
    }


	@Override
	public ArrayList<AggregateData> handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
		ArrayList<AggregateData> list = new ArrayList<>();
		long previousTimestampEnd = -1;
		while(result.next()){

			long timestampStart = result.getLong("timestamp_start");
			long timestampEnd = result.getLong("timestamp_end");
			long periodLength = result.getLong("period_length");
			int data = result.getInt("data");
			String username = result.getString("username");
			float confidence = result.getFloat("confidence");

			//add null data point to list if one or more periods of data is missing before this
			if(previousTimestampEnd != -1 && timestampStart-previousTimestampEnd > periodLength){
				list.add(new AggregateData(previousTimestampEnd+1, "null", username));
			}

			//add this data point to list
			list.add(new AggregateData(timestampStart, ""+ (data/1000.0), username));

			//update previous end timestamp
			previousTimestampEnd = timestampEnd;
		}
		return list;
	}
}