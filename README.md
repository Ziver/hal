# Hal

Hal is a home automation hub with sensor statistics with the functionality to share that data between friends. 
It is developed to be very extensible so future Sensors and other input devices can be supported.
 
Currently supported devices:
- **Tellstick**
    - NexaSelfLearning
    - Oregon0x1A2D
- **Raspberry Pi**, GPIO connected sensors


The project is currently in alpha state, as such things will change and break.

### Screenshots
![](screenshot_01.jpg)

![](screenshot_02.jpg)

![](screenshot_03.jpg)

![](screenshot_04.jpg)

## Installing

A step by step series of examples that tell you have to get a development env running

Stay what the step will be

```
ant run
```

Check `hal.conf.example` for available configuration options.

## Running the tests

The current test coverage is greatly lacking, but to run the JUnit test cases run:

```
ant test
```

## Authors

* **Daniel Collin**
* **Ziver Koc**


## License

This project is licensed under the MIT License - see the [LICENSE.txt](LICENSE.txt) file for details

## Acknowledgments

* Tellstick, for open-sourcing their code 
