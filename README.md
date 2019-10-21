## Flight Controller for Raspberry PI

I was wondering if a Raspberry PI Zero might be sufficient to manage a RC airplane. After all, it supports
WIFI so that we can control the plane remotely and we can receive live images back and it has all the functionality
to connect up to Sensors and control motors via PWM.

One of the major topics related to flight controllers - apart from controlling the flight parameters - is configuration: 

- Which sensors do we have?
- how do they communicate? 
- What is the setup of the plane?

etc. 


## Project Goals

In a first step I wanted to prototype a flexible solution where I can 
- run some **Software Simulation** (Integration with JSBSim
- do the input via a **Game Console**
- support of **Configuration Changes** w/o the need to recompile
- **Monitor** the state of the airplane 
- support different **Flight Modes** 
- play around with different **Alternative Implementations** 

I came to the conclusion that **Java** with it's infrastructure can help me to come up with a good
working solution in just 1 or 2 days.

## Overall Software Design 

- There is no GUI logic needed because we can use **JMX to monitor and update** the key control variables (JMXParameterStore). 
- **Spring** can be used to **configure the classes** and provide specific instances of objects. We can easily 
  adapt any settings and there is no need for hard coded values or object instances.
- The overall Class Design is very simple: The **FlightController** is the heart of all processing. It also
  contains the control loop. It is used to manage the following:
  - **ParameterStore**: We represent the actual parameter values (and potentially some history). 
  - **Devices** (IDevice) consist of 
  	- **Input Sensors** (ISensor) which can be configured to update the Parameter Store. We also need to configure
  	  the input protocol (I2C or SPI). The GameConsole is also implemented as input device.
  	- **OutDevices** (Aileron, Elevator, Rudder, Throttle) where we need to configure the Output Protocol (
  	  Pin and PWM settings)
  	- **Other Devices** can be used to implement additional functionality. 
  - **FlightMode **: implements the Parameter update logic from the input devices to the output devices.
  - The **control loop** is just pulling input and then calling the output with a timer.

For my standard configuration you can have a look at the Spring  **[config.xml](src/main/resources/config.xml)**

## External Dependencies (Java Libraries)

I tried to minimize the use of external dependencies to the following:

- [pi4j](https://pi4j.com/1.2/index.html) -> IO Control for PI  
- [Jamepad](https://github.com/williamahartman/Jamepad) -> Gamepad support 
- [spring-context](https://docs.spring.io/spring-framework/docs/current/spring-framework-reference/core.html) -> Configuration and Dependency Injection
- [logback](http://logback.qos.ch/) -> Logging
- [junit](https://junit.org/junit4/) -> Unit tests


## Performance Considerations

[Navio2](https://emlid.com/navio/) provides some specific hardware to turn a Raspberry PI into a full blown
flight controller and the PI Zero is explicitly not supported because it is too slow.  

It is my expectation that, though it might be too slow to fly multicopters, it should still be fast enough - even without 
the Navio3 hardware - to fly a model airplanes: In **real life, airplanes are flown by humans** and a Raspberry
PI Zero is definitely able to react quicker.

## Hardware  

- Raspberry PI Zero - 10 USD
- CJMCU 10DOF 9 Axis MPU9250 + BMP180 Sensor Module Gyro Acceleration + Barometric Height Sensor - 8 USD
- Servos for Aileron, Rudder and Elevator
- Brushless Motor & ESC
- Foam Airplane (from scrap)


While I am waiting for the hardware to arrive, I go ahead with the implementation of the Software

## Next Steps  

Additional topics can be found in 

- [Basic Configuration](https://github.com/pschatzmann/JFlightController4Pi/edit/master/help/01 Configuration.md)
- [Setting up Manual Mode](https://github.com/pschatzmann/JFlightController4Pi/edit/master/help/02 ModeManual.md)
- [Setting up a Remote Control](https://github.com/pschatzmann/JFlightController4Pi/edit/master/help/03 RemoteControl.md)
- [Using Flightgear](https://github.com/pschatzmann/JFlightController4Pi/edit/master/help/04 Flightgear.md)
- [Setting up Stabilized Mode](https://github.com/pschatzmann/JFlightController4Pi/edit/master/help/05 ModeStabilizedControl)
- [Setting up Stabilized Mode Sensors](https://github.com/pschatzmann/JFlightController4Pi/edit/master/help/06 ModeStabilizedSensors)

