package se.hal.intf;

import se.hal.struct.TriggerFlow;
import se.hal.struct.dso.TriggerDSO;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;

import java.sql.SQLException;
import java.util.List;

/**
 * A class that declares a trigger/condition that
 * needs to be validated before an action can be run
 */
public abstract class HalTrigger{
    private TriggerDSO dso;


    public static HalTrigger getTrigger(DBConnection db, long id) throws SQLException {
        TriggerDSO dso = DBBean.load(db, TriggerDSO.class, id);
        dso.getObject().dso = dso;
        return dso.getObject();
    }
    public static List<HalTrigger> getTriggers(DBConnection db, TriggerFlow flow) {
        return null;
    }


    public Long getId(){
        return (dso!=null ? dso.getId() : null);
    }

    public void save(DBConnection db) throws SQLException {
        if (dso == null)
            dso = new TriggerDSO();
        dso.setObject(this);
        dso.save(db);
    }

    public void delete(DBConnection db) throws SQLException {
        dso.delete(db);
    }



    /**
     * Evaluates if this trigger has passed. If the trigger is
     * true then this method will return true until the {@link #reset()}
     * method is called.
     */
    public abstract boolean evaluate();

    /**
     * Reset the evaluation to false.
     */
    public abstract void reset();

}
