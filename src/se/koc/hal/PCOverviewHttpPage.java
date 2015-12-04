package se.koc.hal;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;

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
			ArrayList<PowerData> minDataList = PowerChallenge.db.exec(
					"SELECT user.username as username, sensor_data_aggr.timestamp_start as timestamp, sensor_data_aggr.data as data "
					+ "FROM sensor_data_aggr, user, sensor "
					+ "WHERE sensor.id = sensor_data_aggr.sensor_id "
					+ "AND user.id = sensor.user_id "
					+ "AND timestamp_end-timestamp_start == " + (DataAggregatorDaemon.FIVE_MINUTES_IN_MS-1), 
					new SQLPowerDataBuilder());
			ArrayList<PowerData> hourDataList = PowerChallenge.db.exec(
					"SELECT user.username as username, sensor_data_aggr.timestamp_start as timestamp, sensor_data_aggr.data as data "
					+ "FROM sensor_data_aggr, user, sensor "
					+ "WHERE sensor.id = sensor_data_aggr.sensor_id "
					+ "AND user.id = sensor.user_id "
					+ "AND timestamp_end-timestamp_start == " + (DataAggregatorDaemon.HOUR_IN_MS-1), 
					new SQLPowerDataBuilder());
			ArrayList<PowerData> dayDataList = PowerChallenge.db.exec(
					"SELECT user.username as username, sensor_data_aggr.timestamp_start as timestamp, sensor_data_aggr.data as data "
					+ "FROM sensor_data_aggr, user, sensor "
					+ "WHERE sensor.id = sensor_data_aggr.sensor_id "
					+ "AND user.id = sensor.user_id "
					+ "AND timestamp_end-timestamp_start == " + (DataAggregatorDaemon.DAY_IN_MS-1), 
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
		int data;
		String username;
		public PowerData(long time, int data, String uname) {
			this.timestamp = time;
			this.data = data;
			this.username = uname;
		}
	}
	
	private static class SQLPowerDataBuilder implements SQLResultHandler<ArrayList<PowerData>> {
		@Override
		public ArrayList<PowerData> handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
			ArrayList<PowerData> list = new ArrayList<>();
			while(result.next()){
				list.add(new PowerData(result.getLong("timestamp"), result.getInt("data"), result.getString("username")));
			}
			return list;
		}
	}

}
