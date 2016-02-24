/*
A interrupt based sensor device that reads multiple sensors and transmits
the data to a central location.
*/

InterruptUtil timerInterrupt;

unsigned int timerMultiplierMAX;
unsigned int timerMultiplier = 0;

// Sensors
HardwarePowerConsumption powerSensor;
HardwareTemperature tempSensor;
HardwareLight lightSensor;

// Protocols
HardwarePowerConsumption powerProtocol;
HardwareTemperature tempProtocol;
HardwareLight lightProtocol;



void timerInterrupt();

void setup()
{
    timerMultipleMAX = POWER_TIMER_MULTIPLIER * TEMPERATURE_TIMER_MULTIPLIER * LIGHT_TIMER_MULTIPLIER; // Find a lowest common denominator
    pinInterrupt = InterruptUtil(timerInterrupt);
    pinInterrupt.setupTimerInterrupt(60*1000); // one minute scheduled interrupt
    
    // Setup Sensors and protocols
    #ifdef POWERCON_ENABLED
    powerSensor = POWERCON_HARDWARE;
    powerSensor.setup();
    powerProtocol = POWERCON_PROTOCOL;
    powerProtocol.setup();
    #endif

    #ifdef TEMPERATURE_ENABLED
    tempSensor = TEMPERATURE_HARDWARE;
    tempSensor.setup();
    tempProtocol = TEMPERATURE_PROTOCOL;
    tempProtocol.setup();
    #endif

    #ifdef LIGHT_ENABLED
    lightSensor = LIGHT_HARDWARE;
    lightSensor.setup();
    lightProtocol = LIGHT_PROTOCOL;
    lightProtocol.setup();
    #endif
}


void loop() {}


void timerInterrupt()
{
    ++timerMultiplier;
    if (timerMultiplier > timerMultiplierMAX)
        timerMultiplier = 1;

    // Send power consumption
    #ifdef POWERCON_ENABLED
    if(timerMultiplier == POWER_TIMER_MULTIPLIER)
    {
        unsigned int consumption = powerSensor.getConsumption();
        powerSensor.reset();
        powerProtocol.setConsumption(consumption);
        powerProtocol.send();
    }
    #endif

    // Handle temperature sensor
    #ifdef TEMPERATURE_ENABLED
    if(timerMultiplier == TEMPERATURE_TIMER_MULTIPLIER)
    {
        unsigned int temperature = tempSensor.getTemperature();
        unsigned int humidity = tempSensor.getHumidity();
        tempProtocol.setTemperature(temperature);
        tempProtocol.setHumidity(humidity);
        tempProtocol.send();
    }
    #endif

    // Handle light sensor
    #ifdef TEMPERATURE_ENABLED
    if(timerMultiplier == LIGHT_TIMER_MULTIPLIER)
    {
        unsigned int lumen = lightSensor.getLuminosity();
        lightProtocol.setLuminosity(lumen);
        lightProtocol.send();
    }
    #endif

}

