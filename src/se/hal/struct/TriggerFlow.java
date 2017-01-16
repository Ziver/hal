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

    private String triggers; // only used for flat DB storage
    private transient ArrayList<HalTrigger> triggerList = new ArrayList<>();
    private String actions; // only used for flat DB storage
    private transient ArrayList<HalAction> actionList = new ArrayList<>();



    public static List<TriggerFlow> getTriggerFlows(DBConnection db) throws SQLException {
        PreparedStatement stmt = db.getPreparedStatement("SELECT * FROM trigger_flow");
        return DBConnection.exec(stmt, DBBeanSQLResultHandler.createList(TriggerFlow.class, db));
    }


    @Override
    public void save(DBConnection db) throws SQLException {
        DataNode root = new DataNode(DataNode.DataType.List);
        for (HalTrigger t : triggerList)
            root.add(t.getId());
        triggers = JSONWriter.toString(root);
        for (HalAction a : actionList)
            root.add(a.getId());
        actions = JSONWriter.toString(root);

        super.save(db);
    }

    @Override
    protected void postUpdateAction() {
        DBConnection db = HalContext.getDB();

        triggerList.clear();
        for (DataNode tId : JSONParser.read(triggers))
            try {
                triggerList.add(HalTrigger.getTrigger(db, tId.getInt()));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, null, e);
            }

        actionList.clear();
        for (DataNode aId : JSONParser.read(actions))
            try {
                actionList.add(HalAction.getAction(db, aId.getInt()));
            } catch (SQLException e) {
                logger.log(Level.SEVERE, null, e);
            }
    }


    public void addTrigger(HalTrigger trigger) {
        triggerList.add(trigger);
    }
    public void addAction(HalAction action) {
        actionList.add(action);
    }


    /**
     * @return true if any one of the triggerList evaluate to true,
     *         false if there are no triggerList added.
     *         Note: this method will not execute any actionList
     */
    public boolean evaluate(){
        for(HalTrigger trigger : triggerList){
            if (trigger.evaluate())
                return true;
        }
        return false;
    }

    /**
     * Executes the associated actionList in this flow
     */
    public void execute(){
        for(HalAction action : actionList){
            action.execute();
        }
    }

    /**
     * Resets all trigger evaluations
     */
    public void reset() {
        for(HalTrigger trigger : triggerList){
            trigger.reset();
        }
    }
}
