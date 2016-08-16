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
        public int id;
		public long timestamp;
		public Float data;
		public String username;

		public AggregateData(int id, long time, Float data, String uname) {
            this.id = id;
			this.timestamp = time;
			this.data = data;
			this.username = uname;
		}
	}
    
    public static List<AggregateData> getAggregateDataForPeriod(DBConnection db, Sensor sensor, AggregationPeriodLength aggrPeriodLength, long ageLimitInMs) throws SQLException {
    	PreparedStatement stmt = db.getPreparedStatement(
                "SELECT user.username as username,"
                        + " sensor.*,"
                        + " sensor_data_aggr.*"
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
			case SECOND: stmt.setLong(3, UTCTimeUtility.SECOND_IN_MS-1); break;
			case MINUTE: stmt.setLong(3, UTCTimeUtility.MINUTE_IN_MS-1); break; 
			case FIVE_MINUTES: stmt.setLong(3, UTCTimeUtility.FIVE_MINUTES_IN_MS-1); break;
			case FIFTEEN_MINUTES: stmt.setLong(3, UTCTimeUtility.FIFTEEN_MINUTES_IN_MS-1); break;
			case HOUR: stmt.setLong(3, UTCTimeUtility.HOUR_IN_MS-1); break;
			case DAY: stmt.setLong(3, UTCTimeUtility.DAY_IN_MS-1); break;
			case WEEK: stmt.setLong(3, UTCTimeUtility.WEEK_IN_MS-1); break;
			default: throw new IllegalArgumentException("selected aggrPeriodLength is not supported");
		}
        stmt.setLong(4, (System.currentTimeMillis() - ageLimitInMs) );
        return DBConnection.exec(stmt , new AggregateDataListSqlResult(sensor));
    }


    private Sensor sensor;

    private AggregateDataListSqlResult(Sensor sensor){
    	this.sensor = sensor;
	}



	@Override
	public ArrayList<AggregateData> handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
		ArrayList<AggregateData> list = new ArrayList<>();
		long previousTimestampEnd = -1;
		while (result.next()){

            int id = result.getInt("id");
			long timestampStart = result.getLong("timestamp_start");
			long timestampEnd = result.getLong("timestamp_end");
			String username = result.getString("username");
			float confidence = result.getFloat("confidence");

			// Calculate the data point
			float data = result.getFloat("data");	//the "raw" recorded data
			float estimatedData = data/confidence;	//estimate the "real" value of the data by looking at the confidence value

            // Add null data point to list if one or more periods of data is missing before this
			if (previousTimestampEnd != -1 && sensor.getDeviceConfig() != null){
                boolean shortInterval = timestampEnd-timestampStart < sensor.getDeviceConfig().getDataInterval();
                long distance = timestampStart - (previousTimestampEnd + 1);
				if (// Only add nulls if the report interval is smaller than the aggregated interval
                        !shortInterval && distance > 0 ||
                        // Only add nulls if space between aggr is larger than sensor report interval
                        shortInterval && distance > sensor.getDeviceConfig().getDataInterval())
					 list.add(new AggregateData(id, previousTimestampEnd + 1, null /*Float.NaN*/, username));
			}

			list.add(new AggregateData(id, timestampEnd, (estimatedData/1000f), username));	//add this data point to list

			// Update previous end timestamp
			previousTimestampEnd = timestampEnd;
		}
		return list;
	}
}