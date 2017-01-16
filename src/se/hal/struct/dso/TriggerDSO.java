package se.hal.struct.dso;

import se.hal.intf.HalTrigger;
import zutil.db.bean.DBBean;


/**
 * A intermediate class for loading HalTrigger objects from DB
 */
@DBBean.DBTable(value = "trigger", superBean = true)
public class TriggerDSO extends ObjectDSO<HalTrigger>{


}
