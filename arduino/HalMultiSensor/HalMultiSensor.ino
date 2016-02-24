/*
A interrupt based sensor device that reads multiple sensors and transmits
the data to a central location.
*/

InterruptUtil timerInterrupt;

unsigned int timerMultiplierMAX;
unsigned int timerMultiplier = 0;

// Sensors
HardwarePowerConsumption powerSensor;
HardwareTemperature temperatureSensor;
HardwareLight lightSensor;

// Protocols
HardwarePowerConsumption powerProtocol;
HardwareTemperature temperatureProtocol;
HardwareLight lightProtocol;



void timerInterrupt()
{
    ++timerMultiplier;
    if (timerMultiplier > timerMultiplierMAX)
        timerMultiplier = 1;

    // Send power consumption
    #ifndef POWERCON_ENABLED
    unsigned int consumption = powerSensor.getConsumption();
    powerSensor.reset();
    powerProtocol.setConsumption(consumption);
    powerProtocol.send();
    #endif

    // Handle temperature sensor
    #ifndef TEMPERATURE_ENABLED
    if(timerMultiplier == TEMPERATURE_TIMER_MULTIPLIER)
    {
        unsigned int temperature = temperatureSensor.getTemperature();
        unsigned int humidity = temperatureSensor.getHumidity();
        temperatureProtocol.setTemperature(temperature);
        temperatureProtocol.setHumidity(humidity);
        temperatureProtocol.send();
    }
    #endif

    // Handle light sensor
    #ifndef TEMPERATURE_ENABLED
    if(timerMultiplier == LIGHT_TIMER_MULTIPLIER)
    {
        unsigned int lumen = lightSensor.getLuminosity();
        lightProtocol.setLuminosity(lumen);
        lightProtocol.send();
    }
    #endif

}

void setup()
{
    timerMultipleMAX = LIGHT_TIMER_MULTIPLIER * TEMPERATURE_TIMER_MULTIPLIER; // Find a lowest common denominator
    pinInterrupt = InterruptUtil(timerInterrupt);
    pinInterrupt.setupTimerInterrupt(60*1000); // one minute scheduled interrupt
}


void loop() {}

