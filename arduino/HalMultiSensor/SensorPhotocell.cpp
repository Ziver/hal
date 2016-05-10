#include "SensorPhotocell.h"
#include <Arduino.h>


void SensorPhotocell::setup()
{
    Interrupt::setupPinInterrupt(PC2); //PC3
}

void SensorPhotocell::read(PowerData& data)
{
    data.consumption = pulse;
}

void SensorPhotocell::reset()
{
    pulse = 0;
}