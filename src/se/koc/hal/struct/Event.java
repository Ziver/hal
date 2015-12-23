package se.koc.hal.struct;

import se.koc.hal.intf.HalEvent;
import se.koc.hal.intf.HalEventController;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.log.LogUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Ziver on 2015-12-15.
 */
@DBBean.DBTable("event")
public class Event extends DBBean{
    private static final Logger logger = LogUtil.getLogger();

    // Event specific data
    private String name;
    private String type;
    private String config;
    // Event specific data
    private transient HalEvent eventData;

    // User configuration
    private User user;



    public static List<Event> getEvents(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM event" );
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Event.class, db) );
    }

    private HalEvent getEventData(){
        if(eventData == null) {
            try {
                Class c = Class.forName(type);
                eventData = (HalEvent) c.newInstance();
            } catch (Exception e){
                logger.log(Level.SEVERE, null, e);
            }
        }
        return eventData;
    }


    public Class<? extends HalEventController> getController(){
        return getEventData().getController();
    }
}
