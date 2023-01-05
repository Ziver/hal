package se.hal.struct;

import zutil.db.DBConnection;
import zutil.db.bean.DBBean;
import zutil.db.bean.DBBeanSQLResultHandler;
import zutil.db.handler.SimpleSQLResult;
import zutil.log.LogUtil;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * A class that encapsulates triggerList and their actionList.
 * TODO: Bad class name, should be renamed when we come up with a better one
 */
@DBBean.DBTable("trigger_flow")
public class TriggerFlow extends DBBean {
    private static final Logger logger = LogUtil.getLogger();

    private boolean enabled = true;
    private String name = "";

    @DBLinkTable(beanClass=Trigger.class, table="trigger", idColumn = "flow_id")
    private List<Trigger> triggerList = new ArrayList<>();
    @DBLinkTable(beanClass=Action.class, table="action", idColumn = "flow_id")
    private List<Action> actionList = new ArrayList<>();


    public static List<TriggerFlow> getTriggerFlows(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement("SELECT * FROM trigger_flow");
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(TriggerFlow.class, db));
    }
    public static TriggerFlow getTriggerFlow(DBConnection db, int id) throws SQLException {
        return DBBean.load(db, TriggerFlow.class, id);
    }
    /**
     * Looks up the parent TriggerFlow for the specified Trigger
     */
    public static TriggerFlow getTriggerFlow(DBConnection db, Trigger trigger) throws SQLException {
        return getParentFlow(db, "trigger", trigger);
    }
    /**
     * Looks up the parent TriggerFlow for the specified Action
     */
    public static TriggerFlow getTriggerFlow(DBConnection db, Action action) throws SQLException {
        return getParentFlow(db, "action", action);
    }
    private static TriggerFlow getParentFlow(DBConnection db, String table, DBBean subObj) throws SQLException {
        if (subObj.getId() == null)
            return null;
        Integer flowId = db.exec("SELECT flow_id FROM "+table+" WHERE id=="+subObj.getId(),
                new SimpleSQLResult<Integer>());
        if (flowId == null)
            return null;
        return TriggerFlow.getTriggerFlow(db, flowId);
    }


    public boolean isEnabled() {
        return enabled;
    }
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }


    public void addTrigger(Trigger trigger) {
        triggerList.add(trigger);
    }
    public List<Trigger> getTriggers() {
        return triggerList;
    }
    public void removeTrigger(Trigger trigger) {
        triggerList.remove(trigger);
    }

    public void addAction(Action action) {
        actionList.add(action);
    }
    public List<Action> getActions() {
        return actionList;
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
        if (triggerList.isEmpty() || !enabled)
            return false;
        for (Trigger trigger : triggerList){
            if (!trigger.evaluate())
                return false;
        }
        return true;
    }

    /**
     * Executes the associated actionList in this flow
     */
    public void execute(){
        if (!enabled)
            return;

        logger.info("Executing flow(id: " + getId() + "): " + getName());

        for (Action action : actionList){
            action.execute();
        }
    }

    /**
     * Resets all trigger evaluations
     */
    public void reset() {
        for (Trigger trigger : triggerList){
            trigger.reset();
        }
    }
}
