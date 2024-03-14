package se.hal.page;

import se.hal.EventControllerManager;
import se.hal.HalContext;
import se.hal.intf.HalEventData;
import se.hal.intf.HalWebPage;
import se.hal.struct.Event;
import se.hal.struct.devicedata.ColorEventData;
import se.hal.struct.devicedata.LevelEventData;
import se.hal.struct.devicedata.OnOffEventData;
import se.hal.util.DeviceNameComparator;
import se.hal.util.HistoryDataListSqlResult;
import se.hal.util.HistoryDataListSqlResult.HistoryData;
import zutil.ObjectUtil;
import zutil.db.DBConnection;
import zutil.io.file.FileUtil;
import zutil.log.LogUtil;
import zutil.parser.Templator;

import java.sql.PreparedStatement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * A empty page with an empty body content generated from server side.
 */
public class EmptyWebPage extends HalWebPage {

    public EmptyWebPage() {
        super("empty");
    }

    @Override
    public Templator httpRespond(
            Map<String, Object> session,
            Map<String, String> cookie,
            Map<String, String> request)
            throws Exception{

            return null;
    }
}
