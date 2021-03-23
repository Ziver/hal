#ifndef SensorPhotocell_H
#define SensorPhotocell_H

#include "HalInterfaces.h"
#include "Interrupt.h"


class SensorPhotocell : public SensorPowerConsumption
{
public:
    virtual void setup();
    virtual void read(PowerData& data);

private:
    static unsigned int pulse;

    static void interruptHandler();
};

#endif // SensorPhotocell_H
