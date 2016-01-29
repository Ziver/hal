#ifndef RF_H
#define RF_H

#include "Arduino.h"

#define ACTIVATE_RADIO_RECEIVER()       (RFRX = true)
#define ACTIVATE_RADIO_TRANSMITTER()    (RFRX = false)
#define IS_RADIO_RECIEVER_ON()          (RFRX)
#define IS_RADIO_TRANSMITTER_ON()       (!RFRX)

extern volatile bool RFRX;

void parseRadioRXBuffer();
void sendTCodedData(uint8_t* data, uint8_t T_long, uint8_t* timings, uint8_t repeat, uint8_t pause);
void sendSCodedData(uint8_t* data, uint8_t pulseCount, uint8_t repeat, uint8_t pause);

#endif  //RF_H
