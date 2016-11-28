package se.hal.page;

import se.hal.HalContext;
import se.hal.deamon.SensorDataAggregatorDaemon.AggregationPeriodLength;
import se.hal.intf.HalHttpPage;
import se.hal.struct.devicedata.PowerConsumptionSensorData;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import se.hal.util.AggregateDataListSqlResult;
import se.hal.util.AggregateDataListSqlResult.AggregateData;
import se.hal.util.UTCTimeUtility;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.DataNode;
import zutil.parser.Templator;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PCOverviewHttpPage extends HalHttpPage {
    private static final String TEMPLATE = "resource/web/pc_overview.tmpl";

    public PCOverviewHttpPage() {
        super("pc_overview");
        super.getRootNav().createSubNav("Sensors").createSubNav(this.getId(), "Power;Challenge").setWeight(50);
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        DBConnection db = HalContext.getDB();

        List<User> users = User.getUsers(db);
        List<Sensor> sensors = Sensor.getSensors(db);

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("users", User.getUsers(db));
        tmpl.set("sensors", sensors);

        return tmpl;
    }


}
