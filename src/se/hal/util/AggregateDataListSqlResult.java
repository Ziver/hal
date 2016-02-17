package se.hal.util;

import se.hal.deamon.SensorDataAggregatorDaemon.AggregationPeriodLength;
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
    
    public static List<AggregateData> getAggregateDataForPeriod(DBConnection db, Sensor sensor, AggregationPeriodLength aggrPeriodLength, long ageLimitInMs) throws SQLException {
    	PreparedStatement stmt = db.getPreparedStatement(
                "SELECT user.username as username,"
                        + " sensor_data_aggr.timestamp_start as timestamp_start,"
                        + " sensor_data_aggr.timestamp_end as timestamp_end,"
                        + " sensor_data_aggr.data as data,"
                        + " sensor_data_aggr.confidence as confidence "
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
        switch(aggrPeriodLength){
			case SECOND: stmt.setLong(3, TimeUtility.SECOND_IN_MS-1); break;
			case MINUTE: stmt.setLong(3, TimeUtility.MINUTE_IN_MS-1); break; 
			case FIVE_MINUTES: stmt.setLong(3, TimeUtility.FIVE_MINUTES_IN_MS-1); break;
			case FIFTEEN_MINUTES: stmt.setLong(3, TimeUtility.FIFTEEN_MINUTES_IN_MS-1); break;
			case HOUR: stmt.setLong(3, TimeUtility.HOUR_IN_MS-1); break;
			case DAY: stmt.setLong(3, TimeUtility.DAY_IN_MS-1); break;
			case WEEK: stmt.setLong(3, TimeUtility.WEEK_IN_MS-1); break;
			default: throw new IllegalArgumentException("selected aggrPeriodLength is not supported");
		}
        stmt.setLong(4, (System.currentTimeMillis() - ageLimitInMs) );
        return DBConnection.exec(stmt , new AggregateDataListSqlResult());
    }


	@Override
	public ArrayList<AggregateData> handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
		ArrayList<AggregateData> list = new ArrayList<>();
		long previousTimestampEnd = -1;
		while(result.next()){

			long timestampStart = result.getLong("timestamp_start");
			long timestampEnd = result.getLong("timestamp_end");
			String username = result.getString("username");
			float confidence = result.getFloat("confidence");

			//Calculate the data point
			float data = result.getFloat("data");	//the "raw" recorded data
			float estimatedData = data/confidence;	//estimate the "real" value of the data by looking at the confidence value

			//add null data point to list if one or more periods of data is missing before this
			if(previousTimestampEnd != -1 && previousTimestampEnd+1 < timestampStart){
				list.add(new AggregateData(previousTimestampEnd+1, "null", username));
			}

			list.add(new AggregateData(timestampStart, ""+ (estimatedData/1000.0), username));	//add this data point to list

			//update previous end timestamp
			previousTimestampEnd = timestampEnd;
		}
		return list;
	}
}