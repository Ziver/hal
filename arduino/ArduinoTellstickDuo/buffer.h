#ifndef BUFFER_H
#define BUFFER_H

#include "Arduino.h"

uint16_t calculateBufferPointerDistance(uint8_t* bufStartP, uint8_t* bufEndP);
uint8_t* getNextBufferPointer(uint8_t* p);
void stepBufferPointer(uint8_t** p);

extern uint8_t RF_rxBuffer[512];   //must have and even number of elements
extern uint8_t* RF_rxBufferStartP;
extern uint8_t* RF_rxBufferEndP;
extern volatile uint8_t* bufferWriteP;

#endif //BUFFER_H
