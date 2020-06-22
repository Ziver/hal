package se.hal.page;

import se.hal.ControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalHttpPage;
import se.hal.struct.Event;
import se.hal.struct.devicedata.SwitchEventData;
import se.hal.util.DeviceNameComparator;
import se.hal.util.HistoryDataListSqlResult;
import se.hal.util.HistoryDataListSqlResult.HistoryData;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;

import java.sql.PreparedStatement;
import java.util.*;
import java.util.logging.Logger;

public class PropertySettingsPage extends HalHttpPage {
    private static final String TEMPLATE = "resource/web/properties_config.tmpl";


    public PropertySettingsPage(){
        super("properties");
        super.getRootNav().createSubNav("Settings").setWeight(100).createSubNav(this.getId(), "Properties");
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

        DBConnection db = HalContext.getDB();

        HashMap properties = HalContext.getProperties();

        Templator tmpl = new Templator(FileUtil.find(TEMPLATE));
        tmpl.set("properties", properties.entrySet());
        return tmpl;

    }
}
