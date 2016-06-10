#ifndef HALCONFIGURATION_H
#define HALCONFIGURATION_H

//#define ENABLE_DEBUG // comment out to disable debug

#define TIMER_MILLISECOND 60*1000 // poling in minutes
#define INDICATOR_PIN     13      // diode


// POWER CONSUMPTION SENSOR
//#define POWERCON_ENABLED // comment out to disable sensor
#define POWERCON_SENSOR         SensorPhotocell()
#define POWERCON_PROTOCOL       ProtocolOregon(11, 186)
#define POWER_TIMER_MULTIPLIER  1

// TEMPERATURE SENSOR
#define TEMPERATURE_ENABLED // comment out to disable sensor
#define TEMPERATURE_SENSOR      SensorDHT(DHT22, 10)
#define TEMPERATURE_PROTOCOL    ProtocolOregon(11, 100)
#define TEMPERATURE_TIMER_MULTIPLIER 10

// LIGHT SENSOR
//#define LIGHT_ENABLED // comment out to disable sensor
#define LIGHT_SENSOR            SensorDH1750()
#define LIGHT_PROTOCOL          ?
#define LIGHT_TIMER_MULTIPLIER  10


#endif // HALCONFIGURATION_H
