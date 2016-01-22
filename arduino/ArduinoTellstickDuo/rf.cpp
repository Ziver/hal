#include "rf.h"
#include "buffer.h"
#include "archtech.h"
#include "config.h"

volatile bool RFRX = false;

void parseRadioRXBuffer() {
  static uint8_t* bufferReadP = RF_rxBufferStartP;
  static uint8_t* startDataP = 0; //will always point to a "high" buffer address
  static uint8_t* endDataP = 0;   //will always point to a "low" buffer address
  static uint8_t prevValue = 0;  //contains the value of the previous buffer index read

  bool parse = false;
  while (bufferReadP != bufferWriteP) { //stop if the read pointer is pointing to where the writing is currently performed

    if ( (((int)bufferReadP) & 0x1) == 1 ) { //buffer pointer is odd (stores highs)
      if (prevValue >= SILENCE_LENGTH) {
        startDataP = bufferReadP;   //some new data must starrt here since this is the first "high" after a silent period
      }
    } else { //buffer pointer is even    (stores lows)
      if (*bufferReadP >= SILENCE_LENGTH) {  //evaluate if it is time to parse the curernt data
        endDataP = bufferReadP;     //this is a silient period and must be the end of a data
        parse = true;
        break;
      }
    }

    uint8_t* nextBufferReadP = getNextBufferPointer(bufferReadP);
    if (nextBufferReadP == startDataP) { //next pointer will point to startDataP. Data will overflow. Reset the data pointers.
      startDataP = 0;
      endDataP = 0;
    }

    //advance buffer pointer one step
    bufferReadP = nextBufferReadP;

    prevValue = *bufferReadP;  //update previous value
  }

  if (!parse) {
    return;
  }

  if (startDataP == 0 || endDataP == 0) {
    return;
  }

  /*
  * At this point the startDataP will point to the first high after a silent period
  * and the endDataP will point at the first (low) silent period after the data data start.
  */

  //make sure that the data set size is big enought to parse.
  uint16_t dataSetSize = calculateBufferPointerDistance(startDataP, endDataP);
  if (dataSetSize < 32) { //at least 32 low/high
    return;
  }

  //Let all available parsers parse the data set now.
  parseArctechSelfLearning(startDataP, endDataP);
  //TODO: add more parsers here
  
};   //end radioTask

void sendTCodedData(uint8_t* data, uint8_t T_long, uint8_t* timings, uint8_t repeat, uint8_t pause) {
  RFRX = false;   //turn off the RF reciever
  for (uint8_t rep = 0; rep < repeat; ++rep) {
    bool nextPinState = HIGH;
    for (int i = 0; i < T_long; ++i) {
      uint8_t timeIndex = (data[i / 4] >> (6 - (2 * (i % 4)))) & 0x03;
      if (timings[timeIndex] > 0 || i == T_long - 1) {
        digitalWrite(TX_PIN, nextPinState);
        delayMicroseconds(10 * timings[timeIndex]);
      }
      nextPinState = !nextPinState;
    }
    digitalWrite(TX_PIN, LOW);
    if (rep < repeat - 1) {
      delay(pause);
    }
  }
  RFRX = true;   //turn on the RF reciever
};

void sendSCodedData(uint8_t* data, uint8_t pulseCount, uint8_t repeat, uint8_t pause) {
  RFRX = false;   //turn off the RF reciever
  for (uint8_t rep = 0; rep < repeat; ++rep) {
    bool nextPinState = HIGH;
    for (int i = 0; i < pulseCount; ++i) {
      if (data[i] > 0 || i == pulseCount - 1) {
        digitalWrite(TX_PIN, nextPinState);
        delayMicroseconds(data[i] * 10);
      }
      nextPinState = !nextPinState;
    }
    delay(pause);
  }
  RFRX = false;   //turn on the RF reciever
};
