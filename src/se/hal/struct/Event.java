package se.hal.struct;

import se.hal.intf.HalEventController;
import se.hal.intf.HalEventData;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.log.LogUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-12-15.
 */
@DBBean.DBTable(value="event", superBean=true)
public class Event extends AbstractDevice<HalEventData>{
    private static final Logger logger = LogUtil.getLogger();


    public static Event getEvent(DBConnection db, long id) throws SQLException{
        return DBBean.load(db, Event.class, id);
    }

    public static List<Event> getLocalEvents(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement( "SELECT event.* FROM event,user WHERE user.external == 0 AND user.id == event.user_id" );
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Event.class, db) );
    }



    public Class<? extends HalEventController> getController(){
        return getDeviceConfig().getEventController();
    }
}
