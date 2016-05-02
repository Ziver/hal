#include "SensorPhotocell.h"


void pinInterrupt()
{
    //++pulse;
}


void SensorPhotocell::setup()
{
    interrupt = new Interrupt(pinInterrupt);
    interrupt->setupPinInterrupt(PC2); //PC3
}

unsigned int SensorPhotocell::getConsumption()
{
    return pulse;
}

void SensorPhotocell::reset()
{
    pulse = 0;
}