package se.hal;

import org.junit.Test;
import se.hal.intf.HalAction;
import se.hal.intf.HalTrigger;
import se.hal.struct.TriggerFlow;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 *
 */
public class TriggerManagerTest {

    private TriggerManager manager = new TriggerManager();


    @Test
    public void registerAvailableTrigger(){
        assertEquals(Collections.EMPTY_LIST, manager.getAvailableTriggers());

        manager.addAvailableTrigger(TestTrigger.class);
        manager.addAvailableTrigger(TestTrigger.class);
        assertEquals(1, manager.getAvailableTriggers().size());
        assertTrue(manager.getAvailableTriggers().contains(TestTrigger.class));
    }

    @Test
    public void registerAvailableAction(){
        assertEquals(Collections.EMPTY_LIST, manager.getAvailableActions());

        manager.addAvailableAction(TestAction.class);
        manager.addAvailableAction(TestAction.class);
        assertEquals(1, manager.getAvailableActions().size());
        assertTrue(manager.getAvailableActions().contains(TestAction.class));
    }


    @Test
    public void register(){
        registerAvailableTrigger();

        TriggerFlow flow = new TriggerFlow();
        flow.addTrigger(new TestTrigger(true));
        TestAction action = new TestAction();
        flow.addAction(action);
        manager.register(flow);
        manager.evaluateAndExecute();
        assertEquals(1, action.nrOfExecutions);
    }


    @Test
    public void evaluateAndExecute(){
        registerAvailableTrigger();

        TriggerFlow flow = new TriggerFlow();
        TestTrigger trigger = new TestTrigger(true);
        flow.addTrigger(trigger);
        TestAction action = new TestAction();
        flow.addAction(action);
        manager.register(flow);

        manager.evaluateAndExecute();
        assertEquals("Action executed nr of times",
                1, action.nrOfExecutions);

        manager.evaluateAndExecute();
        assertEquals("Action executed nr of times",
                1, action.nrOfExecutions);

    }

    /////////////////////////////////////////////////////////////////////////////

    private static class TestTrigger implements HalTrigger {
        boolean evaluation;
        TestTrigger(boolean b){ evaluation = b; }
        @Override
        public boolean evaluate() { return evaluation; }

        @Override
        public void reset() { evaluation = false; }
    }


    private class TestAction implements HalAction {
        int nrOfExecutions;
        @Override
        public void execute() { nrOfExecutions++; }
    }

}