package se.hal.page;

import se.hal.HalContext;
import se.hal.deamon.SensorDataAggregatorDaemon.AggregationPeriodLength;
import se.hal.intf.HalHttpPage;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import se.hal.util.AggregateDataListSqlResult;
import se.hal.util.AggregateDataListSqlResult.AggregateData;
import se.hal.util.UTCTimeUtility;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PCOverviewHttpPage extends HalHttpPage {
	private static final String TEMPLATE = "resource/web/pc_overview.tmpl";

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

		List<User> users = User.getUsers(db);

		ArrayList<AggregateData> minDataList = new ArrayList<>();
		ArrayList<AggregateData> hourDataList = new ArrayList<>();
		ArrayList<AggregateData> dayDataList = new ArrayList<>();
		ArrayList<AggregateData> weekDataList = new ArrayList<>();

		for(User user : users){
			List<Sensor> userSensors = Sensor.getSensors(db, user);
			for(Sensor sensor : userSensors){
				minDataList.addAll(AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.FIVE_MINUTES, UTCTimeUtility.DAY_IN_MS));

				hourDataList.addAll(AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.HOUR, UTCTimeUtility.WEEK_IN_MS));

				dayDataList.addAll(AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.DAY, UTCTimeUtility.INFINITY));

				weekDataList.addAll(AggregateDataListSqlResult.getAggregateDataForPeriod(db, sensor, AggregationPeriodLength.WEEK, UTCTimeUtility.INFINITY));
			}
		}


		Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
		tmpl.set("minData", minDataList);
		tmpl.set("hourData", hourDataList);
		tmpl.set("dayData", dayDataList);
		tmpl.set("weekData", weekDataList);
		tmpl.set("username", User.getUsers(db));

		return tmpl;
	}

}
