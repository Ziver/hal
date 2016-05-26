#include "SensorPhotocell.h"
#include <Arduino.h>

void SensorPhotocell::interruptHandler()
{
    ++pulse;
}


void SensorPhotocell::setup()
{
    Interrupt::setPinCallback(SensorPhotocell::interruptHandler);
    Interrupt::setupPinInterrupt(PC2); //PC3
}

void SensorPhotocell::read(PowerData& data)
{
    data.consumption = pulse;
    pulse = 0;
}
