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
    virtual int getConsumption() = 0;
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
    virtual int getLuminosity() = 0;
};



class Protocol
{
public:
    virtual void setup() = 0;
    virtual void send() = 0;
};

#endif // HALINTERFACES_H