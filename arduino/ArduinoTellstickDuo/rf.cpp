#include "rf.h"
#include "buffer.h"
#include "archtech.h"
#include "config.h"
#include "oregonV2.1.h"

#define SILENCE_LENGTH  100 //the number of samples with "low" that represents a silent period between two signals

volatile bool RFRX = false;

void parseRadioRXBuffer() {
  static uint8_t* bufferReadP = RF_rxBufferStartP;
  static uint8_t* startDataP = 0; //will always point to a "high" buffer address
  static uint8_t* endDataP = 0;   //will always point to a "low" buffer address
  static uint8_t prevValue = 0;  //contains the value of the previous buffer index read

  bool parse = false;
  while (bufferReadP != bufferWriteP) { //stop if the read pointer is pointing to where the writing is currently performed
    uint8_t sampleCount = *bufferReadP;
    
    if ( (((uintptr_t)bufferReadP) & 0x1) == 1 ) { //buffer pointer is odd (stores highs)
      //Serial.print("high:"); Serial.println(sampleCount);
      if (prevValue >= SILENCE_LENGTH) {
        startDataP = bufferReadP;   //some new data must start here since this is the first "high" after a silent period
      }
      
      //stream data to stream parsers
      parseOregonStream(HIGH, sampleCount);
      
    } else { //buffer pointer is even    (stores lows)
      //Serial.print("low:"); Serial.println(sampleCount);
      if (sampleCount >= SILENCE_LENGTH) {  //evaluate if it is time to parse the curernt data
        endDataP = bufferReadP;     //this is a silient period and must be the end of a data
        if(startDataP != 0){
          parse = true;
          break;
        }
      }
      
      //stream data to stream parsers
      parseOregonStream(LOW, sampleCount);
      
    }

    //step the read pointer one step
    uint8_t* nextBufferReadP = getNextBufferPointer(bufferReadP);
    if (nextBufferReadP == startDataP) { //next pointer will point to startDataP. Data will overflow. Reset the data pointers.
      startDataP = 0;
      endDataP = 0;
      prevValue = 0;
    }

    //advance buffer pointer one step
    bufferReadP = nextBufferReadP;

    prevValue = sampleCount;  //update previous value
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

  //Serial.print((uintptr_t)startDataP); Serial.print(" - "); Serial.println((uintptr_t)endDataP);

  //Let all available parsers parse the data set now.
  parseArctechSelfLearning(startDataP, endDataP);
  //TODO: add more parsers here
  
  //reset the data pointers since the data have been parsed at this point
  startDataP = 0;
  endDataP = 0;
  
};   //end radioTask

void sendTCodedData(uint8_t* data, uint8_t T_long, uint8_t* timings, uint8_t repeat, uint8_t pause) {
  ACTIVATE_RADIO_TRANSMITTER();
  for (uint8_t rep = 0; rep < repeat; ++rep) {
    bool nextPinState = HIGH;
    for (int i = 0; i < T_long; ++i) {
      uint8_t timeIndex = (data[i / 4] >> (6 - (2 * (i % 4)))) & 0x03;
      if (timings[timeIndex] > 0 || i == T_long - 1) {
        if(nextPinState){
            TX_PIN_HIGH();
        }else{
            TX_PIN_LOW();
        }
        delayMicroseconds(10 * timings[timeIndex]);
      }
      nextPinState = !nextPinState;
    }
    TX_PIN_LOW();
    if (rep < repeat - 1) {
      delay(pause);
    }
  }
  ACTIVATE_RADIO_RECEIVER();
};

void sendSCodedData(uint8_t* data, uint8_t pulseCount, uint8_t repeat, uint8_t pause) {
  ACTIVATE_RADIO_TRANSMITTER();
  for (uint8_t rep = 0; rep < repeat; ++rep) {
    bool nextPinState = HIGH;
    for (int i = 0; i < pulseCount; ++i) {
      if (data[i] > 0 || i == pulseCount - 1) {
        if(nextPinState){
            TX_PIN_HIGH();
        }else{
            TX_PIN_LOW();
        }
        delayMicroseconds(data[i] * 10);
      }
      nextPinState = !nextPinState;
    }
    delay(pause);
  }
  TX_PIN_LOW();
  ACTIVATE_RADIO_RECEIVER();
};
