#ifndef USART_H
#define USART_H

#include "Arduino.h"

void parseSerialForCommand();
bool parseRxBuffer(byte* buffer, uint8_t startIndex, uint8_t endIndex, bool debug, uint8_t repeat, uint8_t pause);
bool handleSCommand(byte* buffer, uint8_t startIndex, uint8_t endIndex, bool debug, uint8_t repeat, uint8_t pause);
bool handleTCommand(byte* buffer, uint8_t startIndex, uint8_t endIndex, bool debug, uint8_t repeat, uint8_t pause);

extern uint8_t Serial_rxBuffer[79];

#endif  //USART_H
