package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalHttpPage;
import se.hal.struct.Sensor;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.parser.Templator;
import zutil.ui.Configurator;
import zutil.ui.Configurator.ConfigurationParam;

import java.util.Map;

public class UserConfigHttpPage extends HalHttpPage {
    private static final String TEMPLATE = "web-resource/user_config.tmpl";


    public UserConfigHttpPage() {
        super("Profile", "user_profile");
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
            User user;
            switch(request.get("action")) {
                // Local User
                case "modify_local_user":
                    localUser.setUserName(request.get("username"));
                    localUser.setAddress(request.get("address"));
                    localUser.save(db);
                    break;
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("user", localUser);

        return tmpl;

    }

}
