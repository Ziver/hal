#include "SensorPhotocell.h"
#include <Arduino.h>


void SensorPhotocell::setup()
{
    Interrupt::setupPinInterrupt(PC2); //PC3
}

unsigned int SensorPhotocell::getConsumption()
{
    return pulse;
}

void SensorPhotocell::reset()
{
    pulse = 0;
}