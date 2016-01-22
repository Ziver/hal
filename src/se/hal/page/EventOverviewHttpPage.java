package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalEventData;
import se.hal.intf.HalHttpPage;
import se.hal.struct.Event;
import se.hal.struct.SwitchEventData;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;
import zutil.ui.Configurator;
import zutil.ui.Configurator.ConfigurationParam;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventOverviewHttpPage extends HalHttpPage {
    private static final String OVERVIEW_TEMPLATE = "web-resource/event_overview.tmpl";
    private static final String DETAIL_TEMPLATE = "web-resource/event_detail.tmpl";


    public EventOverviewHttpPage(){
        super("Overview", "event_overview");
        super.getRootNav().getSubNav("events").addSubNav(super.getNav());
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        DBConnection db = HalContext.getDB();
        int id = (request.containsKey("id") ? Integer.parseInt(request.get("id")) : -1);

        if(request.containsKey("action")){
            // change event data
            Event event = Event.getEvent(db, id);
            if (event instanceof SwitchEventData){
                if ( ! ((SwitchEventData)event).isOn())
                    ((SwitchEventData)event).turnOn();
                else
                    ((SwitchEventData)event).turnOff();
            }
            ControllerManager.getInstance().send(event);
        }

        // Save new input
        if(!request.containsKey("action") && id >= 0){
            Event event = Event.getEvent(db, id);

            // get history data
            PreparedStatement stmt = db.getPreparedStatement("SELECT * FROM event_data_raw WHERE event_id == ?");
            stmt.setLong(1, event.getId());
            List<HistoryData> history = DBConnection.exec(stmt, new HistoryDataListSqlResult());

            Templator tmpl = new Templator(FileUtil.find(DETAIL_TEMPLATE));
            tmpl.set("event", event);
            tmpl.set("history", history);
            return tmpl;
        }
        else {
            Templator tmpl = new Templator(FileUtil.find(OVERVIEW_TEMPLATE));
            tmpl.set("events", Event.getLocalEvents(db));
            return tmpl;
        }
    }



    protected static class HistoryData{
        public long timestamp;
        public double data;
    }

    protected class HistoryDataListSqlResult implements SQLResultHandler<List<HistoryData>> {
        @Override
        public List<HistoryData> handleQueryResult(Statement stmt, ResultSet result) throws SQLException {
            ArrayList<HistoryData> list = new ArrayList<HistoryData>();
            while(result.next()){
                HistoryData data = new HistoryData();
                data.timestamp = result.getLong("timestamp");
                data.data = result.getLong("data");
                list.add(data);
            }
            return list;
        }
    }
}