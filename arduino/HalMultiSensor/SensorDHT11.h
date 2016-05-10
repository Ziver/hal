#ifndef SensorDHT11_h
#define SensorDHT11_h

#include <Arduino.h>
#include "HalInterfaces.h"


class SensorDHT11 : public SensorTemperature
{
public:
    SensorDHT11(short pin) : pin(pin) {};

    virtual void setup();
    virtual void read(TemperatureData& data);

private:
    short pin;
};
#endif
