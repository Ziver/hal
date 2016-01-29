#include "usart.h"
#include "rf.h"
#include "buffer.h"
#include "config.h"

/*
* Timer2 interrupt in 16kHz. Samples the RF Rx data pin
*/
ISR(TIMER2_COMPA_vect) {

  if ( !IS_RADIO_RECIEVER_ON() ) { //if no Radio Rx should be performed
    return;
  }

  uint8_t bit = RX_PIN_READ();

  //will store "lows" on even buffer addresses and "highs" on odd buffer addresses
  if ( ( ((uintptr_t)bufferWriteP) & 0x1 ) != bit ) { //compare the bit and the buffer pointer address. true if one is odd and one is even.
    //step the buffer pointer
    if ( bufferWriteP+1 > RF_rxBufferEndP ) {
      *RF_rxBufferStartP = 1;   //reset the next data point before going there
      bufferWriteP = RF_rxBufferStartP;
    } else {
      *(bufferWriteP+1) = 1;   //reset the next data point before going there
      ++bufferWriteP;
    }
  } else {
    if ( *bufferWriteP < 255 ) {  //Do not step the value if it already is 255 (max value)
      ++(*bufferWriteP);    //step the buffer value
    }
  }

};//end timer2 interrupt


void setup() {

  Serial.begin(9600);

  setupPins();

  //setup timer2 interrupt at 16kHz for RF sampling
  cli();    //stop interrupts
  TCCR2A = 0;   // set entire TCCR2A register to 0
  TCCR2B = 0;   // same for TCCR2B
  TCNT2  = 0;   //initialize counter value to 0
  OCR2A = 124;  // = ( (16000000Hz) / (16000Hz*8prescaler) ) - 1    (must be <256)
  TCCR2A |= (1 << WGM21);  // turn on CTC mode
  TCCR2B |= (1 << CS21);    // Set CS21 bit for 8 prescaler
  TIMSK2 |= (1 << OCIE2A);  // enable timer compare interrupt
  sei();    //allow interrupts

  //reset buffer just to be sure
  for (uint8_t* p = RF_rxBufferStartP; p <= RF_rxBufferEndP; ++p) {
    *p = 0;
  }
  
  Serial.println(F("+V2"));

  ACTIVATE_RADIO_RECEIVER();

};//end setup

void loop() {

  //Receive and execute command over serial
  parseSerialForCommand();

  //Receive signal over air and send it over serial
  parseRadioRXBuffer();

};//end loop
