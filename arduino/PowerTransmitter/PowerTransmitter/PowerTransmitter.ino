/*
* Protocol: Oregon V2.1
* Emulating sensor: THGR2228N
*/

#include <Wire.h>
#include "BH1750FVI.h"

BH1750FVI LightSensor;
const byte TX_PIN = 10;
const byte LED_PIN = 13;
const unsigned long TIME = 512;
const unsigned long TWOTIME = TIME*2;
#define SEND_HIGH() digitalWrite(TX_PIN, HIGH)
#define SEND_LOW() digitalWrite(TX_PIN, LOW)
byte OregonMessageBuffer[9];
unsigned long previousTime = 0;
unsigned long currentTime = millis();
int impulseCount = 0;

void setup()
{
  Serial.begin(9600);
  
  pinMode(TX_PIN, OUTPUT);
  pinMode(LED_PIN, OUTPUT);
  SEND_LOW();
  byte ID[] = { 0x1A,0x2D  };  //temperature/humidity sensor (THGR2228N)
  setType(OregonMessageBuffer, ID);
  setChannel(OregonMessageBuffer, 0x20);
  
  LightSensor.begin();
  LightSensor.SetAddress(Device_Address_L);
  LightSensor.SetMode(Continuous_H_resolution_Mode);
  Serial.print("Started");
}


boolean light = false;
void loop()
{
  currentTime = millis();
  uint16_t lux = LightSensor.GetLightIntensity();
  if(lux > 100 && !light){
    light = true;
    impulseCount++;
  }else if(lux < 100){
    light = false;
  }
  if(currentTime - previousTime > 60000) {
    previousTime = currentTime;
    Serial.print("total impulses = ");
    Serial.println(impulseCount);
    send433(impulseCount,0,0xBA);
    impulseCount = 0;
    delay(500);
  }
}

void send433(float temperature, byte humidity, byte Identitet)
{
  digitalWrite(LED_PIN, HIGH);
  setId(OregonMessageBuffer, Identitet); //set id of the sensor, BB=187
  setBatteryLevel(OregonMessageBuffer, 1); // 0 : low, 1 : high
  setTemperature(OregonMessageBuffer, temperature); //org setTemperature(OregonMessageBuffer, 55.5);
  setHumidity(OregonMessageBuffer, humidity);
  calculateAndSetChecksum(OregonMessageBuffer);

  // Show the Oregon Message
  for (byte i = 0; i < sizeof(OregonMessageBuffer); ++i) { 
    Serial.print(OregonMessageBuffer[i] >> 4, HEX);
    Serial.print(OregonMessageBuffer[i] & 0x0F, HEX);
  }
  Serial.println();

  // Send the Message over RF
  sendOregon(OregonMessageBuffer, sizeof(OregonMessageBuffer));
  // Send a "pause"
  SEND_LOW();  
  delayMicroseconds(TWOTIME*8);
  // Send a copie of the first message. The v2.1 protocol send the message two time
  sendOregon(OregonMessageBuffer, sizeof(OregonMessageBuffer));
  SEND_LOW();
  digitalWrite(LED_PIN, LOW);
}

inline void setId(byte *data, byte ID)
{
  data[3] = ID;
}

void setBatteryLevel(byte *data, byte level)
{
  if(!level) data[4] = 0x0C;
  else data[4] = 0x00;
}

void setTemperature(byte *data, float temp)
{
  // Set temperature sign
  if(temp < 0)
  {
    data[6] = 0x08;
    temp *= -1;
  }
  else
  {
    data[6] = 0x00;
  }

  // Determine decimal and float part
  int tempInt = (int)temp;
  int td = (int)(tempInt / 10);
  int tf = (int)round((float)((float)tempInt/10 - (float)td) * 10);

  int tempFloat = (int)round((float)(temp - (float)tempInt) * 10);

  // Set temperature decimal part
  data[5] = (td << 4);
  data[5] |= tf;

  // Set temperature float part
  data[4] |= (tempFloat << 4);
}

void setHumidity(byte* data, byte hum)
{
  data[7] = (hum/10);
  data[6] |= (hum - data[7]*10) << 4;
}

