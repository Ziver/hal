#include "usart.h"
#include "rf.h"

uint8_t Serial_rxBuffer[79];

void parseSerialForCommand() {
  if (Serial.available() > 0) {
    uint8_t rxDataSize = Serial.readBytesUntil('+', &Serial_rxBuffer[0], 79);
    if (rxDataSize > 0) {
      parseRxBuffer(&Serial_rxBuffer[0], 0, rxDataSize, false, 3, 0);
    }
  }
};   //end serialTask

bool parseRxBuffer(byte* buffer, uint8_t startIndex, uint8_t endIndex, bool debug, uint8_t repeat, uint8_t pause) {
  if (startIndex > endIndex) {
    return false;
  }
  char c = buffer[startIndex];
  //Serial.print("DEBUG: char:"); Serial.println(c, DEC);
  switch (c) {
    case 'S':
      return handleSCommand(buffer, startIndex + 1, endIndex, debug, repeat, pause);
    case 'T':
      return handleTCommand(buffer, startIndex + 1, endIndex, debug, repeat, pause);
    case 'V':
      Serial.println(F("+V2"));
      return parseRxBuffer(buffer, startIndex + 1, endIndex, debug, repeat, pause);
    case 'D':
      return parseRxBuffer(buffer, startIndex + 1, endIndex, !debug, repeat, pause);
    case 'P':
      if (endIndex - startIndex + 1 < 3) {
        return false;
      } //at least {'P',[p-value],'+'} must be left in the buffer
      return parseRxBuffer(buffer, startIndex + 2, endIndex, debug, repeat, buffer[startIndex + 1]);
    case 'R':
      if (endIndex - startIndex + 1 < 3) {
        return false;
      } //at least {'R',[r-value],'+'} must be left in the buffer
      return parseRxBuffer(buffer, startIndex + 2, endIndex, debug, buffer[startIndex + 1], pause);
    case '+':
      return true;
    default:
      //Serial.print("DEBUG: unknown char: '"); Serial.print(c, BIN); Serial.println("'");
      return false;
  }
}; //end parseRxBuffer

bool handleSCommand(byte* buffer, uint8_t startIndex, uint8_t endIndex, bool debug, uint8_t repeat, uint8_t pause) {
  //Parse message received from serial
  uint8_t S_data[78]; //78 pulses
  uint8_t pulseCount = 0;
  for (uint8_t i = startIndex; i <= endIndex; ++i) {
    if (buffer[i] == '+') {
      break;
    } else if (i == endIndex) {
      return false;
    } else {
      S_data[pulseCount++] = buffer[i];
    }
  }
  //Send message
  sendSCodedData(&S_data[0], pulseCount, repeat, pause);

  //send confirmation over serial
  Serial.println(F("+S"));
  return true;
}; //end handleS

bool handleTCommand(byte* buffer, uint8_t startIndex, uint8_t endIndex, bool debug, uint8_t repeat, uint8_t pause) {
  //Parse message received from serial
  uint8_t T_data[72]; //0-188 pulses
  if (endIndex - startIndex < 5) {
    //Serial.println("DEBUG: wrong size!");
    return false;
  }
  uint8_t buff_p = startIndex;
  uint8_t T_times[4] = {buffer[buff_p++], buffer[buff_p++], buffer[buff_p++], buffer[buff_p++]};
  uint8_t T_long = buffer[buff_p++];
  uint8_t T_bytes = 0;
  if ( (T_long / 4.0) > (float)(T_long / 4) ) {
    T_bytes = T_long / 4 + 1;
  } else {
    T_bytes = T_long / 4;
  }
  uint8_t j = 0;
  while (j < T_bytes) {
    if (buffer[buff_p] == '+') {
      break;
    } else if (buff_p >= endIndex) {
      return false;
    } else {
      T_data[j++] = buffer[buff_p++];
    }
  }
  if ( j != T_bytes ) {
    return false;
  }

  //Send message
  sendTCodedData(&T_data[0], T_long, &T_times[0], repeat, pause);

  //send confirmation over serial
  Serial.println(F("+T"));
  return parseRxBuffer(buffer, buff_p, endIndex, debug, repeat, pause);
}; //end handleT
