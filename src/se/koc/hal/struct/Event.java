package se.koc.hal.struct;

import se.koc.hal.intf.HalEvent;
import se.koc.hal.intf.HalEventController;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.io.StringInputStream;
import zutil.io.StringOutputStream;
import zutil.log.LogUtil;
import zutil.parser.json.JSONObjectInputStream;
import zutil.parser.json.JSONObjectOutputStream;
import zutil.parser.json.JSONParser;
import zutil.parser.json.JSONWriter;

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
    private User user;



    public static List<Event> getEvents(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM event" );
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


    public Class<? extends HalEventController> getController(){
        return getEventData().getEventController();
    }
}
