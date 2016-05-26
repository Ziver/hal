#ifndef INTERRUPT_H
#define INTERRUPT_H


typedef void (*InterruptFunction) ();

class Interrupt
{
public:
    static void wakeUp() { wakeUpNow = true; };
    static void sleep();
    static void setupPinInterrupt(int pin);
    static void setupWatchDogInterrupt(unsigned int milliseconds);
    //static void setupTimerInterrupt(unsigned int milliseconds);

    static void setPinCallback(InterruptFunction callback){ Interrupt::pinCallback = callback;}
    static void setWatchDogCallback(InterruptFunction callback){ Interrupt::wdtCallback = callback;}

    /* Should not be called externally, used as triggering functions */
    static void handlePinInterrupt();
    static void handleWatchDogInterrupt();
private:
    static bool wakeUpNow;

    static InterruptFunction pinCallback;
    static InterruptFunction wdtCallback;

    static void setupWatchDogInterrupt();

    // Disable constructors and copy operators
    Interrupt() {};
    Interrupt(Interrupt const&);
    void operator=(Interrupt const&);

};


#endif // INTERRUPT_H