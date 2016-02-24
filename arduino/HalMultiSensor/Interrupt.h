#ifndef INTERRUPT_H
#define INTERRUPT_H

#include <Arduino.h>


typedef void (*InterruptFunction) ();

class Interrupt
{
public:
    Interrupt(InterruptFunction callback) : callback(callback) {};

    //static void sleep(int milliseconds);
    void setupPinInterrupt(int pin);
    void setupTimerInterrupt(unsigned int milliseconds);
private:
    InterruptFunction callback;

};

#endif // INTERRUPT_H