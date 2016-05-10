#ifndef INTERRUPT_H
#define INTERRUPT_H


typedef void (*InterruptFunction) ();

class Interrupt
{
public:
    static void wakeUp();
    static void sleep();
    static void setupPinInterrupt(int pin);
    static void setupWatchDogInterrupt(unsigned int milliseconds);
    //static void setupTimerInterrupt(unsigned int milliseconds);

    static void setCallback(InterruptFunction callback){ Interrupt::callback = callback;}
    static InterruptFunction getCallback(){ return Interrupt::callback;}

private:
    static InterruptFunction callback;
    static bool wakeUpNow;

    Interrupt() {};
    Interrupt(Interrupt const&);
    void operator=(Interrupt const&);
};


#endif // INTERRUPT_H