package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalEventData;
import se.hal.intf.HalHttpPage;
import se.hal.struct.Event;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;
import zutil.ui.Configurator;
import zutil.ui.Configurator.ConfigurationParam;

import java.util.Map;

public class EventConfigHttpPage extends HalHttpPage {
    private static final String TEMPLATE = "web-resource/event_config.tmpl";

    private class EventDataParams{
        public Class clazz;
        public ConfigurationParam[] params;
    }
    private EventDataParams[] eventConfigurations;


    public EventConfigHttpPage() {
        super("Configuration", "event_config");
        super.getRootNav().getSubNav("events").addSubNav(super.getNav());

        eventConfigurations = new EventDataParams[
                ControllerManager.getInstance().getAvailableEvents().size()];
        int i=0;
        for(Class c : ControllerManager.getInstance().getAvailableEvents()){
            eventConfigurations[i] = new EventDataParams();
            eventConfigurations[i].clazz = c;
            eventConfigurations[i].params = Configurator.getConfiguration(c);
            ++i;
        }
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
            int id = (request.containsKey("id") ? Integer.parseInt(request.get("id")) : -1);
            Event event;
            switch(request.get("action")) {
                // Local events
                case "create_local_event":
                    event = new Event();
                    event.setName(request.get("name"));
                    event.setType(request.get("type"));
                    event.setUser(localUser);
                    event.getDeviceConfig().setValues(request).applyConfiguration();
                    event.save(db);
                    ControllerManager.getInstance().register(event);
                    break;
                case "modify_local_event":
                    event = Event.getEvent(db, id);
                    if(event != null){
                        event.setName(request.get("name"));
                        event.setType(request.get("type"));
                        event.setUser(localUser);
                        event.getDeviceConfig().setValues(request).applyConfiguration();
                        event.save(db);
                    }
                    break;
                case "remove_local_event":
                    event = Event.getEvent(db, id);
                    if(event != null) {
                        ControllerManager.getInstance().deregister(event);
                        event.delete(db);
                    }
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
