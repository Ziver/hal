# Hal

Hal is a home automation hub with sensor statistics with the functionality to 
share that data between friends. It is developed to be very extensible so future 
Sensors and other input devices can be supported.
 
Currently supported devices:
- **Network Scanner**, IP scanner to detect devices on local network
- **NUT**, Linux UPS daemon
- **Tellstick**, Supported devices:
    - NexaSelfLearning
    - Oregon0x1A2D
- **Raspberry Pi**, GPIO connected sensors


The project is currently in alpha state, and as such things will change and break.

### Screenshots
![](screenshot_01.jpg)

![](screenshot_02.jpg)

![](screenshot_03.jpg)

![](screenshot_04.jpg)

## Installing

To run the Hal server you first need to clone the git repository and then run the 
ant command to build and run:

```
ant run
```

Check `hal.conf.example` for available configuration options.

## Running the tests

The current test coverage is greatly lacking, but to run the available JUnit 
test-cases run:

```
ant test
```

## Authors

* **Daniel Collin**
* **Ziver Koc**


## License

This project is licensed under the MIT License - see the 
[LICENSE.txt](LICENSE.txt) file for details

## Acknowledgments

* Tellstick, for open-sourcing their code 
