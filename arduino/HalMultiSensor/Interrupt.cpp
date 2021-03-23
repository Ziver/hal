#include "Interrupt.h"
#include <avr/power.h>
#include <avr/sleep.h>
#include <avr/wdt.h>
#include "HalInterfaces.h"

void emptyFunc(){}
bool Interrupt::wakeUpNow = false;
InterruptFunction Interrupt::pinCallback = emptyFunc;
InterruptFunction Interrupt::wdtCallback = emptyFunc;


void Interrupt::handlePinInterrupt()        // the interrupt is handled here after wakeup
{
    (*Interrupt::pinCallback) ();
    //Interrupt::wakeUp();
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

    //power_adc_disable();
    //power_spi_disable();
    //power_usart0_disable();
    //power_timer0_disable();
    //power_timer1_disable();
    //power_timer2_disable();
    //power_twi_disable();
    //power_all_disable();

    while( ! Interrupt::wakeUpNow)
    {
        sleep_mode();           // here the device is actually put to sleep!!
                 // THE PROGRAM CONTINUES FROM HERE AFTER WAKING UP
    }
    sleep_disable();        // first thing after waking from sleep:
                            // disable sleep...

    //power_adc_enable();
    //power_spi_enable();
    //power_usart0_enable();
    //power_timer0_enable();
    //power_timer1_enable();
    //power_timer2_enable();
    //power_twi_enable();
    //power_all_enable();     // during normal running time.
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
    attachInterrupt((pin == PIND2 ? 0 : 1), Interrupt::handlePinInterrupt, RISING);

    //detachInterrupt(0);      // disables interrupt 0 on pin 2 so the
                             // wakeUpNow code will not be executed

    interrupts(); // enable all interrupts
}

//////////////////////////////////////////////////////////////////////////
// Watchdog timer
uint16_t wdtTime;
int32_t wdtTimeLeft;


void Interrupt::handleWatchDogInterrupt()
{
    wdt_disable();
    if (wdtTime <= 0)
        return;
    DEBUGF("WDT interrupt, time=%u, timeLeft=%ld", wdtTime, wdtTimeLeft);

    if (wdtTimeLeft <= 0)
    {
        Interrupt::wakeUp();
        (*Interrupt::wdtCallback) ();
        wdtTimeLeft = wdtTime;
    }

    setupWatchDogInterrupt();
}

ISR(WDT_vect)
{
    Interrupt::handleWatchDogInterrupt();
}

void Interrupt::setupWatchDogInterrupt(int32_t milliseconds)
{
    wdtTimeLeft = wdtTime = milliseconds;
    setupWatchDogInterrupt();
}

void Interrupt::setupWatchDogInterrupt()
{
    if (wdtTime <= 0){
        wdt_disable();
        return;
    }

    noInterrupts();

    unsigned short duration;

    if (8000 <= wdtTimeLeft){
        wdtTimeLeft -= 8000;
        duration = (1 << WDP3) | (1 << WDP0);
    } else if (4000 <= wdtTimeLeft){
       wdtTimeLeft -= 4000;
       duration = (1 << WDP3);
    } else if (2000 <= wdtTimeLeft){
       wdtTimeLeft -= 2000;
       duration = (1 << WDP2) | (1 << WDP1) | (1 << WDP0);
    } else if (1000 <= wdtTimeLeft){
       wdtTimeLeft -= 1000;
       duration = (1 << WDP2) | (1 << WDP1);
    } else if (500 <= wdtTimeLeft){
       wdtTimeLeft -= 500;
       duration = (1 << WDP2) | (1 << WDP0);
    } else if (256 <= wdtTimeLeft){
       wdtTimeLeft -= 256;
       duration = (1 << WDP2);
    } else if (128 <= wdtTimeLeft){
       wdtTimeLeft -= 128;
       duration = (1 << WDP1) | (1 << WDP0);
    } else if (64 <= wdtTimeLeft){
       wdtTimeLeft -= 64;
       duration = (1 << WDP1);
    } else if (32 <= wdtTimeLeft){
       wdtTimeLeft -= 32;
       duration = (1 << WDP0);
    } else { //(16 <= wdtTimeLeft){
       wdtTimeLeft -= 16;
       duration = 0;
    }

    wdt_reset();
    MCUSR &= ~(1 << WDRF);  // reset status flag

    /* WDCE = Watchdog Change Enable
     *
     * WDTON(1)  WDE  WDIE  Mode
     * 1         0    0     Stopped
     * 1         0    1     Interrupt
     * 1         1    0     Reset
     * 1         1    1     Interrupt first, reset on second trigger
     * 0         x    x     Reset
     */
    WDTCSR = (1 << WDCE) | (1<<WDE); // enable configuration
    /* WDP3 WDP2 WDP1 WDP0  Number of cycles  Typical Time-out time (VCC = 5.0V)
     * 0    0    0    0     2K (2048)         16 ms
     * 0    0    0    1     4K (4096)         32 ms
     * 0    0    1    0     8K (8192)         64 ms
     * 0    0    1    1     16K (16384)       0.125 s
     * 0    1    0    0     32K (32768)       0.25 s
     * 0    1    0    1     64K (65536)       0.5 s
     * 0    1    1    0     128K (131072)     1.0 s
     * 0    1    1    1     256K (262144)     2.0 s
     * 1    0    0    0     512K (524288)     4.0 s
     * 1    0    0    1     1024K (1048576)   8.0 s
     */
    WDTCSR = (1 << WDIE) | duration;
    //WDTCSR = (1 << WDIE) | (1 << WDP3) | (1 << WDP0);

    interrupts();
}


//////////////////////////////////////////////////////////////////////////
// Timer 1
/*
ISR(Timer1_COMPA_vect) // timer compare interrupt service routine
{
    //DEBUG("Timer1e Interrupt");
    __pinInterruptHandler__();
}
*/
/*
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
/*    TCCR1B |= (1 << CS12); // 256 prescaler
    TCNT1 = 34286; // preload timer 65536-16MHz/256/2Hz
    //TODO: TIMSK1 |= (1 << TOIE1); // enable timer overflow interrupt

    interrupts(); // enable all interrupts
}
*/
