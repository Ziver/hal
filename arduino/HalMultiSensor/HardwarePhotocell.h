#ifndef HARDWAREPHOTOCELL_H
#define HARDWAREPHOTOCELL_H

#include "HalInterfaces.h"
#include "Interrupt.h"


class HardwarePhotocell : public HardwarePowerConsumption 
{
public:
    virtual void setup();
    virtual unsigned int getConsumption();
    virtual void reset();

private:
    Interrupt* interrupt;
    unsigned int pulse;
};

#endif // HARDWAREPHOTOCELL_H
