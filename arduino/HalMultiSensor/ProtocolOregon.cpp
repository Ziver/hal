#include "ProtocolOregon.h"

#define RF_DELAY 512
#define RF_DELAY_LONG RF_DELAY*2
#define RF_SEND_HIGH() digitalWrite(txPin, HIGH)
#define RF_SEND_LOW() digitalWrite(txPin, LOW)


void ProtocolOregon::setup()
{
  pinMode(txPin, OUTPUT);
  RF_SEND_LOW();
}


void ProtocolOregon::send(const PowerData& data)
{
    send(data.consumption, 0);
}

void ProtocolOregon::send(const TemperatureData& data)
{
    send(data.temperature, data.humidity);
}

void ProtocolOregon::send(const LightData& data)
{
    send(data.lumen, 0);
}



void ProtocolOregon::send(float temperature, short humidity)
{
    byte buffer[9];
    setType(buffer, 0x1A,0x2D); //temperature/humidity sensor (THGR2228N)
    setChannel(buffer, 0x20);
    setId(buffer, address); //set id of the sensor, BB=187
    setBatteryLevel(buffer, true); // false : low, true : high
    setTemperature(buffer, temperature); //org setTemperature(OregonMessageBuffer, 55.5);
    setHumidity(buffer, humidity);
    calculateAndSetChecksum(buffer);

    // Send the Message over RF
    rfSend(buffer, sizeof(buffer));
    // Send a "pause"
    RF_SEND_LOW();
    delayMicroseconds(RF_DELAY_LONG * 8);
    // Send a copy of the first message. The v2.1 protocol send the message two RF_DELAYs
    rfSend(buffer, sizeof(buffer));
    RF_SEND_LOW();
}

/**
 * \brief Set the sensor type
 * \param data Oregon message
 * \param type Sensor type
 */
inline void ProtocolOregon::setType(byte data[], byte b1, byte b2)
{
  data[0] = b1;
  data[1] = b2;
}

/**
 * \brief Set the sensor channel
 * \param data Oregon message
 * \param channel Sensor channel (0x10, 0x20, 0x30)
 */
inline void ProtocolOregon::setChannel(byte data[], byte channel)
{
  data[2] = channel;
}


inline void ProtocolOregon::setId(byte data[], byte id)
{
  data[3] = id;
}

/**
 * \param   level     false: low, true: high
 */
inline void ProtocolOregon::setBatteryLevel(byte data[], bool level)
{
  if(!level) data[4] = 0x0C;
  else data[4] = 0x00;
}

inline void ProtocolOregon::setTemperature(byte data[], float temp)
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

inline void ProtocolOregon::setHumidity(byte data[], byte hum)
{
  data[7] = (hum/10);
  data[6] |= (hum - data[7]*10) << 4;
}

inline void ProtocolOregon::calculateAndSetChecksum(byte data[])
{
  int sum = 0;
  for(byte i = 0; i<8;i++)
  {
    sum += (data[i]&0xF0) >> 4;
    sum += (data[i]&0x0F);
  }
  data[8] = ((sum - 0x0A) & 0xFF);
}




//*********************************************************************************************************


/**
 * \brief Send logical "0" over RF
 * \details a zero bit be represented by an off-to-on transition
 * \ of the RF signal at the middle of a clock period.
 * \ Remember, the Oregon v2.1 protocol adds an inverted bit first
 */
inline void ProtocolOregon::sendZero(void)
{
  RF_SEND_HIGH();
  delayMicroseconds(RF_DELAY);
  RF_SEND_LOW();
  delayMicroseconds(RF_DELAY_LONG);
  RF_SEND_HIGH();
  delayMicroseconds(RF_DELAY);
}

/**
 * \brief Send logical "1" over RF
 * \details a one bit be represented by an on-to-off transition
 * \ of the RF signal at the middle of a clock period.
 * \ Remember, the Oregon v2.1 protocol add an inverted bit first
 */
inline void ProtocolOregon::sendOne(void)
{
  RF_SEND_LOW();
  delayMicroseconds(RF_DELAY);
  RF_SEND_HIGH();
  delayMicroseconds(RF_DELAY_LONG);
  RF_SEND_LOW();
  delayMicroseconds(RF_DELAY);
}


/******************************************************************/
/******************************************************************/
/******************************************************************/

/**
 * \brief Send a buffer over RF
 * \param data Data to send
 * \param length size of data array
 */
void ProtocolOregon::sendData(byte data[], byte length)
{
  for (byte i=0; i<length; ++i)
  {
    (bitRead(data[i], 0)) ? sendOne() : sendZero();
    (bitRead(data[i], 1)) ? sendOne() : sendZero();
    (bitRead(data[i], 2)) ? sendOne() : sendZero();
    (bitRead(data[i], 3)) ? sendOne() : sendZero();
    (bitRead(data[i], 4)) ? sendOne() : sendZero();
    (bitRead(data[i], 5)) ? sendOne() : sendZero();
    (bitRead(data[i], 6)) ? sendOne() : sendZero();
    (bitRead(data[i], 7)) ? sendOne() : sendZero();
  }
}

/**
 * \brief Send an Oregon message
 * \param data The Oregon message
 */
void ProtocolOregon::rfSend(byte data[], byte size)
{
    // Send preamble
    byte preamble[] = { 0xFF,0xFF };
    sendData(preamble, 2);
    // Send sync nibble
    //sendQuarterLSB(0xA); // It is not use in this version since the sync nibble is include in the Oregon message to send.
    // Send data
    sendData(data, size);
    // Send postamble
    byte postamble[] = { 0x00 };
    sendData(postamble, 1);
}

