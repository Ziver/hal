package se.hal.page;

import se.hal.EventControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalEventData;
import se.hal.intf.HalWebPage;
import se.hal.struct.Event;
import se.hal.struct.devicedata.ColorEventData;
import se.hal.struct.devicedata.LevelEventData;
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


    public EventOverviewWebPage() {
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

        if ("modify".equals(request.get("action"))) {
            int id = (ObjectUtil.isEmpty(request.get("action-id")) ? -1 : Integer.parseInt(request.get("action-id")));

            // change event data
            HalEventData eventData = null;

            switch (request.get("type")) {
                case "level":
                    eventData = new LevelEventData(Integer.parseInt(request.get("data")) / 100.0, 0);
                    break;

                case "color":
                    eventData = new ColorEventData(request.get("data"), 0);
                    break;

                case "on-off":
                    eventData = new OnOffEventData("on".equals(request.get("data")), 0);
                    break;
            }

            if (eventData != null) {
                logger.info("Modifying Event(" + id + ") state: " + eventData.toString());
                Event event = Event.getEvent(db, id);
                EventControllerManager.getInstance().send(event, eventData);
            } else {
                logger.warning("Unable to process event change request, data type most likely not supported: " + request.get("type"));
            }
        }

        int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));

        // --------------------------------------
        // Detailed page
        // --------------------------------------
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
        // --------------------------------------
        // Overview page
        // --------------------------------------
        else {
            List<Event> events = Event.getLocalEvents(db);
            Collections.sort(events, DeviceNameComparator.getInstance());

            Templator tmpl = new Templator(FileUtil.find(OVERVIEW_TEMPLATE));
            tmpl.set("events", events);
            return tmpl;
        }
    }
}