void calculateAndSetChecksum(byte* data)
{
  int sum = 0;
  for(byte i = 0; i<8;i++)
  {
    sum += (data[i]&0xF0) >> 4;
    sum += (data[i]&0xF);
  }
  data[8] = ((sum - 0xa) & 0xFF);
}




//*********************************************************************************************************


/**
 * \brief Send logical "0" over RF
 * \details azero bit be represented by an off-to-on transition
 * \ of the RF signal at the middle of a clock period.
 * \ Remenber, the Oregon v2.1 protocol add an inverted bit first
 */
inline void sendZero(void)
{
  SEND_HIGH();
  delayMicroseconds(TIME);
  SEND_LOW();
  delayMicroseconds(TWOTIME);
  SEND_HIGH();
  delayMicroseconds(TIME);
}

/**
 * \brief Send logical "1" over RF
 * \details a one bit be represented by an on-to-off transition
 * \ of the RF signal at the middle of a clock period.
 * \ Remenber, the Oregon v2.1 protocol add an inverted bit first
 */
inline void sendOne(void)
{
  SEND_LOW();
  delayMicroseconds(TIME);
  SEND_HIGH();
  delayMicroseconds(TWOTIME);
  SEND_LOW();
  delayMicroseconds(TIME);
}

/**
 * \brief Send a bits quarter (4 bits = MSB from 8 bits value) over RF
 * \param data Data to send
 */
inline void sendQuarterMSB(const byte data)
{
  (bitRead(data, 4)) ? sendOne() : sendZero();
  (bitRead(data, 5)) ? sendOne() : sendZero();
  (bitRead(data, 6)) ? sendOne() : sendZero();
  (bitRead(data, 7)) ? sendOne() : sendZero();
}

/**
 * \brief Send a bits quarter (4 bits = LSB from 8 bits value) over RF
 * \param data Data to send
 */
inline void sendQuarterLSB(const byte data)
{
  (bitRead(data, 0)) ? sendOne() : sendZero();
  (bitRead(data, 1)) ? sendOne() : sendZero();
  (bitRead(data, 2)) ? sendOne() : sendZero();
  (bitRead(data, 3)) ? sendOne() : sendZero();
}

/******************************************************************/
/******************************************************************/
/******************************************************************/

/**
 * \brief Send a buffer over RF
 * \param data Data to send
 * \param size size of data to send
 */
void sendData(byte *data, byte size)
{
  for(byte i = 0; i < size; ++i)
  {
    sendQuarterLSB(data[i]);
    sendQuarterMSB(data[i]);
  }
}

/**
 * \brief Send an Oregon message
 * \param data The Oregon message
 */
void sendOregon(byte *data, byte size)
{
  sendPreamble();
  //sendSync();
  sendData(data, size);
  sendPostamble();
}

/**
 * \brief Send preamble
 * \details The preamble consists of 16 "1" bits
 */
inline void sendPreamble(void)
{
  byte PREAMBLE[]={ 
    0xFF,0xFF   };
  sendData(PREAMBLE, 2);
}

/**
 * \brief Send postamble
 * \details The postamble consists of 8 "0" bits
 */
inline void sendPostamble(void)
{
  byte POSTAMBLE[]={ 
    0x00   };
  sendData(POSTAMBLE, 1);
}

/**
 * \brief Send sync nibble
 * \details The sync is 0xA. It is not use in this version since the sync nibble
 * \ is include in the Oregon message to send.
 */
inline void sendSync(void)
{
  sendQuarterLSB(0xA);
}

/******************************************************************/
/******************************************************************/
/******************************************************************/

/**
 * \brief Set the sensor type
 * \param data Oregon message
 * \param type Sensor type
 */
inline void setType(byte *data, byte* type)
{
  data[0] = type[0];
  data[1] = type[1];
}

/**
 * \brief Set the sensor channel
 * \param data Oregon message
 * \param channel Sensor channel (0x10, 0x20, 0x30)
 */
inline void setChannel(byte *data, byte channel)
{
  data[2] = channel;
}



