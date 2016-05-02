#include "Interrupt.h"
#include <Arduino.h>
#include <avr/power.h>
#include <avr/sleep.h>
#include <avr/wdt.h>


void emptyFunc(){}
InterruptFunction Interrupt::callback = emptyFunc;
bool Interrupt::wakeUpNow = false;


void __interruptHandler__()        // the interrupt is handled here after wakeup
{
  // execute code here after wake-up before returning to the loop() function
  // timers and code using timers (serial.print and more...) will not work here.

    noInterrupts(); // disable all interrupts
    (Interrupt::getCallback()) ();
    interrupts(); // enable all interrupts
}

ISR(Timer1_COMPA_vect) // timer compare interrupt service routine
{
    __interruptHandler__();
}

ISR(WDT_vect) { }


void Interrupt::wakeUp()
{
    wakeUpNow = true;
}

void Interrupt::sleep()
{
    /*
     * The 5 different modes are:
     *     SLEEP_MODE_IDLE         -the least power savings
     *     SLEEP_MODE_ADC
     *     SLEEP_MODE_PWR_SAVE
     *     SLEEP_MODE_STANDBY
     *     SLEEP_MODE_PWR_DOWN     -the most power savings
     *
     * For now, we want as much power savings as possible, so we
     * choose the according
     * sleep mode: SLEEP_MODE_PWR_DOWN
     */
    set_sleep_mode(SLEEP_MODE_PWR_DOWN);   // sleep mode is set here
    wakeUpNow = false;

    sleep_enable();         // enables the sleep bit in the mcucr register
                            // so sleep is possible. just a safety pin
    /*
    power_adc_disable();
    power_spi_disable();
    power_timer0_disable();
    power_timer1_disable();
    power_timer2_disable();
    power_twi_disable();
    */
    while( ! Interrupt::wakeUpNow)
    {
        sleep_mode();           // here the device is actually put to sleep!!
                 // THE PROGRAM CONTINUES FROM HERE AFTER WAKING UP
    }
    sleep_disable();        // first thing after waking from sleep:
                            // disable sleep...

    power_all_enable();     // during normal running time.
}

void Interrupt::setupPinInterrupt(int pin)
{
    noInterrupts(); // disable all interrupts

    /* Now it is time to enable an interrupt.
     * In the function call attachInterrupt(A, B, C)
     * A   can be either 0 or 1 for interrupts on pin 2 or 3.
     * B   Name of a function you want to execute at interrupt for A.
     * C   Trigger mode of the interrupt pin. can be:
     *             LOW        a low level triggers
     *             CHANGE     a change in level triggers
     *             RISING     a rising edge of a level triggers
     *             FALLING    a falling edge of a level triggers
     *
     * In all but the IDLE sleep modes only LOW can be used.
     */
    attachInterrupt(pin, __interruptHandler__, LOW);

    //detachInterrupt(0);      // disables interrupt 0 on pin 2 so the
                             // wakeUpNow code will not be executed

    interrupts(); // enable all interrupts
}

void Interrupt::setupTimerInterrupt(unsigned int milliseconds)
{
    noInterrupts(); // disable all interrupts

    // initialize Timer1
    TCCR1A = 0;
    TCCR1B = 0;

    /* Clock Select Bit Description
     *  CS12    CS11    CS10    Description
     *  0       0       0       No clock source (Stop timer)
     *  0       0       1       clk/1 (No prescaling)
     *  0       1       0       clk/8
     *  0       1       1       clk/64
     *  1       0       0       clk/256
     *  1       0       1       clk/1024
     *  1       1       0       External clock source on T1 pin. Clock on falling edge.
     *  1       1       1       External clock source on T1 pin. Clock on rising edge.
     */
    TCCR1B |= (1 << CS12); // 256 prescaler
    TCNT1 = 34286; // preload timer 65536-16MHz/256/2Hz
    TIMSK1 |= (1 << TOIE1); // enable timer overflow interrupt

    interrupts(); // enable all interrupts
}

