// 
//    FILE: dht11.h
// VERSION: 0.3.2
// PURPOSE: DHT11 Temperature & Humidity Sensor library for Arduino
// LICENSE: GPL v3 (http://www.gnu.org/licenses/gpl.html)
//
// DATASHEET: http://www.micro4you.com/files/sensor/DHT11.pdf
//
//     URL: http://arduino.cc/playground/Main/DHT11Lib
//
// HISTORY:
// George Hadjikyriacou - Original version
// see dht.cpp file
// *** Terry King: Changed include Arduino.h for 1.0x  
#ifndef dht11_h
#define dht11_h

#include <Arduino.h>
#include "HalInterfaces.h"

#define DHT11LIB_VERSION "0.3.2"

class SensorDHT11 : public SensorTemperature
{
public:
    SensorDHT11(int pin);
    virtual void setup();
    virtual int getTemperature();
    virtual int getHumidity();
    int read();

private:
    unsigned int pin;
    float temperature;
    unsigned char humidity;
};
#endif
