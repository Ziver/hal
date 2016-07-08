#ifndef INTERRUPT_H
#define INTERRUPT_H

#include <Arduino.h>

typedef void (*InterruptFunction) ();

class Interrupt
{
public:
    static void wakeUp() { wakeUpNow = true; };
    static void sleep();
    static void setupPinInterrupt(int pin);
    static void setupWatchDogInterrupt(int32_t milliseconds);
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