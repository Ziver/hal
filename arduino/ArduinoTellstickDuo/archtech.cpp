#include "archtech.h"
#include "buffer.h"

bool parseArctechSelfLearning(uint8_t* bufStartP, uint8_t* bufEndP) {    //start points to a "one" buffer byte, end points to a "zero" buffer byte
  uint64_t data = 0;
  bool dimValuePresent = false;

  for (uint8_t i = 0; i < 31; ++i) {

    if (calculateBufferPointerDistance(bufStartP, bufEndP) < 4) { //less than 4 more high/low rto read in buffer
      return false;
    }

    uint8_t b1 = *bufStartP;    //no of high
    stepBufferPointer(&bufStartP);
    uint8_t b2 = *bufStartP;    //no of low
    stepBufferPointer(&bufStartP);
    uint8_t b3 = *bufStartP;    //no of high
    stepBufferPointer(&bufStartP);
    uint8_t b4 = *bufStartP;    //no of low
    stepBufferPointer(&bufStartP);

    //TODO: add support for absolute dim values

    if (ARCHTECH_LOW_LOW <= b1 && b1 <= ARCHTECH_LOW_HIGH &&
        ARCHTECH_LOW_LOW <= b2 && b2 <= ARCHTECH_LOW_HIGH &&
        ARCHTECH_LOW_LOW <= b3 && b3 <= ARCHTECH_LOW_HIGH &&
        ARCHTECH_HIGH_LOW <= b4 && b4 <= ARCHTECH_HIGH_HIGH) {  //"one" is sent over air
      data <<= 1; //shift in a zero
      data |= 0x1;    //add one
    } else if (ARCHTECH_LOW_LOW <= b1 && b1 <= ARCHTECH_LOW_HIGH &&
               ARCHTECH_HIGH_LOW <= b2 && b2 <= ARCHTECH_HIGH_HIGH &&
               ARCHTECH_LOW_LOW <= b3 && b3 <= ARCHTECH_LOW_HIGH &&
               ARCHTECH_LOW_LOW <= b4 && b4 <= ARCHTECH_LOW_HIGH) { //"zero" is sent over air
      data <<= 1; //shift in a zero
    } else {
      return false;
    }

  }

  Serial.print(F("+Wclass:command;protocol:arctech;model:selflearning;data:0x"));
  uint8_t hexToSend = (dimValuePresent ? 9 : 8);
  for (int8_t i = hexToSend - 1; i >= 0; --i) {
    Serial.print( (byte)((data >> (4 * i)) & 0x0F), HEX);
  }
  Serial.println(F(";"));

  return true;
}; //end parseArctechSelfLearning
