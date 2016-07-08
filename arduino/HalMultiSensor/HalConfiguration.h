#ifndef HALCONFIGURATION_H
#define HALCONFIGURATION_H

//#define ENABLE_DEBUG // comment out to disable debug


#define TIMER_MILLISECOND 60000 // poling in minutes
#define INDICATOR_PIN     13      // diode
#define DEVICE_BASE_ID    20

// POWER CONSUMPTION SENSOR
//#define POWERCON_ENABLED // comment out to disable sensor
#define POWERCON_SENSOR         SensorPhotocell()
#define POWERCON_PROTOCOL       ProtocolOregon(11, DEVICE_BASE_ID + 1)
#define POWER_TIMER_MULTIPLIER  1

// TEMPERATURE SENSOR
#define TEMPERATURE_ENABLED // comment out to disable sensor
#define TEMPERATURE_SENSOR      SensorDHT(DHT11, 10)
#define TEMPERATURE_PROTOCOL    ProtocolOregon(11, DEVICE_BASE_ID + 2)
#define TEMPERATURE_TIMER_MULTIPLIER 10

// LIGHT SENSOR
#define LIGHT_ENABLED // comment out to disable sensor
#define LIGHT_SENSOR            SensorBH1750()
#define LIGHT_PROTOCOL          ProtocolOregon(11, DEVICE_BASE_ID + 3)
#define LIGHT_TIMER_MULTIPLIER  10


#endif // HALCONFIGURATION_H
