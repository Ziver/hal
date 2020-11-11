package se.hal.page;

import se.hal.HalContext;
import se.hal.intf.HalWebPage;
import se.hal.page.HalAlertManager.AlertLevel;
import se.hal.page.HalAlertManager.AlertTTL;
import se.hal.page.HalAlertManager.HalAlert;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;

import java.util.Map;
import java.util.logging.Logger;

public class UserConfigWebPage extends HalWebPage {
    private static final Logger logger = LogUtil.getLogger();
    private static final String TEMPLATE = HalContext.RESOURCE_WEB_ROOT + "/user_config.tmpl";


    public UserConfigWebPage() {
        super("user_profile");
        super.getUserNav().createSubNav(this.getId(), "Profile");
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception {

        DBConnection db = HalContext.getDB();
        User localUser = User.getLocalUser(db);

        // Save new input
        if (request.containsKey("action")) {
            User user;
            switch(request.get("action")) {
                // Local User
                case "modify_local_user":
                    if (localUser == null) {
                        localUser = new User();
                        localUser.setExternal(false);
                    }
                    logger.info("Modifying user: " + localUser.getUsername());
                    localUser.setUsername(request.get("username"));
                    localUser.setEmail(request.get("email"));
                    localUser.setAddress(request.get("address"));
                    localUser.save(db);

                    HalAlertManager.getInstance().addAlert(new HalAlert(
                            AlertLevel.SUCCESS, "Successfully saved profile changes", AlertTTL.ONE_VIEW));
                    break;
            }
        }

        // Output
        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("user", localUser);

        return tmpl;

    }

}
