package se.hal.struct;

import se.hal.HalContext;
import se.hal.intf.HalAction;
import se.hal.intf.HalTrigger;
import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.log.LogUtil;
import zutil.parser.DataNode;
import zutil.parser.json.JSONParser;
import zutil.parser.json.JSONWriter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A class that encapsulates triggerList and their actionList.
 * TODO: Bad class name, should be renamed when we come up with a better one
 */
@DBBean.DBTable("trigger_flow")
public class TriggerFlow extends DBBean {
    private static final Logger logger = LogUtil.getLogger();

    private transient List<Trigger> triggerList = new ArrayList<>();
    private transient List<Action> actionList = new ArrayList<>();



    public static List<TriggerFlow> getTriggerFlows(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement("SELECT * FROM trigger_flow");
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(TriggerFlow.class, db));
    }
    public static TriggerFlow getTriggerFlow(DBConnection db, int id) throws SQLException {
        return DBBean.load(db, TriggerFlow.class, id);
    }



    @Override
    protected void postUpdateAction() {
        DBConnection db = HalContext.getDB();

        triggerList.clear();
        triggerList = Trigger.getTriggers(db, this);

        actionList.clear();
        actionList = Action.getActions(db, this);
    }


    public void addTrigger(Trigger trigger) {
        triggerList.add(trigger);
    }
    public void removeTrigger(Trigger trigger) {
        triggerList.remove(trigger);
    }

    public void addAction(Action action) {
        actionList.add(action);
    }
    public void removeAction(Action action) {
        actionList.remove(action);
    }

    /**
     * @return true if any one of the triggerList evaluate to true,
     *         false if there are no triggerList added.
     *         Note: this method will not execute any actionList
     */
    public boolean evaluate(){
        for(Trigger trigger : triggerList){
            if (trigger.evaluate())
                return true;
        }
        return false;
    }

    /**
     * Executes the associated actionList in this flow
     */
    public void execute(){
        for(Action action : actionList){
            action.execute();
        }
    }

    /**
     * Resets all trigger evaluations
     */
    public void reset() {
        for(Trigger trigger : triggerList){
            trigger.reset();
        }
    }


}
