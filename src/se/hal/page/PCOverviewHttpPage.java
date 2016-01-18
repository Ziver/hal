package se.hal.page;

import se.hal.HalContext;
import se.hal.intf.HalHttpPage;
import se.hal.util.TimeUtility;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PCOverviewHttpPage extends HalHttpPage {
	private static final String TEMPLATE = "web-resource/pc_overview.tmpl";

	public PCOverviewHttpPage() {
		super("Power;Challenge", "pc_overview");
        super.getRootNav().getSubNav("sensors").addSubNav(super.getNav());
	}

	@Override
	public Templator httpRespond(
			Map<String, Object> session,
			Map<String, String> cookie,
			Map<String, String> request)
					throws Exception{

		DBConnection db = HalContext.getDB();

		List<User> users = User.getAllUsers(db);

		ArrayList<PowerData> minDataList = new ArrayList<PowerData>();
		ArrayList<PowerData> hourDataList = new ArrayList<PowerData>();
		ArrayList<PowerData> dayDataList = new ArrayList<PowerData>();

		for(User user : users){
			List<Sensor> userSensors = Sensor.getSensors(db, user);
			for(Sensor sensor : userSensors){
				PreparedStatement stmt = db.getPreparedStatement(
						"SELECT user.username as username,"
								+ " sensor_data_aggr.timestamp_start as timestamp_start,"
								+ " sensor_data_aggr.timestamp_end as timestamp_end,"
								+ " sensor_data_aggr.data as data,"
								+ " sensor_data_aggr.confidence as confidence,"
								+ TimeUtility.FIVE_MINUTES_IN_MS + " as period_length"
								+ " FROM sensor_data_aggr, user, sensor"
								+ " WHERE sensor.id = sensor_data_aggr.sensor_id"
								+ " AND sensor.id = " + sensor.getId()
								+ " AND user.id = sensor.user_id"
								+ " AND user.id = " + user.getId()
								+ " AND timestamp_end-timestamp_start == ?"
								+ " AND timestamp_start > ?"
								+ " ORDER BY timestamp_start ASC");
				stmt.setLong(1, TimeUtility.FIVE_MINUTES_IN_MS-1);
				stmt.setLong(2, (System.currentTimeMillis() - TimeUtility.DAY_IN_MS) );
				ArrayList<PowerData> userPowerData = DBConnection.exec(stmt , new SQLPowerDataBuilder());
				minDataList.addAll(userPowerData);

				stmt = db.getPreparedStatement(
						"SELECT user.username as username,"
								+ " sensor_data_aggr.timestamp_start as timestamp_start,"
								+ " sensor_data_aggr.timestamp_end as timestamp_end,"
								+ " sensor_data_aggr.data as data,"
								+ " sensor_data_aggr.confidence as confidence,"
								+ TimeUtility.HOUR_IN_MS + " as period_length"
								+ " FROM sensor_data_aggr, user, sensor"
								+ " WHERE sensor.id = sensor_data_aggr.sensor_id"
								+ " AND sensor.id = " + sensor.getId()
								+ " AND user.id = sensor.user_id"
								+ " AND user.id = " + user.getId()
								+ " AND timestamp_end-timestamp_start == ?"
								+ " AND timestamp_start > ?"
								+ " ORDER BY timestamp_start ASC");
				stmt.setLong(1, TimeUtility.HOUR_IN_MS-1);
				stmt.setLong(2, (System.currentTimeMillis() - TimeUtility.WEEK_IN_MS) );
				userPowerData = DBConnection.exec(stmt, new SQLPowerDataBuilder());
				hourDataList.addAll(userPowerData);

				stmt = db.getPreparedStatement(
						"SELECT user.username as username,"
								+ " sensor_data_aggr.timestamp_start as timestamp_start,"
								+ " sensor_data_aggr.timestamp_end as timestamp_end,"
								+ " sensor_data_aggr.data as data,"
								+ " sensor_data_aggr.confidence as confidence,"
								+ TimeUtility.DAY_IN_MS + " as period_length"
								+ " FROM sensor_data_aggr, user, sensor"
								+ " WHERE sensor.id = sensor_data_aggr.sensor_id"
								+ " AND sensor.id = " + sensor.getId()
								+ " AND user.id = sensor.user_id"
								+ " AND user.id = " + user.getId()
								+ " AND timestamp_end-timestamp_start == ?"
								+ " ORDER BY timestamp_start ASC");
				stmt.setLong(1, TimeUtility.DAY_IN_MS-1);
				userPowerData = DBConnection.exec(stmt, new SQLPowerDataBuilder());
				dayDataList.addAll(userPowerData);
			}
		}


		Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
		tmpl.set("minData", minDataList);
		tmpl.set("hourData", hourDataList);
		tmpl.set("dayData", dayDataList);
		tmpl.set("username", new String[]{"Ziver", "Daniel"});

		return tmpl;
	}

	public static class PowerData{
		public long timestamp;
		public String data;
		public String username;
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
					list.add(new PowerData(previousTimestampEnd+1, "null", username));
				}

				//add this data point to list
				list.add(new PowerData(timestampStart, ""+ (data/1000.0), username));

				//update previous end timestamp
				previousTimestampEnd = timestampEnd;
			}
			return list;
		}
	}

}
