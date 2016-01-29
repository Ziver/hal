#ifndef OREGONV21_H
#define OREGONV21_H

#include "Arduino.h"

void reset();
void parseOregonStream(bool level, uint8_t sampleCount);
int8_t getByte(bool level, uint8_t count);
int8_t getBit(bool level, uint8_t count);

#endif //OREGONV21_H