//
//    FILE: dht11.cpp
// VERSION: 0.3.2
// PURPOSE: DHT11 Temperature & Humidity Sensor library for Arduino
// LICENSE: GPL v3 (http://www.gnu.org/licenses/gpl.html)
//
// DATASHEET: http://www.micro4you.com/files/sensor/DHT11.pdf
//
// HISTORY:
// George Hadjikyriacou - Original version (??)
// Mod by SimKard - Version 0.2 (24/11/2010)
// Mod by Rob Tillaart - Version 0.3 (28/03/2011)
// + added comments
// + removed all non DHT11 specific code
// + added references
// Refactored by Ziver Koc
//

#include "SensorDHT11.h"


void SensorDHT11::setup(){}


void SensorDHT11::read(TemperatureData& data)
{
	// BUFFER TO RECEIVE
	uint8_t bits[5] = {0};
	uint8_t cnt = 7;
	uint8_t idx = 0;

	// REQUEST SAMPLE
	pinMode(pin, OUTPUT);
	digitalWrite(pin, LOW);
	delay(18);
	digitalWrite(pin, HIGH);
	delayMicroseconds(40);
	pinMode(pin, INPUT);

	// ACKNOWLEDGE or TIMEOUT
	unsigned int loopCnt = 10000;
	while(digitalRead(pin) == LOW)
		if (loopCnt-- == 0)
		{
		    DEBUG("DHT11 timeout");
		    return;;
		}

	loopCnt = 10000;
	while(digitalRead(pin) == HIGH)
		if (loopCnt-- == 0)
        {
            DEBUG("DHT11 timeout");
            return;;
        }

	// READ OUTPUT - 40 BITS => 5 BYTES or TIMEOUT
	for (int i=0; i<40; i++)
	{
		loopCnt = 10000;
		while(digitalRead(pin) == LOW)
			if (loopCnt-- == 0)
			{
                DEBUG("DHT11 timeout");
                return;;
            }

		unsigned long t = micros();

		loopCnt = 10000;
		while(digitalRead(pin) == HIGH)
			if (loopCnt-- == 0)
            {
                DEBUG("DHT11 timeout");
                return;;
            }

		if ((micros() - t) > 40) bits[idx] |= (1 << cnt);
		if (cnt == 0)   // next byte?
		{
			cnt = 7;    // restart at MSB
			idx++;      // next byte!
		}
		else cnt--;
	}


	uint8_t sum = bits[0] + bits[2];
	if (bits[4] != sum)
        DEBUG("DHT11 checksum error");

    // WRITE TO RIGHT VARS
        // as bits[1] and bits[3] are allways zero they are omitted in formulas.
    data.temperature = bits[2];
    data.humidity    = bits[0];
}

