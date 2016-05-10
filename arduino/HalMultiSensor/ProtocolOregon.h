#ifndef PROTOCOLOREGON_H
#define PROTOCOLOREGON_H

#include <Arduino.h>
#include "HalInterfaces.h"


class ProtocolOregon : public ProtocolTemperature, public ProtocolPowerConsumption
{
public:
    ProtocolOregon(short pin, unsigned char address) : txPin(pin), address(address){};

    virtual void setup();
    virtual void send(const TemperatureData& data);
    virtual void send(const PowerData& data);

private:
    short txPin;
    unsigned char address;

    void send(float temperature, short humidity);
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
