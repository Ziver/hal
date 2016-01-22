#ifndef RF_H
#define RF_H

#include "Arduino.h"

#define SILENCE_LENGTH  100 //the number of samples with "low" that represents a silent period between two signals

void parseRadioRXBuffer();
void sendTCodedData(uint8_t* data, uint8_t T_long, uint8_t* timings, uint8_t repeat, uint8_t pause);
void sendSCodedData(uint8_t* data, uint8_t pulseCount, uint8_t repeat, uint8_t pause);

extern volatile bool RFRX;

#endif  //RF_H
