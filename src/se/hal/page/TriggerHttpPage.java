package se.hal.page;

import se.hal.HalContext;
import se.hal.TriggerManager;
import se.hal.intf.HalAction;
import se.hal.intf.HalHttpPage;
import se.hal.intf.HalTrigger;
import se.hal.struct.Action;
import se.hal.struct.ClassConfigurationData;
import se.hal.struct.Trigger;
import se.hal.struct.TriggerFlow;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;

import java.util.ArrayList;
import java.util.Map;

public class TriggerHttpPage extends HalHttpPage {
    private static final String TEMPLATE = "resource/web/trigger.tmpl";

    private ArrayList<ClassConfigurationData> triggerConfigurators;
    private ArrayList<ClassConfigurationData> actionConfigurators;


    public TriggerHttpPage(){
        super("trigger");
        super.getRootNav().createSubNav("Events").createSubNav(this.getId(), "Triggers");

        triggerConfigurators = new ArrayList<>();
        for(Class c : TriggerManager.getInstance().getAvailableTriggers())
            triggerConfigurators.add(new ClassConfigurationData(c));
        actionConfigurators = new ArrayList<>();
        for(Class c : TriggerManager.getInstance().getAvailableActions())
            actionConfigurators.add(new ClassConfigurationData(c));
    }


    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{
        DBConnection db = HalContext.getDB();

        if(request.containsKey("action")){
            TriggerFlow flow = null;
            if (request.containsKey("flow_id"))
                flow = TriggerFlow.getTriggerFlow(db, Integer.parseInt(request.get("flow_id")));
            Trigger trigger = null;
            if (request.containsKey("trigger_id"))
                trigger = Trigger.getTrigger(db, Integer.parseInt(request.get("trigger_id")));
            Action action = null;
            if (request.containsKey("action_id"))
                action = Action.getAction(db, Integer.parseInt(request.get("action_id")));


            switch(request.get("action")) {
                // Flows
                case "create_flow":
                    flow = new TriggerFlow();
                    flow.save(db);
                    break;
                case "remove_flow":
                    flow.delete(db);
                    break;

                // Triggers
                case "create_trigger":
                    //TODO: trigger = new HalTrigger();
                    flow.addTrigger(trigger);
                case "modify_trigger":
                    // TODO: save attrib
                    trigger.save(db);
                    break;
                case "remove_trigger":
                    flow.removeTrigger(trigger);
                    trigger.delete(db);
                    break;

                // Triggers
                case "create_action":
                    //TODO: action = new HalAction();
                    flow.addAction(action);
                case "modify_action":
                    // TODO: save attrib
                    action.save(db);
                    break;
                case "remove_action":
                    flow.removeAction(action);
                    action.delete(db);
                    break;
            }
        }


        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("triggerConf", triggerConfigurators);
        tmpl.set("actionConf", actionConfigurators);
        tmpl.set("availableTriggers", TriggerManager.getInstance().getAvailableTriggers());
        tmpl.set("availableActions", TriggerManager.getInstance().getAvailableActions());
        tmpl.set("flows", TriggerFlow.getTriggerFlows(db));
        return tmpl;
    }
}
