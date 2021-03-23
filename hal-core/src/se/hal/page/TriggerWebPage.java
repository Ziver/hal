package se.hal.page;

import se.hal.HalContext;
import se.hal.TriggerManager;
import se.hal.intf.HalWebPage;
import se.hal.struct.Action;
import se.hal.struct.ClassConfigurationData;
import se.hal.struct.Trigger;
import se.hal.struct.TriggerFlow;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;
import zutil.ui.UserMessageManager;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

import static zutil.ui.UserMessageManager.*;

public class TriggerWebPage extends HalWebPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/trigger.tmpl";

    private ArrayList<ClassConfigurationData> triggerConfigurators;
    private ArrayList<ClassConfigurationData> actionConfigurators;


    public TriggerWebPage() {
        super("trigger");
        super.getRootNav().createSubNav("Events").createSubNav(this.getId(), "Triggers");

        triggerConfigurators = new ArrayList<>();
        for (Class c : TriggerManager.getInstance().getAvailableTriggers())
            triggerConfigurators.add(new ClassConfigurationData(c));
        actionConfigurators = new ArrayList<>();
        for (Class c : TriggerManager.getInstance().getAvailableActions())
            actionConfigurators.add(new ClassConfigurationData(c));
    }

    @Override
    public Templator httpRespond (
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {
        DBConnection db = HalContext.getDB();

        if (request.containsKey("action")) {
            TriggerFlow flow = (ObjectUtil.isEmpty(request.get("flow-id")) ? null :
                    TriggerFlow.getTriggerFlow(db, Integer.parseInt(request.get("flow-id"))));
            Trigger trigger = (ObjectUtil.isEmpty(request.get("trigger-id")) ? null :
                    Trigger.getTrigger(db, Integer.parseInt(request.get("trigger-id"))));
            Action action = (ObjectUtil.isEmpty(request.get("action-id")) ? null :
                    Action.getAction(db, Integer.parseInt(request.get("action-id"))));

            switch(request.get("action")) {
                // Flows
                case "create_flow":
                    logger.info("Creating new flow.");
                    flow = new TriggerFlow();
                    flow.save(db);
                    break;

                case "modify_flow":
                    logger.info("Modifying flow: " + flow.getName());
                    flow.setEnabled("on".equals(request.get("enabled")));
                    flow.setName(request.get("name"));
                    flow.save(db);
                    break;

                case "remove_flow":
                    logger.info("Removing flow: " + flow.getName());
                    flow.delete(db);
                    break;

                // Triggers
                case "create_trigger":
                    if (flow == null) {
                        logger.warning("Invalid flow id: " + request.get("flow-id"));
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Invalid flow id: " + request.get("flow-id"), MessageTTL.ONE_VIEW));
                        break;
                    }

                    logger.info("Creating trigger associated to flow: " + flow.getName());
                    trigger = new Trigger();
                    flow.addTrigger(trigger);
                    flow.save(db);
                    /* FALLTHROUGH */
                case "modify_trigger":
                    logger.info("Modifying trigger: " + trigger.getId());
                    trigger.setObjectClass(request.get("type"));
                    trigger.getObjectConfigurator().setValues(request).applyConfiguration();
                    trigger.save(db); // will save all sub beans also
                    break;

                case "remove_trigger":
                    if (flow == null)
                        flow = TriggerFlow.getTriggerFlow(db, trigger);
                    logger.info("Removing trigger: " + trigger.getId());
                    flow.removeTrigger(trigger);
                    trigger.delete(db);
                    break;

                // Triggers
                case "create_action":
                    if (flow == null) {
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Invalid flow id", MessageTTL.ONE_VIEW));
                        break;
                    }

                    logger.info("Creating action associated with flow: " + flow.getName());
                    action = new Action();
                    flow.addAction(action);
                    flow.save(db);
                    /* FALLTHROUGH */
                case "modify_action":
                    logger.info("Modifying action: " + action.getId());
                    action.setObjectClass(request.get("type"));
                    action.getObjectConfigurator().setValues(request).applyConfiguration();
                    action.save(db); // will save all sub beans also
                    break;

                case "remove_action":
                    if (flow == null)
                        flow = TriggerFlow.getTriggerFlow(db, action);
                    logger.info("Removing action: " + action.getId());
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
