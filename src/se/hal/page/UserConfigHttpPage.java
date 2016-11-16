package se.hal.page;

import se.hal.HalContext;
import se.hal.intf.HalHttpPage;
import se.hal.struct.User;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.net.http.HttpHeader;
import zutil.parser.Templator;

import java.util.Map;

public class UserConfigHttpPage extends HalHttpPage {
    private static final String TEMPLATE = "resource/web/user_config.tmpl";


    public UserConfigHttpPage() {
        super("user_profile");
        super.getUserNav().createSubNav(this.getId(), "Profile");
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
                    if (localUser == null) {
                        localUser = new User();
                        localUser.setExternal(false);
                    }
                    localUser.setUsername(request.get("username"));
                    localUser.setEmail(request.get("email"));
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
