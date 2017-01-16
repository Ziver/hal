package se.hal.intf;

import se.hal.struct.dso.ActionDSO;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;

import java.sql.SQLException;

/**
 * Defines a action that will be executed
 */
public abstract class HalAction{
    private ActionDSO dso;


    public static HalAction getAction(DBConnection db, long id) throws SQLException {
        ActionDSO dso = DBBean.load(db, ActionDSO.class, id);
        dso.getObject().dso = dso;
        return dso.getObject();
    }


    public Long getId(){
        return (dso!=null ? dso.getId() : null);
    }

    public void save(DBConnection db) throws SQLException {
        if (dso == null)
            dso = new ActionDSO();
        dso.setObject(this);
        dso.save(db);
    }



    /**
     * Executes this specific action
     */
    public abstract void execute();
}
