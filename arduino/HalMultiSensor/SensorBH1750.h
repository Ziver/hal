#ifndef SensorBH1750_H
#define SensorBH1750_H

#include <Wire.h>
#include "HalInterfaces.h"


class SensorBH1750 : public SensorPowerConsumption, public SensorLight{
public:
    virtual void setup();
    virtual void read(PowerData& data);
    virtual void read(LightData& data);

private:
    unsigned int pulses;

    void configure(uint8_t mode);

};

#endif // SensorBH1750_H

