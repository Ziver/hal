#include "SensorPhotocell.h"
#include <Arduino.h>

unsigned int SensorPhotocell::pulse = 0;

void SensorPhotocell::interruptHandler()
{
    digitalWrite(INDICATOR_PIN, HIGH);
    DEBUG("PHCELL: INTERRUPT");
    ++pulse;
    digitalWrite(INDICATOR_PIN, LOW);
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
