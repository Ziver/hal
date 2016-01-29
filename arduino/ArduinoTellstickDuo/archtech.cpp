#include "archtech.h"
#include "buffer.h"

/*******************************************************************************
                      --ARCHTECH PROTOCOL SPECIFICATION--

    ----SIGNAL----
A signal consists of a PREAMBLE, DATA and an POSTAMBLE.
Each signal is sent 4 times in a row to increase the delivery success rate.

    ----PREAMBLE----
send high for 250 microseconds
send low for 2,500 microseconds

    ----DATA----
26 bits of transmitter id
1 bit indicating if the on/off bit is targeting a group
1 bit indicating "on" or "off"
4 bits indicating the targeted unit/channel/device
4 bits indicating a absolute dim level (optional)

Total: 32 or 36 bits depending if absolute dimming is used

Each real bit in the data field is sent over air as a pair of two inverted bits.
  real bit        bits over air
     1       =        "01"
     0       =        "10"

Over air a "1"(one) is sent as:
send high for 250 microseconds
send low for 1,250 microseconds

Over air a "0"(zero) is sent as:
send high for 250 microseconds
send low for 250 microseconds

    ----POSTAMBLE----
send high for 250 microseconds
send low for 10,000 microseconds

*******************************************************************************/

#define ARCHTECH_SHORT_MIN       2
#define ARCHTECH_SHORT_MAX       7
#define ARCHTECH_LONG_MIN       17
#define ARCHTECH_LONG_MAX       23

#define IS_SHORT(b) ( ARCHTECH_SHORT_MIN <= b && b <= ARCHTECH_SHORT_MAX )
#define IS_LONG(b)  (  ARCHTECH_LONG_MIN <= b && b <= ARCHTECH_LONG_MAX  )

#define IS_ZERO(b1,b2,b3,b4) ( IS_SHORT(b1) && IS_LONG(b2)  && IS_SHORT(b3) && IS_SHORT(b4) )
#define IS_ONE(b1,b2,b3,b4)  ( IS_SHORT(b1) && IS_SHORT(b2) && IS_SHORT(b3) && IS_LONG(b4)  )

bool parseArctechSelfLearning(uint8_t* bufStartP, uint8_t* bufEndP) {    //start points to a "high" buffer byte, end points to a "low" buffer byte
  uint64_t data = 0;
  bool dimValuePresent;

  uint16_t bitsInBuffer = calculateBufferPointerDistance(bufStartP, bufEndP) / 4;   //each bit is representd by 4 high/low
  if (bitsInBuffer == 32) {
    dimValuePresent = false;
  }else if(bitsInBuffer == 36){
    dimValuePresent = true;
  } else {
    return false;
  }
  
  for (uint8_t i = 0; i < bitsInBuffer-1; ++i) {

    uint8_t b1 = *bufStartP;    //no of high
    stepBufferPointer(&bufStartP);
    uint8_t b2 = *bufStartP;    //no of low
    stepBufferPointer(&bufStartP);
    uint8_t b3 = *bufStartP;    //no of high
    stepBufferPointer(&bufStartP);
    uint8_t b4 = *bufStartP;    //no of low
    stepBufferPointer(&bufStartP);

    if (IS_ONE(b1,b2,b3,b4)) {  //"one" is sent over air
      data <<= 1; //shift in a zero
      data |= 0x1;    //add one
    } else if (IS_ZERO(b1,b2,b3,b4)) { //"zero" is sent over air
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
