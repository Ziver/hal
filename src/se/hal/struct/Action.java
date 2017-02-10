package se.hal.struct;

import se.hal.intf.HalAction;
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
