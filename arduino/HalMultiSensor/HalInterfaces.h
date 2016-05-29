#ifndef HALINTERFACES_H
#define HALINTERFACES_H

#include "HalConfiguration.h"

// Utility functions

#ifdef ENABLE_DEBUG
    #include <Arduino.h>

    #define DEBUG(msg) \
        Serial.println(msg); \
        Serial.flush();
    #define DEBUGF(msg, ...) \
        static char buffer[80];\
        snprintf(buffer, sizeof(buffer), msg, __VA_ARGS__);\
        Serial.println(buffer);\
        Serial.flush();
#else
    #define DEBUG(msg)
    #define DEBUGF(msg, ...)
#endif


inline void pulse(short pin, short count)
{
    while (--count >= 0)
    {
        digitalWrite(INDICATOR_PIN, HIGH);
        delay(150);
        digitalWrite(INDICATOR_PIN, LOW);
        if (count != 0) delay(200);
    }
}

///////////////////////////////////////////////////////////////////////////
// INTERFACES

class Sensor
{
public:
    virtual void setup() = 0;
};
class Protocol
{
public:
    virtual void setup() = 0;
};



struct PowerData
{
    unsigned int consumption;
};
class SensorPowerConsumption : public Sensor
{
public:
    // returns number of pulses from power meter
    virtual void read(PowerData& data) = 0;
};
class ProtocolPowerConsumption : public Protocol
{
public:
    virtual void send(const PowerData& data) = 0;
};


struct TemperatureData
{
    float temperature;
    float humidity;
};
class SensorTemperature : public Sensor
{
public:
    virtual void read(TemperatureData& data) = 0;
};
class ProtocolTemperature : public Protocol
{
public:
    virtual void send(const TemperatureData& data) = 0;
};


struct LightData
{
    unsigned int lumen;
};
class SensorLight : public Sensor
{
public:
    virtual void read(LightData& data) = 0;
};
class ProtocolLight : public Protocol
{
public:
    virtual void send(const LightData& data) = 0;
};


#endif // HALINTERFACES_H
