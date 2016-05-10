#ifndef SensorPhotocell_H
#define SensorPhotocell_H

#include "HalInterfaces.h"
#include "Interrupt.h"


class SensorPhotocell : public SensorPowerConsumption
{
public:
    virtual void setup();
    virtual void read(PowerData& data);
    virtual void reset();

private:
    unsigned int pulse;
};

#endif // SensorPhotocell_H
