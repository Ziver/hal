#ifndef HARDWAREPHOTOCELL_H
#define HARDWAREPHOTOCELL_H


class HardwarePhotocell : public HardwarePowerConsumption
{
public:
    virtual void setup();
    virtual unsigned int getConsumption();
    virtual void reset();

private:
    unsigned int pulse;
}

#endif // HARDWAREPHOTOCELL_H