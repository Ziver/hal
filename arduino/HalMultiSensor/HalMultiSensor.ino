/*
A interrupt based sensor device that reads multiple sensors and transmits
the data to a central location.
*/
#include <Arduino.h>

#include "HalConfiguration.h"
#include "HalInterfaces.h"
#include "HalInclude.h"
#include "Interrupt.h"


#ifndef POWERCON_ENABLED
    #define POWER_TIMER_MULTIPLIER 1
#endif
#ifndef TEMPERATURE_ENABLED
    #define TEMPERATURE_TIMER_MULTIPLIER 1
#endif
#ifndef LIGHT_ENABLED
    #define LIGHT_TIMER_MULTIPLIER 1
#endif
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
    pinMode(INDICATOR_PIN, OUTPUT);

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

    DEBUG("Setup SLEEP_INTERRUPT");
    Interrupt::setWatchDogCallback(timerInterruptFunc);
    Interrupt::setupWatchDogInterrupt(TIMER_MILLISECOND); // one minute scheduled interrupt


    pulse(INDICATOR_PIN, 3);
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
    digitalWrite(INDICATOR_PIN, HIGH);

    // Send power consumption
    #ifdef POWERCON_ENABLED
    if (timerMultiplier % POWER_TIMER_MULTIPLIER == 0)
    {
        static PowerData powerData;
        powerSensor->read(powerData); // not needed, only here for future use
        DEBUGF("Read POWERCON_SENSOR= consumption:%d", powerData.consumption);
        powerProtocol->send(powerData);
    }
    #endif

    // Handle temperature sensor
    #ifdef TEMPERATURE_ENABLED
    if (timerMultiplier % TEMPERATURE_TIMER_MULTIPLIER == 0)
    {
        static TemperatureData tempData;
        tempSensor->read(tempData);
        DEBUGF("Read TEMPERATURE_SENSOR= temperature:%d, humidity:%d", (int)tempData.temperature, (int)tempData.humidity);
        tempProtocol->send(tempData);
    }
    #endif

    // Handle light sensor
    #ifdef LIGHT_ENABLED
    if (timerMultiplier % LIGHT_TIMER_MULTIPLIER == 0)
    {
        static LightData lightData;
        lightSensor->read(lightData);
        DEBUGF("Read LIGHT_SENSOR= lumen:%d", lightData.lumen);
        lightProtocol->send(lightData);
    }
    #endif

    digitalWrite(INDICATOR_PIN, LOW);

    DEBUG("Sleeping");
    Interrupt::sleep();
    DEBUG("Wakeup");
}

