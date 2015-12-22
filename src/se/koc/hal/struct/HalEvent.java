package se.koc.hal.struct;

import se.koc.hal.intf.HalEventController;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Ziver on 2015-12-15.
 */
@DBBean.DBTable("event")
public class HalEvent extends DBBean{
    // Event specific data
    private String name;
    private String type;
    private String config;

    // User configuration
    private User user;



    public static List<HalEvent> getEvents(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement( "SELECT * FROM event" );
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(HalEvent.class, db) );
    }



    public Class<? extends HalEventController> getController(){
        return null;
    }
}
