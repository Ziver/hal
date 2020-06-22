package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import se.hal.page.HalAlertManager.AlertLevel;
import se.hal.page.HalAlertManager.AlertTTL;
import se.hal.page.HalAlertManager.HalAlert;
import se.hal.struct.ClassConfigurationData;
import se.hal.struct.Event;
import se.hal.struct.User;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;

import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Logger;

public class EventConfigWebPage extends HalWebPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TEMPLATE = "resource/web/event_config.tmpl";

    private ArrayList<ClassConfigurationData> eventConfigurations;


    public EventConfigWebPage() {
        super("event_config");
        super.getRootNav().createSubNav("Settings").createSubNav(this.getId(), "Event Settings").setWeight(100);

        eventConfigurations = new ArrayList<>();
        for(Class c : ControllerManager.getInstance().getAvailableEvents())
            eventConfigurations.add(new ClassConfigurationData(c));
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
        if(request.containsKey("action")){
            int id = (ObjectUtil.isEmpty(request.get("id")) ? -1 : Integer.parseInt(request.get("id")));
            Event event;

            switch(request.get("action")) {
                // Local events
                case "create_local_event":
                    logger.info("Creating new event: " + request.get("name"));
                    event = new Event();
                    event.setName(request.get("name"));
                    event.setType(request.get("type"));
                    event.setUser(localUser);
                    event.getDeviceConfigurator().setValues(request).applyConfiguration();
                    event.save(db);
                    ControllerManager.getInstance().register(event);

                    HalAlertManager.getInstance().addAlert(new HalAlert(
                            AlertLevel.SUCCESS, "Successfully created new event: " + event.getName(), AlertTTL.ONE_VIEW));
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

                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.SUCCESS, "Successfully saved event: "+event.getName(), AlertTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown event id: " + id);
                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.ERROR, "Unknown event id: " + id, AlertTTL.ONE_VIEW));
                    }
                    break;

                case "remove_local_event":
                    event = Event.getEvent(db, id);
                    if (event != null) {
                        logger.info("Removing event: " + event.getName());
                        ControllerManager.getInstance().deregister(event);
                        event.delete(db);

                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.SUCCESS, "Successfully removed event: "+event.getName(), AlertTTL.ONE_VIEW));
                    } else {
                        logger.warning("Unknown event id: " + id);
                        HalAlertManager.getInstance().addAlert(new HalAlert(
                                AlertLevel.ERROR, "Unknown event id: "+id, AlertTTL.ONE_VIEW));
                    }
                    break;

                case "remove_all_detected_events":
                    ControllerManager.getInstance().clearDetectedEvents();
                    break;
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("user", localUser);
        tmpl.set("localEvents", Event.getLocalEvents(db));
        tmpl.set("localEventConf", eventConfigurations);
        tmpl.set("detectedEvents", ControllerManager.getInstance().getDetectedEvents());
        tmpl.set("availableEvents", ControllerManager.getInstance().getAvailableEvents());

        return tmpl;

    }

}
