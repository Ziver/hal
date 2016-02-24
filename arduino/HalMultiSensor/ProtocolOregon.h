#ifndef PROTOCOLOREGON_H
#define PROTOCOLOREGON_H

#include <Arduino.h>
#include "HalInterfaces.h"


class ProtocolOregon : public ProtocolTemperature, public ProtocolPowerConsumption
{
public:
    ProtocolOregon(unsigned char address) : address(address){};

    virtual void setup();
    virtual void setTemperature(float temp);
    virtual void setHumidity(unsigned char humidity);
    virtual void setConsumption(unsigned int cons); //Power
    virtual void send();
private:
    unsigned char address;
    float temperature;
    unsigned char humidity;
};

#endif // PROTOCOLOREGON_H
