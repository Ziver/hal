package se.hal.page;

import se.hal.EventControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalAbstractController;
import se.hal.intf.HalAbstractControllerManager;
import se.hal.intf.HalScannableController;
import se.hal.intf.HalWebPage;
import se.hal.util.ClassConfigurationFacade;
import se.hal.struct.Event;
import se.hal.struct.User;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;
import zutil.ui.UserMessageManager.MessageLevel;
import zutil.ui.UserMessageManager.MessageTTL;
import zutil.ui.UserMessageManager.UserMessage;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;


public class EventConfigWebPage extends HalWebPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/event_config.tmpl";

    private ArrayList<ClassConfigurationFacade> eventConfigurations = new ArrayList<>();


    public EventConfigWebPage() {
        super("event_config");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Event Settings").setWeight(200);

        for (Class c : EventControllerManager.getInstance().getAvailableDeviceConfigs())
            eventConfigurations.add(new ClassConfigurationFacade(c));
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        DBConnection db = HalContext.getDB();
        User localUser = User.getLocalUser(db);

        // Save new input
        if (request.containsKey("action")) {
            int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));
            Event event;

            switch(request.get("action")) {
                case "create_local_event":
                    logger.info("Creating new event: " + request.get("name"));
                    event = new Event();
                    event.setName(request.get("name"));
                    event.setType(request.get("type"));
                    event.setUser(localUser);
                    event.getDeviceConfigurator().setValues(request).applyConfiguration();
                    event.save(db);
                    EventControllerManager.getInstance().register(event);

                    HalAlertManager.getInstance().addAlert(new UserMessage(
                            MessageLevel.SUCCESS, "Successfully created new event: " + event.getName(), MessageTTL.ONE_VIEW));
                    break;

                case "modify_local_event":
                    event = Event.getEvent(db, id);
                    if (event != null) {
                        logger.info("Modifying event: " + event.getName());
                        event.setName(request.get("name"));
                        event.setType(request.get("type"));
                        event.setUser(localUser);
                        event.getDeviceConfigurator().setValues(request).applyConfiguration();
                        event.save(db);

                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully saved event: "+event.getName(), MessageTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown event id: " + id);
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Unknown event id: " + id, MessageTTL.ONE_VIEW));
                    }
                    break;

                case "remove_local_event":
                    event = Event.getEvent(db, id);
                    if (event != null) {
                        logger.info("Removing event: " + event.getName());
                        EventControllerManager.getInstance().deregister(event);
                        event.delete(db);

                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.SUCCESS, "Successfully removed event: "+event.getName(), MessageTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown event id: " + id);
                        HalAlertManager.getInstance().addAlert(new UserMessage(
                                MessageLevel.ERROR, "Unknown event id: "+id, MessageTTL.ONE_VIEW));
                    }
                    break;

                case "remove_all_detected_events":
                    EventControllerManager.getInstance().clearDetectedDevices();
                    break;

                case "start_scan":
                    for (HalAbstractController controller : HalAbstractControllerManager.getControllers()) {
                        if (controller instanceof HalScannableController) {
                            ((HalScannableController) controller).startScan();

                            HalAlertManager.getInstance().addAlert(new UserMessage(
                                    MessageLevel.SUCCESS, "Initiated scanning on controller: " + controller.getClass().getName(), MessageTTL.ONE_VIEW));
                        }
                    }
                    break;
            }
        }

        // Is any scan active?
        boolean scanning = false;

        for (HalAbstractController controller : HalAbstractControllerManager.getControllers()) {
            if (controller instanceof HalScannableController) {
                scanning |= ((HalScannableController) controller).isScanning();
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("user", localUser);
        tmpl.set("scanning", scanning);
        tmpl.set("localEvents", Event.getLocalEvents(db));
        tmpl.set("detectedEvents", EventControllerManager.getInstance().getDetectedDevices());
        tmpl.set("availableEventConfigClasses", EventControllerManager.getInstance().getAvailableDeviceConfigs());
        tmpl.set("availableEventObjectConfig", eventConfigurations);

        return tmpl;
    }

}
