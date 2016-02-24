#include "HardwarePhotocell.h"


void pinInterrupt()
{
    //++pulse;
}


void HardwarePhotocell::setup()
{
    interrupt = new Interrupt(pinInterrupt);
    interrupt->setupPinInterrupt(PC2); //PC3
}

unsigned int HardwarePhotocell::getConsumption()
{
    return pulse;
}

void HardwarePhotocell::reset()
{
    pulse = 0;
}