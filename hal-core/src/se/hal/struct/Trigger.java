package se.hal.struct;

import se.hal.intf.HalTrigger;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanObjectDSO;
import zutil.db.bean.DBBeanSQLResultHandler;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * A class that declares a trigger/condition that
 * needs to be validated before an action can be run
 */
@DBBean.DBTable(value = "trigger", superBean = true)
public class Trigger extends DBBeanObjectDSO<HalTrigger>{



    public static Trigger getTrigger(DBConnection db, long id) throws SQLException {
        return DBBean.load(db, Trigger.class, id);
    }



    public Trigger() { }
    public Trigger(HalTrigger trigger) {
        this.setObject(trigger);
    }


    /**
     * Evaluates if this trigger has passed. If the trigger is
     * true then this method will return true until the {@link #reset()}
     * method is called.
     */
    public boolean evaluate(){
        if (getObject() != null)
            return getObject().evaluate();
        return false;
    }

    /**
     * Reset the evaluation to false.
     */
    public void reset(){
        if (getObject() != null)
            getObject().reset();
    }

}
