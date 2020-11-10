package se.hal.intf;


/**
 * A interface that declares a trigger/condition that
 * needs to be validated before an action can be run
 */
public interface HalTrigger{

    /**
     * Evaluates if this trigger has passed. If the trigger is
     * true then this method will return true until the {@link #reset()}
     * method is called.
     */
    boolean evaluate();

    /**
     * Reset the evaluation to false.
     */
    void reset();

}
