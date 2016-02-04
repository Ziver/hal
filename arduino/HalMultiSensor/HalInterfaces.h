#ifndef HALINTERFACES_H
#define HALINTERFACES_H

class HardwarePowerConsumption
{
public:
    virtual void setup() = 0;
    virtual int getConsumption() = 0;
}

class HardwareTemperature
{
public:
    virtual void setup() = 0;
    virtual int getTemperature() = 0;
    virtual int getHumidity() = 0;
}

class HardwareLight
{
public:
    virtual void setup() = 0;
    virtual int getLuminosity() = 0;
}

class HardwareInterrupt
{
public:
    virtual void interrupt(bool enable) = 0;
}


class Protocol
{
public:
    virtual void setup() = 0;
    virtual void send() = 0;
}

#endif // HALINTERFACES_H