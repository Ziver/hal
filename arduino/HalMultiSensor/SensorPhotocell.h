#ifndef SensorPhotocell_H
#define SensorPhotocell_H

#include "HalInterfaces.h"
#include "Interrupt.h"


class SensorPhotocell : public SensorPowerConsumption
{
public:
    virtual void setup();
    virtual unsigned int getConsumption();
    virtual void reset();

private:
    Interrupt* interrupt;
    unsigned int pulse;
};

#endif // SensorPhotocell_H
