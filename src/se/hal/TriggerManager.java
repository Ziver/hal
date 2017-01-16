package se.hal;

import se.hal.intf.HalAction;
import se.hal.intf.HalTrigger;
import se.hal.struct.TriggerFlow;
import zutil.log.LogUtil;
import zutil.plugin.PluginManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Handles all triggers registered on Hal
 */
public class TriggerManager {
    private static final Logger logger = LogUtil.getLogger();
    private static final long EVALUATION_INTERVAL = 5 * 1000;
    private static TriggerManager instance;

    private ArrayList<Class<? extends HalTrigger>> availableTriggers = new ArrayList<>();
    private ArrayList<Class<? extends HalAction>>  availableActions  = new ArrayList<>();

    private ArrayList<TriggerFlow> triggerFlows = new ArrayList<>();
    private ScheduledExecutorService executor;



    public void setEvaluationInterval(long interval) {
        if (executor != null)
            executor.shutdownNow();
        executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                evaluateAndExecute();
            }
        }, 0, interval, TimeUnit.MILLISECONDS);
    }


    public void addAvailableTrigger(Class<? extends HalTrigger> clazz) {
        if ( ! availableTriggers.contains(clazz))
            availableTriggers.add(clazz);
    }
    public List<Class<? extends HalTrigger>> getAvailableTriggers() {
        return availableTriggers;
    }

    public void addAvailableAction(Class<? extends HalAction> clazz) {
        if ( ! availableActions.contains(clazz))
            availableActions.add(clazz);
    }
    public List<Class<? extends HalAction>> getAvailableActions() {
        return availableActions;
    }

    public void register(TriggerFlow flow){
        if ( ! triggerFlows.contains(flow))
            triggerFlows.add(flow);
    }


    /**
     * Main execution method.
     * This method will go through all flows and evaluate them. If the
     * evaluation of a trigger returns true then its execute method will be called.
     */
    public synchronized void evaluateAndExecute() {
        for (int i = 0; i < triggerFlows.size(); i++) { // avoid foreach as triggerFlow can change while we are running
            TriggerFlow flow = triggerFlows.get(i);
            if (flow.evaluate()) {
                logger.fine("Flow "+ flow.getId() +" evaluated true, executing actions");
                flow.execute();
                flow.reset();
            }
        }
    }


    public static void initialize(PluginManager pluginManager) {
        TriggerManager manager = new TriggerManager();

        for (Iterator<Class<? extends HalTrigger>> it = pluginManager.getClassIterator(HalTrigger.class);
             it.hasNext(); ) {
            manager.addAvailableTrigger(it.next());
        }

        for (Iterator<Class<? extends HalAction>> it = pluginManager.getClassIterator(HalAction.class);
             it.hasNext(); ) {
            manager.addAvailableAction(it.next());
        }

        manager.setEvaluationInterval(EVALUATION_INTERVAL);
        instance = manager;
    }

    public static TriggerManager getInstance(){
        return instance;
    }
}
