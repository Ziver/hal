#ifndef INTERRUPT_H
#define INTERRUPT_H

typedef void (*InterruptFunction) ();

class Interrupt
{
public:
    Interrupt(InterruptFunction callback) : callback(callback) {};

    //static void sleep(int milliseconds);
    void setupPinInterrupt(int pin);
    void setupTimerInterrupt(int milliseconds);
private:
    InterruptFunction callback;
}

#endif // INTERRUPT_H