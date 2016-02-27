package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalEventData;
import se.hal.intf.HalHttpPage;
import se.hal.struct.Event;
import se.hal.struct.SwitchEventData;
import se.hal.util.HistoryDataListSqlResult;
import se.hal.util.HistoryDataListSqlResult.HistoryData;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

public class EventOverviewHttpPage extends HalHttpPage {
    private static final int HISTORY_LIMIT = 200;
    private static final String OVERVIEW_TEMPLATE = "resource/web/event_overview.tmpl";
    private static final String DETAIL_TEMPLATE = "resource/web/event_detail.tmpl";


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
            HalEventData eventData = event.getDeviceData();
            if (eventData instanceof SwitchEventData){
                if ( request.containsKey("data") && "on".equals(request.get("data")))
                    ((SwitchEventData)eventData).turnOn();
                else
                    ((SwitchEventData)eventData).turnOff();
            }
            ControllerManager.getInstance().send(event);
        }

        // Save new input
        if(!request.containsKey("action") && id >= 0){
            Event event = Event.getEvent(db, id);

            // get history data
            PreparedStatement stmt = db.getPreparedStatement(
                    "SELECT * FROM event_data_raw WHERE event_id == ? ORDER BY timestamp DESC LIMIT ?");
            stmt.setLong(1, event.getId());
            stmt.setLong(2, HISTORY_LIMIT);
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
}
