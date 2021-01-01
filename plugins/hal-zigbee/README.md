# Plugin Configuration

|Config Parameter |Value                        |Description |
|-----------------|-----------------------------|------------|
|zwave.com_port   |Name or location of com port |The port where radio dongle is connected|
|zigbee.pan_id    |Integer |The PAN ID for the radio module|

# Hardware

You need to buy the following items:

1. http://www.ti.com/tool/cc2531emk
   * Example item:
     https://www.aliexpress.com/item/4001091370032.html
2. http://www.ti.com/tool/cc-debugger
    * Example kit:
      https://www.aliexpress.com/item/4001095299084.html

## Flashing Dongle
Instructions are originally from: https://www.zigbee2mqtt.io/information/flashing_the_cc2531.html

### Windows
1. Download and install the "SmartRF Flash Programmer" **(Not v2)** application from https://www.ti.com/tool/FLASH-PROGRAMMER


2. Connect CC debugger to the CC2531 USB sniffer.
3. Connect **BOTH** the CC2531 USB sniffer and the CC debugger to your PC using USB. 
   * If the light on the CC debugger is RED press set reset button on the CC debugger. The light on the CC debugger should now turn GREEN. If not use [CC debugger user guide](http://www.ti.com/lit/ug/swru197h/swru197h.pdf) to troubleshoot your problem.
4. Download the firmware [CC2531_DEFAULT_20190608.zip](https://github.com/Koenkk/Z-Stack-firmware/raw/master/coordinator/Z-Stack_Home_1.2/bin/default/CC2531_DEFAULT_20190608.zip)
4. Start SmartRF Flash Programmer, with the properties below:
    * Flash image: Make sure to select the .hex file and not the .bin file.
    * Uncheck "Retain IEEE address when reprogramming the chip.

### Linux or MacOS

1. Install prerequisites for CC-Tool using a package manager (e.g. Homebrew for macOS)
    * Ubuntu/Debian: libusb-1.0-0-dev, libboost-all-dev, autoconf, libtool
    * Fedora: dh-autoreconf, boost-devel, libusb1-devel, gcc-c++
    * Archlinux: dh-autoreconf, libusb, boost
    * macOS: brew install autoconf automake libusb boost pkgconfig libtool
    * Raspbian: dh-autoreconf, libusb-1.0-0-dev, libboost-all-dev

2. Build cc-tool
   
        git clone https://github.com/dashesy/cc-tool.git
        cd cc-tool
        ./bootstrap
        ./configure
        make

3. Connect **BOTH** the CC2531 USB sniffer and the CC debugger to your PC using USB.
    * If the light on the CC debugger is RED press set reset button on the CC debugger. The light on the CC debugger should now turn GREEN. If not use [CC debugger user guide](http://www.ti.com/lit/ug/swru197h/swru197h.pdf) to troubleshoot your problem.
4. Download the firmware [CC2531_DEFAULT_20190608.zip](https://github.com/Koenkk/Z-Stack-firmware/raw/master/coordinator/Z-Stack_Home_1.2/bin/default/CC2531_DEFAULT_20190608.zip)
5. Flash your firmware:

        sudo ./cc-tool -e -w CC2531ZNP-Prod.hex

