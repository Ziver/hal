#ifndef HALINTERFACES_H
#define HALINTERFACES_H


class Hardware
{
public:
    virtual void setup() = 0;
};

class HardwarePowerConsumption : public Hardware
{
public:
    // returns number of pulses from power meter
    virtual unsigned int getConsumption() = 0;
    virtual void reset() = 0;
};

class HardwareTemperature : public Hardware
{
public:
    virtual int getTemperature() = 0;
    virtual int getHumidity() = 0;
};

class HardwareLight : public Hardware
{
public:
    virtual void setup() = 0;
    virtual unsigned int getLuminosity() = 0;
};



class Protocol
{
public:
    virtual void setup() = 0;
    virtual void send() = 0;
};

class ProtocolPowerConsumption : public Protocol
{
public:
    virtual void setConsumption(unsigned int cons) = 0;
};

class ProtocolTemperature : public Protocol
{
public:
    virtual void setTemperature(float temp) = 0;
    virtual void setHumidity(unsigned char humidity) = 0;
};

class ProtocolLight : public Protocol
{
public:
    virtual void setLuminosity(int lumen) = 0;
};

#endif // HALINTERFACES_H
