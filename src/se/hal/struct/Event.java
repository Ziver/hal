package se.hal.struct;

import se.hal.intf.HalEvent;
import se.hal.intf.HalEventController;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.io.StringInputStream;
import zutil.io.StringOutputStream;
import zutil.log.LogUtil;
import zutil.parser.json.JSONObjectInputStream;
import zutil.parser.json.JSONObjectOutputStream;

import java.io.IOException;
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
    @DBColumn("user_id")
    private User user;


    public static Event getEvent(DBConnection db, long id) throws SQLException{
        return DBBean.load(db, Event.class, id);
    }

    public static List<Event> getLocalEvents(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement( "SELECT event.* FROM event,user WHERE user.external == 0 AND user.id == event.user_id" );
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(Event.class, db) );
    }


    public void setEventData(HalEvent eventData){
        this.eventData = eventData;
    }
    public HalEvent getEventData(){
        if(eventData == null) {
            try {
                Class c = Class.forName(type);

                JSONObjectInputStream in = new JSONObjectInputStream(
                        new StringInputStream(config));
                eventData = (HalEvent) in.readObject(c);
                in.close();
            } catch (Exception e){
                logger.log(Level.SEVERE, "Unable to read event data", e);
            }
        }
        return eventData;
    }
    public void save(DBConnection db) throws SQLException {
        if(eventData != null) {
            try {
                StringOutputStream buff = new StringOutputStream();
                JSONObjectOutputStream out = new JSONObjectOutputStream(buff);
                out.enableMetaData(false);
                out.writeObject(eventData);
                out.close();
                this.config = buff.toString();
            } catch (IOException e){
                logger.log(Level.SEVERE, "Unable to save event data", e);
            }
        }
        else
            this.config = null;
        super.save(db);
    }


    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getConfig() {
        return config;
    }
    public void setConfig(String config) {
        this.config = config;
        this.eventData = null; // invalidate current sensor data object
    }

    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }


    public Class<? extends HalEventController> getController(){
        return getEventData().getEventController();
    }
}
