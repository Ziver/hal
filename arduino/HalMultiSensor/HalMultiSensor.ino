/*
A interrupt based sensor device that reads multiple sensors and transmits
the data to a central location.
*/
#if (ARDUINO >= 100)
#include <Arduino.h>
#else
#include <WProgram.h>
#endif
#include <Wire.h>

#include "HalInterfaces.h"
#include "HalDefinitions.h"
#include "HalConfiguration.h"
#include "Interrupt.h"


Interrupt* interrupt;

unsigned int timerMultiplierMAX;
unsigned int timerMultiplier = 0;

// Sensors
SensorPowerConsumption* powerSensor;
SensorTemperature* tempSensor;
SensorLight* lightSensor;

// Protocols
ProtocolPowerConsumption* powerProtocol;
ProtocolTemperature* tempProtocol;
ProtocolLight* lightProtocol;



void timerInterruptFunc();

void setup()
{
    timerMultiplierMAX = POWER_TIMER_MULTIPLIER * TEMPERATURE_TIMER_MULTIPLIER * LIGHT_TIMER_MULTIPLIER; // Find a lowest common denominator
    interrupt = new Interrupt(timerInterruptFunc);
    interrupt->setupTimerInterrupt(60*1000); // one minute scheduled interrupt
    
    // Setup Sensors and protocols
    #ifdef POWERCON_ENABLED
    powerSensor = new POWERCON_SENSOR;
    powerSensor->setup();
    powerProtocol = new POWERCON_PROTOCOL;
    powerProtocol->setup();
    #endif

    #ifdef TEMPERATURE_ENABLED
    tempSensor = new TEMPERATURE_SENSOR;
    tempSensor->setup();
    tempProtocol = new TEMPERATURE_PROTOCOL;
    tempProtocol->setup();
    #endif

    #ifdef LIGHT_ENABLED
    lightSensor = new LIGHT_SENSOR;
    lightSensor->setup();
    lightProtocol = new LIGHT_PROTOCOL;
    lightProtocol->setup();
    #endif
}


void loop() {}


void timerInterruptFunc()
{
    ++timerMultiplier;
    if (timerMultiplier > timerMultiplierMAX)
        timerMultiplier = 1;

    // Send power consumption
    #ifdef POWERCON_ENABLED
    if(timerMultiplier == POWER_TIMER_MULTIPLIER)
    {
        unsigned int consumption = powerSensor->getConsumption();
        powerSensor->reset();
        powerProtocol->setConsumption(consumption);
        powerProtocol->send();
    }
    #endif

    // Handle temperature sensor
    #ifdef TEMPERATURE_ENABLED
    if(timerMultiplier == TEMPERATURE_TIMER_MULTIPLIER)
    {
        unsigned int temperature = tempSensor->getTemperature();
        unsigned int humidity = tempSensor->getHumidity();
        tempProtocol->setTemperature(temperature);
        tempProtocol->setHumidity(humidity);
        tempProtocol->send();
    }
    #endif

    // Handle light sensor
    #ifdef TEMPERATURE_ENABLED
    if(timerMultiplier == LIGHT_TIMER_MULTIPLIER)
    {
        unsigned int lumen = lightSensor->getLuminosity();
        lightProtocol->setLuminosity(lumen);
        lightProtocol->send();
    }
    #endif

}

