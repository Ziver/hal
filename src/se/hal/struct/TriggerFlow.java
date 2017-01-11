package se.hal.struct;

import se.hal.intf.HalAction;
import se.hal.intf.HalTrigger;
import zutil.db.bean.DBBean;

import java.util.ArrayList;

/**
 * A class that encapsulates triggers and their actions.
 * TODO: Bad class name, should be renamed when we come up with a better one
 */
public class TriggerFlow extends DBBean {
    private ArrayList<HalTrigger> triggers = new ArrayList<>();
    private ArrayList<HalAction>  actions = new ArrayList<>();


    public void addTrigger(HalTrigger trigger) {
        triggers.add(trigger);
    }
    public void addAction(HalAction action) {
        actions.add(action);
    }

    /**
     * @return true if any one of the triggers evaluate to true,
     *         false if there are no triggers added.
     *         Note: this method will not execute any actions
     */
    public boolean evaluate(){
        for(HalTrigger trigger : triggers){
            if (trigger.evaluate())
                return true;
        }
        return false;
    }

    /**
     * Executes the associated actions in this flow
     */
    public void execute(){
        for(HalAction action : actions){
            action.execute();
        }
    }

    /**
     * Resets all trigger evaluations
     */
    public void reset() {
        for(HalTrigger trigger : triggers){
            trigger.reset();
        }
    }
}
