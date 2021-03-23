#ifndef HALCONFIGURATION_H
#define HALCONFIGURATION_H

//#define ENABLE_DEBUG // comment out to disable debug


#define TIMER_MILLISECOND 60000 // poling in minutes
#define INDICATOR_PIN     13      // diode
#define TX_PIN            11
#define DEVICE_BASE_ID    99

// POWER CONSUMPTION SENSOR
//#define POWERCON_ENABLED // comment out to disable sensor
#define POWERCON_SENSOR         SensorPhotocell()
#define POWERCON_PROTOCOL       ProtocolOregon(TX_PIN, DEVICE_BASE_ID + 0)
#define POWER_TIMER_MULTIPLIER  1

// TEMPERATURE SENSOR
#define TEMPERATURE_ENABLED // comment out to disable sensor
#define TEMPERATURE_SENSOR      SensorDHT(DHT11, 10)
#define TEMPERATURE_PROTOCOL    ProtocolOregon(TX_PIN, DEVICE_BASE_ID + 1)
#define TEMPERATURE_TIMER_MULTIPLIER 10

// LIGHT SENSOR
//#define LIGHT_ENABLED // comment out to disable sensor
#define LIGHT_SENSOR            SensorBH1750()
#define LIGHT_PROTOCOL          ProtocolOregon(TX_PIN, DEVICE_BASE_ID + 2)
#define LIGHT_TIMER_MULTIPLIER  10


#endif // HALCONFIGURATION_H
