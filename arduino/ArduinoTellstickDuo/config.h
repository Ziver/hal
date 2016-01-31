#ifndef CONFIG_H
#define CONFIG_H

#include "Arduino.h"

/*
* RX PIN = 7
* TX PIN = 8
*/
inline void setupPins(){
    pinMode(7, INPUT); 
    pinMode(8, OUTPUT);
};

#if defined(__AVR_ATmega168__) || defined(__AVR_ATmega168P__) || defined(__AVR_ATmega328P__)
    //pin7 = PD7 = port D, bit 8
    #define     RX_PIN_READ()        ( digitalRead(7) )    // optimized "digitalRead(7)"
    //pin8 = PB0 = port B, bit 1
    #define     TX_PIN_LOW()         ( PORTB &= 0b01111111 )    // optimized "digitalWrite(8, LOW)"
    #define     TX_PIN_HIGH()        ( PORTB |= 0b10000000 )    // optimized "digitalWrite(8, HIGH)"
#else
    #unsupported architecture
#endif

#endif //CONFIG_H
