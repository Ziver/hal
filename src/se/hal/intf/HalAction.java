package se.hal.intf;

import se.hal.struct.TriggerFlow;
import se.hal.struct.dso.ActionDSO;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;

import java.sql.SQLException;
import java.util.List;

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
    public static List<HalAction> getActions(DBConnection db, TriggerFlow flow) {
        // TODO:
        return null;
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

    public void delete(DBConnection db) throws SQLException {
        dso.delete(db);
    }


    /**
     * Executes this specific action
     */
    public abstract void execute();
}
