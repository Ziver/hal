#ifndef HALCONFIGURATION_H
#define HALCONFIGURATION_H

#include "Definitions.h"

#define POLTIME 60*10 // poling in seconds

// POWER CONSUMPTION SENSOR
#define POWERCON_ENABLED // comment out to disable sensor
#define POWERCON_HARDWARE HW_BH1750
#define POWERCON_PROTOCOL PROT_OREGON

// TEMPERATURE SENSOR
#define TEMPERATURE_ENABLED // comment out to disable sensor
#define TEMPERATURE_HARDWARE HW_DHT11
#define TEMPERATURE_PROTOCOL PROT_OREGON
#define TEMPERATURE_POL_MULTIPLE 1 // poling in seconds

// LIGHT SENSOR
#define LIGHT_ENABLED // comment out to disable sensor
#define LIGHT_HARDWARE HW_BHI750
#define LIGHT_PROTOCOL PROT_OREGON
#define LIGHT_POL_MULTIPLE 1 // poling in seconds


#endif // HALCONFIGURATION_H