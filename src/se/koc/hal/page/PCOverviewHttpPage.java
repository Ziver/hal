package se.koc.hal.page;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

import se.koc.hal.HalContext;
import se.koc.hal.deamon.DataAggregatorDaemon;
import zutil.db.SQLResultHandler;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeaderParser;
import zutil.net.http.HttpPage;
import zutil.net.http.HttpPrintStream;
import zutil.parser.Templator;

public class PCOverviewHttpPage implements HttpPage {

	@Override
	public void respond(HttpPrintStream out, HttpHeaderParser client_info, Map<String, Object> session, Map<String, String> cookie, Map<String, String> request) throws IOException {
		
		try {
			ArrayList<PowerData> minDataList = HalContext.db.exec(
					"SELECT user.username as username, "
						+ "sensor_data_aggr.timestamp_start as timestamp_start, "
						+ "sensor_data_aggr.timestamp_end as timestamp_end , "
						+ "sensor_data_aggr.data as data, "
						+ "sensor_data_aggr.confidence as confidence, "
						+ DataAggregatorDaemon.FIVE_MINUTES_IN_MS + " as period_length"
					+ "FROM sensor_data_aggr, user, sensor "
					+ "WHERE sensor.id = sensor_data_aggr.sensor_id "
						+ "AND user.id = sensor.user_id "
						+ "AND timestamp_end-timestamp_start == " + (DataAggregatorDaemon.FIVE_MINUTES_IN_MS-1)
						+ "AND timestamp_start > " + (System.currentTimeMillis() - DataAggregatorDaemon.DAY_IN_MS)
					+ "ORDER BY timestamp_start ASC",
					new SQLPowerDataBuilder());
			ArrayList<PowerData> hourDataList = HalContext.db.exec(
					"SELECT user.username as username, "
						+ "sensor_data_aggr.timestamp_start as timestamp_start, "
						+ "sensor_data_aggr.timestamp_end as timestamp_end , "
						+ "sensor_data_aggr.data as data, "
						+ "sensor_data_aggr.confidence as confidence, "
						+ DataAggregatorDaemon.HOUR_IN_MS + " as period_length"
					+ "FROM sensor_data_aggr, user, sensor "
					+ "WHERE sensor.id = sensor_data_aggr.sensor_id "
						+ "AND user.id = sensor.user_id "
						+ "AND timestamp_end-timestamp_start == " + (DataAggregatorDaemon.HOUR_IN_MS-1)
						+ "AND timestamp_start > " + (System.currentTimeMillis() - 3*DataAggregatorDaemon.DAY_IN_MS)
					+ "ORDER BY timestamp_start ASC", 
					new SQLPowerDataBuilder());
			ArrayList<PowerData> dayDataList = HalContext.db.exec(
					"SELECT user.username as username, "
						+ "sensor_data_aggr.timestamp_start as timestamp_start, "
						+ "sensor_data_aggr.timestamp_end as timestamp_end , "
						+ "sensor_data_aggr.data as data, "
						+ "sensor_data_aggr.confidence as confidence, "
						+ DataAggregatorDaemon.DAY_IN_MS + " as period_length"
					+ "FROM sensor_data_aggr, user, sensor "
					+ "WHERE sensor.id = sensor_data_aggr.sensor_id "
						+ "AND user.id = sensor.user_id "
						+ "AND timestamp_end-timestamp_start == " + (DataAggregatorDaemon.DAY_IN_MS-1)
					+ "ORDER BY timestamp_start ASC",
					new SQLPowerDataBuilder());
		
		
			Templator tmpl = new Templator(FileUtil.find("web-resource/index.html"));
			tmpl.set("minData", minDataList);
			tmpl.set("hourData", hourDataList);
			tmpl.set("dayData", dayDataList);
			tmpl.set("username", new String[]{"Ziver", "Daniel"});
			
			out.print(tmpl.compile());
		
		} catch (SQLException e) {
			throw new IOException(e);
		}
	}
	
	public static class PowerData{
		long timestamp;
		String data;
		String username;
		public PowerData(long time, String data, String uname) {
			this.timestamp = time;
			this.data = data;
			this.username = uname;
		}
	}
	
	private static class SQLPowerDataBuilder implements SQLResultHandler<ArrayList<PowerData>> {
		@Override
		public ArrayList<PowerData> handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			ArrayList<PowerData> list = new ArrayList<>();
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
					list.add(new PowerData(previousTimestampEnd+1, "undefined", username));
				}
				
				//add this data point to list
				list.add(new PowerData(timestampStart, data/1000+"", username));
				
				//update previous end timestamp
				previousTimestampEnd = timestampEnd;
			}
			return list;
		}
	}

}
