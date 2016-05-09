/*
A interrupt based sensor device that reads multiple sensors and transmits
the data to a central location.
*/
#include <Arduino.h>
#include <Wire.h>

#include "HalConfiguration.h"
#include "HalInterfaces.h"
#include "HalInclude.h"
#include "Interrupt.h"


#define TIMER_MULTIPLIER_MAX \
    POWER_TIMER_MULTIPLIER * TEMPERATURE_TIMER_MULTIPLIER * LIGHT_TIMER_MULTIPLIER
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
    #ifdef ENABLE_DEBUG
    Serial.begin(9600);
    #endif

    // Setup Sensors and protocols
    #ifdef POWERCON_ENABLED
    DEBUG("Setup POWERCON_SENSOR");
    powerSensor = new POWERCON_SENSOR;
    powerSensor->setup();
    powerProtocol = new POWERCON_PROTOCOL;
    powerProtocol->setup();
    #endif

    #ifdef TEMPERATURE_ENABLED
    DEBUG("Setup TEMPERATURE_SENSOR");
    tempSensor = new TEMPERATURE_SENSOR;
    tempSensor->setup();
    tempProtocol = new TEMPERATURE_PROTOCOL;
    tempProtocol->setup();
    #endif

    #ifdef LIGHT_ENABLED
    DEBUG("Setup LIGHT_SENSOR");
    lightSensor = new LIGHT_SENSOR;
    lightSensor->setup();
    lightProtocol = new LIGHT_PROTOCOL;
    lightProtocol->setup();
    #endif

    DEBUG("Setup INTERRUPT");
    Interrupt::setCallback(timerInterruptFunc);
    Interrupt::setupWatchDogInterrupt(TIMER_MILLISECOND); // one minute scheduled interrupt

    DEBUG("Ready");
}


void timerInterruptFunc()
{
    ++timerMultiplier;
    if (timerMultiplier > TIMER_MULTIPLIER_MAX)
        timerMultiplier = 1;
}

void loop()
{
    noInterrupts();

    // Send power consumption
    #ifdef POWERCON_ENABLED
    if(timerMultiplier == POWER_TIMER_MULTIPLIER)
    {
        unsigned int consumption = powerSensor->getConsumption();
        DEBUGF("Read POWERCON_SENSOR= consumption:%d", consumption);
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
        DEBUGF("Read TEMPERATURE_SENSOR= temperature:%d, humidity:%d", temperature, humidity);
        tempProtocol->setTemperature(temperature);
        tempProtocol->setHumidity(humidity);
        tempProtocol->send();
    }
    #endif

    // Handle light sensor
    #ifdef LIGHT_ENABLED
    if(timerMultiplier == LIGHT_TIMER_MULTIPLIER)
    {
        unsigned int lumen = lightSensor->getLuminosity();
        DEBUG("Read LIGHT_SENSOR= lumen:%d", lumen);
        lightProtocol->setLuminosity(lumen);
        lightProtocol->send();
    }
    #endif

    interrupts();

    DEBUG("Sleeping");
    Interrupt::sleep();
    DEBUG("Wakeup");
}

