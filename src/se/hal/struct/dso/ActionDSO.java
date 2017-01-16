package se.hal.struct.dso;

import se.hal.intf.HalAction;
import zutil.db.bean.DBBean;


/**
 * A intermediate class for loading HalAction objects from DB
 */
@DBBean.DBTable(value = "action", superBean = true)
public class ActionDSO extends ObjectDSO<HalAction>{


}
