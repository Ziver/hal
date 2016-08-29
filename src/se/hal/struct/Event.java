package se.hal.struct;

import se.hal.intf.HalDeviceData;
import se.hal.intf.HalEventController;
import se.hal.intf.HalEventConfig;
import se.hal.intf.HalEventData;
import se.hal.util.DeviceDataSqlResult;
import zutil.db.DBConnection;
import zutil.db.SQLResultHandler;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.db.handler.SimpleSQLResult;
import zutil.log.LogUtil;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-12-15.
 */
@DBBean.DBTable(value="event", superBean=true)
public class Event extends AbstractDevice<HalEventConfig,HalEventData>{
    private static final Logger logger = LogUtil.getLogger();


    public static Event getEvent(DBConnection db, long id) throws SQLException{
        return DBBean.load(db, Event.class, id);
    }

    public static List<Event> getLocalEvents(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement(
                "SELECT event.* FROM event,user WHERE user.external == 0 AND user.id == event.user_id");
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Event.class, db));
    }




    public Class<? extends HalEventController> getController(){
        return getDeviceConfig().getEventControllerClass();
    }

    @Override
    protected HalEventData getLatestDeviceData(DBConnection db) {
        try {
            Class deviceDataClass = getDeviceConfig().getEventDataClass();
            if (deviceDataClass == null)
                throw new ClassNotFoundException("Unknown event data class for: " + getDeviceConfig().getClass());

            if (getId() != null) {
                PreparedStatement stmt = db.getPreparedStatement(
                        "SELECT data FROM event_data_raw WHERE event_id == ? ORDER BY timestamp DESC LIMIT 1");
                stmt.setLong(1, getId());
                return (HalEventData)
                        DBConnection.exec(stmt, new DeviceDataSqlResult(deviceDataClass));
            }
        } catch (Exception e){
            logger.log(Level.WARNING, null, e);
        }
        return null;
    }
}
