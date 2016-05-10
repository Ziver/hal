#ifndef HALCONFIGURATION_H
#define HALCONFIGURATION_H

#define ENABLE_DEBUG // comment out to disable debug

#define TIMER_MILLISECOND 60*1000 // poling in minutes
#define INDICATOR_PIN     13      // diod

// POWER CONSUMPTION SENSOR
#define POWERCON_ENABLED // comment out to disable sensor
#define POWERCON_SENSOR         SensorBH1750()
#define POWERCON_PROTOCOL       ProtocolOregon(11, 118)
#define POWER_TIMER_MULTIPLIER  1

// TEMPERATURE SENSOR
#define TEMPERATURE_ENABLED // comment out to disable sensor
#define TEMPERATURE_SENSOR      SensorDHT11(10)
#define TEMPERATURE_PROTOCOL    ProtocolOregon(11, 100)
#define TEMPERATURE_TIMER_MULTIPLIER 1

// LIGHT SENSOR
//#define LIGHT_ENABLED // comment out to disable sensor
#define LIGHT_SENSOR            SensorDH1750()
#define LIGHT_PROTOCOL          ?
#define LIGHT_TIMER_MULTIPLIER  1


#endif // HALCONFIGURATION_H
