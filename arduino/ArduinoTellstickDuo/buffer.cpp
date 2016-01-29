#include "buffer.h"

uint8_t RF_rxBuffer[512];   //must have and even number of elements
uint8_t* RF_rxBufferStartP = &RF_rxBuffer[0];
uint8_t* RF_rxBufferEndP = &RF_rxBuffer[511];
volatile uint8_t* bufferWriteP = RF_rxBufferStartP;
