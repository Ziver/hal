

public void pinInterrupt()
{
    ++pulse;
}


public HardwarePhotocell::setup()
{
    pinInterrupt = InterruptUtil(pinInterrupt);
    pinInterrupt.setupPinInterrupt(PC2); //PC3
}

public unsigned int HardwarePhotocell::getConsumption()
{
    return pulse;
}

public unsigned int HardwarePhotocell::reset()
{
    pulse = 0;
}