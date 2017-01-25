package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.TriggerManager;
import se.hal.intf.HalHttpPage;
import se.hal.struct.Event;
import se.hal.struct.TriggerFlow;
import se.hal.struct.devicedata.SwitchEventData;
import se.hal.util.HistoryDataListSqlResult;
import se.hal.util.HistoryDataListSqlResult.HistoryData;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;

public class TriggerHttpPage extends HalHttpPage {
    private static final String TEMPLATE = "resource/web/trigger.tmpl";


    public TriggerHttpPage(){
        super("trigger");
        super.getRootNav().createSubNav("Events").createSubNav(this.getId(), "Triggers");
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{
        DBConnection db = HalContext.getDB();

        if(request.containsKey("action")){
            switch(request.get("action")) {
                // Local Sensors
                case "create_flow":
                    TriggerFlow flow = new TriggerFlow();
                    flow.save(db);
                    break;
            }
        }


        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("availableTriggers", TriggerManager.getInstance().getAvailableTriggers());
        tmpl.set("availableActions", TriggerManager.getInstance().getAvailableActions());
        tmpl.set("flows", TriggerFlow.getTriggerFlows(db));
        return tmpl;
    }
}
