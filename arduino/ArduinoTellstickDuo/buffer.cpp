#include "buffer.h"

uint8_t RF_rxBuffer[512];   //must have and even number of elements
uint8_t* RF_rxBufferStartP = &RF_rxBuffer[0];
uint8_t* RF_rxBufferEndP = &RF_rxBuffer[511];
volatile uint8_t* bufferWriteP = RF_rxBufferStartP;

uint16_t calculateBufferPointerDistance(uint8_t* bufStartP, uint8_t* bufEndP) {
  if (bufStartP <= bufEndP) {
    return bufEndP - bufStartP + 1;
  } else {
    return (RF_rxBufferEndP - bufStartP) + (bufEndP - RF_rxBufferStartP) + 2;
  }
};   //end calculateBufferPointerDistance

uint8_t* getNextBufferPointer(uint8_t* p) {
  if ( p + 1 > RF_rxBufferEndP) {
    return RF_rxBufferStartP;
  } else {
    return p + 1;
  }
};  //end getNextBufferPointer

void stepBufferPointer(uint8_t** p) {
  *p = getNextBufferPointer(*p);
};  //end stepBufferPointer
