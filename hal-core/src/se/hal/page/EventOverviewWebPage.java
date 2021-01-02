package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import se.hal.struct.Event;
import se.hal.struct.devicedata.OnOffEventData;
import se.hal.util.DeviceNameComparator;
import se.hal.util.HistoryDataListSqlResult;
import se.hal.util.HistoryDataListSqlResult.HistoryData;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class EventOverviewWebPage extends HalWebPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final int HISTORY_LIMIT = 200;
    private static final String OVERVIEW_TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/event_overview.tmpl";
    private static final String DETAIL_TEMPLATE   = HalContext.RESOURCE_WEB_ROOT + "/event_detail.tmpl";


    public EventOverviewWebPage(){
        super("event_overview");
        super.getRootNav().createSubNav("Events").createSubNav(this.getId(), "Overview");
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        DBConnection db = HalContext.getDB();

        if (request.containsKey("action")) {
            int id = (ObjectUtil.isEmpty(request.get("action_id")) ? -1 : Integer.parseInt(request.get("action_id")));

            // change event data
            OnOffEventData eventData = new OnOffEventData();
            if (request.containsKey("enabled") && "on".equals(request.get("enabled")))
                eventData.turnOn();
            else
                eventData.turnOff();

            logger.info("Modifying Event(" + id + ") state: " + eventData.toString());
            Event event = Event.getEvent(db, id);
            event.setDeviceData(eventData);
            ControllerManager.getInstance().send(event);
        }

        int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));

        // Save new input
        if (id >= 0) {
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
            List<Event> events = Event.getLocalEvents(db);
            Collections.sort(events, DeviceNameComparator.getInstance());

            Templator tmpl = new Templator(FileUtil.find(OVERVIEW_TEMPLATE));
            tmpl.set("events", events);
            return tmpl;
        }
    }
}
