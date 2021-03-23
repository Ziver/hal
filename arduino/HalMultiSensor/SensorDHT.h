#ifndef SensorDHT_h
#define SensorDHT_h

#include <Arduino.h>
#include "HalInterfaces.h"


// Define types of sensors.
#define DHT11 11
#define DHT22 22
#define DHT21 21


class SensorDHT : public SensorTemperature
{
public:
    SensorDHT(uint8_t  type, uint8_t pin) : _type(type), _pin(pin) {};

    virtual void setup();
    virtual void read(TemperatureData& data);

private:
    uint8_t _type, _pin;
    uint32_t _maxcycles;
    #ifdef __AVR
        // Use direct GPIO access on an 8-bit AVR so keep track of the port and bitmask
        // for the digital pin connected to the DHT.  Other platforms will use digitalRead.
        uint8_t _bit, _port;
    #endif

    uint32_t expectPulse(bool level);
};
#endif
