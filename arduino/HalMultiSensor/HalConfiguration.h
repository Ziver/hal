#ifndef HALCONFIGURATION_H
#define HALCONFIGURATION_H

#include "Definitions.h"


// POWER CONSUMPTION SENSOR
#define POWERCON_ENABLED // comment out to disable sensor
#define POWERCON_HARDWARE HW_BH1750
#define POWERCON_PROTOCOL PROT_OREGON

// TEMPERATURE SENSOR
#define TEMPERATURE_ENABLED // comment out to disable sensor
#define TEMPERATURE_HARDWARE HW_DHT11
#define TEMPERATURE_PROTOCOL PROT_OREGON
#define TEMPERATURE_TIMER_MULTIPLIER 1 // poling in minutes

// LIGHT SENSOR
#define LIGHT_ENABLED // comment out to disable sensor
#define LIGHT_HARDWARE HW_BHI750
#define LIGHT_PROTOCOL PROT_OREGON
#define LIGHT_TIMER_MULTIPLIER 1 // poling in minutes


#endif // HALCONFIGURATION_H