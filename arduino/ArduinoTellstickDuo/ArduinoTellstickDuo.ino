#include "usart.h"
#include "rf.h"
#include "buffer.h"
#include "config.h"


ISR(TIMER2_COMPA_vect) { //timer2 interrupt 16kHz. Samples the RF Rx data pin

  if (!RFRX) { //if no Radio Rx should be performed
    return;
  }

  uint8_t bit = digitalRead(RX_PIN); // optimized "digital_read(7)" = "PIND & 0x1"

  //will store "lows" on even buffer addresses and "highs" on odd buffer addresses
  if ( ( ((int)bufferWriteP) & 0x1 ) != bit ) { //compare the bit and the buffer pointer address. true if one is odd and one is even.
    //step the buffer pointer
    if ( bufferWriteP + 1 > RF_rxBufferEndP) {
      bufferWriteP = RF_rxBufferStartP;
    } else {
      ++bufferWriteP;
    }
    *bufferWriteP = 1;  //reset and step once on this addess
  } else {
    if (*bufferWriteP < 255) {  //only values up to 255
      ++(*bufferWriteP);    //step the buffer
    }
  }

};//end timer2 interrupt


void setup() {

  Serial.begin(9600);

  pinMode(RX_PIN, INPUT); //set RX pin to input
  pinMode(TX_PIN, OUTPUT); //ser TX pin as output

  //setup timer2 interrupt at 16kHz for RF sampling
  cli();//stop interrupts
  TCCR2A = 0;// set entire TCCR2A register to 0
  TCCR2B = 0;// same for TCCR2B
  TCNT2  = 0;//initialize counter value to 0
  OCR2A = 124;// = ( (16*10^6) / (16000Hz*8pc) ) - 1 (must be <256)
  TCCR2A |= (1 << WGM21);  // turn on CTC mode
  TCCR2B |= (1 << CS21);    // Set CS21 bit for 8 prescaler
  TIMSK2 |= (1 << OCIE2A);  // enable timer compare interrupt
  sei();//allow interrupts

  //reset buffer just to be sure
  for (uint8_t* p = RF_rxBufferStartP; p <= RF_rxBufferEndP; ++p) {
    *p = 0;
  }

  RFRX = true;    //start receiving radio data

};//end setup

void loop() {

  //Receive and execute command over serial
  parseSerialForCommand();

  //Receive signal over air and send it over serial
  parseRadioRXBuffer();

};//end loop
