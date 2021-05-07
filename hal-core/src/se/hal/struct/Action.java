package se.hal.struct;

import se.hal.intf.HalAction;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanObjectDSO;

import java.sql.SQLException;

/**
 * Defines a action that will be executed
 */
@DBBean.DBTable(value = "action", superBean = true)
public class Action extends DBBeanObjectDSO<HalAction>{


    public static Action getAction(DBConnection db, long id) throws SQLException {
        return DBBean.load(db, Action.class, id);
    }


    public Action() { }
    public Action(HalAction action) {
        this.setObject(action);
    }



    /**
     * Executes this specific action
     */
    public void execute(){
        if (getObject() != null)
            getObject().execute();
    }

}
