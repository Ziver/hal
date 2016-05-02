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

    void setType(byte *data, byte b1, byte b2);
    void setChannel(byte *data, byte channel);
    void setId(byte *data, byte id);
    void setBatteryLevel(byte *data, bool level);
    void setTemperature(byte *data, float temp);
    void setHumidity(byte* data, byte hum);
    void calculateAndSetChecksum(byte* data);

    void sendZero(void);
    void sendOne(void);
    void sendData(byte *data, byte length);
    void rfSend(byte *data, byte size);
};

#endif // PROTOCOLOREGON_H
