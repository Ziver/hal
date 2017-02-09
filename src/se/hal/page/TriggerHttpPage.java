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
            if (request.containsKey("flow-id") && !request.get("flow-id").isEmpty())
                flow = TriggerFlow.getTriggerFlow(db, Integer.parseInt(request.get("flow-id")));
            Trigger trigger = null;
            if (request.containsKey("trigger-id") && !request.get("trigger-id").isEmpty())
                trigger = Trigger.getTrigger(db, Integer.parseInt(request.get("trigger-id")));
            Action action = null;
            if (request.containsKey("action-id") && !request.get("action-id").isEmpty())
                action = Action.getAction(db, Integer.parseInt(request.get("action-id")));


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
                    if (flow == null){
                        HalAlertManager.getInstance().addAlert(new HalAlertManager.HalAlert(
                                HalAlertManager.AlertLevel.ERROR, "Invalid flow id", HalAlertManager.AlertTTL.ONE_VIEW));
                        break;
                    }
                    trigger = new Trigger();
                    flow.addTrigger(trigger);
                    /* FALLTHROUGH */
                case "modify_trigger":
                    trigger.setObjectClass(request.get("type"));
                    trigger.getObjectConfigurator().setValues(request).applyConfiguration();
                    flow.save(db); // will save all sub beans also
                    break;
                case "remove_trigger":
                    flow.removeTrigger(trigger);
                    trigger.delete(db);
                    break;

                // Triggers
                case "create_action":
                    if (flow == null){
                        HalAlertManager.getInstance().addAlert(new HalAlertManager.HalAlert(
                                HalAlertManager.AlertLevel.ERROR, "Invalid flow id", HalAlertManager.AlertTTL.ONE_VIEW));
                        break;
                    }
                    action = new Action();
                    flow.addAction(action);
                    /* FALLTHROUGH */
                case "modify_action":
                    action.setObjectClass(request.get("type"));
                    action.getObjectConfigurator().setValues(request).applyConfiguration();
                    flow.save(db); // will save all sub beans also
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
