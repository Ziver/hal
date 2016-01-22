#ifndef ARCHTECH_H
#define ARCHTECH_H

#include "Arduino.h"

#define ARCHTECH_LOW_LOW     2      //a "low" is defined as at least this many "low" samples in a row
#define ARCHTECH_LOW_HIGH    7      //a "low" is defined as at most this many "low" samples in a row
#define ARCHTECH_HIGH_LOW   17      //a "high" is defined as at least this many "high" samples in a row
#define ARCHTECH_HIGH_HIGH  23      //a "high" is defined as at most this many "high" samples in a row

bool parseArctechSelfLearning(uint8_t* bufStartP, uint8_t* bufEndP);

#endif //ARCHTECH_H