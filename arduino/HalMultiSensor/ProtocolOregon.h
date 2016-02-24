#ifndef PROTOCOLOREGON_H
#define PROTOCOLOREGON_H


class ProtocolOregon : public ProtocolTemperature : public ProtocolPower
{
public:
    ProtocolOregon(unsigned char address) : address(address){}

    virtual void setup();
    virtual void setTemperature(float temp);
    virtual void setHumidity(unsigned char humidity);
    virtual void setConsumption(unsigned int cons); //Power
    virtual void send();
private:
    unsigned char address;
    unsigned float temperature;
    unsigned char humidity;
};

#endif // PROTOCOLOREGON_H