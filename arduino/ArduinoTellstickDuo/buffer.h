#ifndef BUFFER_H
#define BUFFER_H

#include "Arduino.h"

extern uint8_t RF_rxBuffer[512];   //must have and even number of elements
extern uint8_t* RF_rxBufferStartP;
extern uint8_t* RF_rxBufferEndP;
extern volatile uint8_t* bufferWriteP;

inline uint16_t calculateBufferPointerDistance(uint8_t* bufStartP, uint8_t* bufEndP) {
  if (bufStartP <= bufEndP) {
    return bufEndP - bufStartP + 1;
  } else {
    return (RF_rxBufferEndP - bufStartP) + (bufEndP - RF_rxBufferStartP) + 2;
  }
};   //end calculateBufferPointerDistance

inline uint8_t* getNextBufferPointer(uint8_t* p) {
  if ( p + 1 > RF_rxBufferEndP) {
    return RF_rxBufferStartP;
  } else {
    return p + 1;
  }
};  //end getNextBufferPointer

inline void stepBufferPointer(uint8_t** p) {
  *p = getNextBufferPointer(*p);
};  //end stepBufferPointer

#endif //BUFFER_H
