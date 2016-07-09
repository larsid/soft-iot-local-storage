# DriverMQTT
MQTT Driver for services and general applications.
--
[![Build Status](https://magnum.travis-ci.com/WiserUFBA/DriverMQTT.svg?token=9bNq5p5MKERJTo9DstR2&branch=master)](https://magnum.travis-ci.com/WiserUFBA/DriverMQTT)

### Introduction
The **DriverMQTT** package is a driver for communications
between computers and devices using the lightweight protocol
**TATU TPI** (**T**hing **P**rotocol for **I**nternet). This driver helps the 
developer in the task of communicates with devices using the 
communication protocol **MQTT**.

### What is MQTT?
**MQTT** is a protocol for Machine to Machine communication and is 
widely used in IoT applications, and recentely as choosed as one of the 
standards for the Internet of Things. However MQTT don't specify methods 
of communication such as **HTTP**, he just specify a way to communicate.

The way that **MQTT** works is replicating messages, and this 
allow a integration on differents ways of communication like **XBEE**,
**ZigBee**,**Ethernet**, **Wi-Fi**, RF and many others.

#### Operation of MQTT
The "MQTT Network" work is easy to undestand and just need to 
things a MQTT Broker (Broker is how the server is called) that 
is the server, responsible for replicate the messages, and a client that 
could be everything that talks **MQTT**. 

The clients who want to communicate just need to connect to a broker, 
when connected he could **PUBLISH** and **SUBSCRIBE** to a topic, when they 
PUBLISH in a topic the message go to the broker and is replicated to 
everyone who is **SUBSCRIBED** to this topic and this is how **MQTT** works, 
replicating messages.

### Why use TATU and the TATU TPI protocol?
The **TATU** project aims to helps the development of the [SmartUFBA](http://wiki.dcc.ufba.br/SmartUFBA) 
projects, proposing standards for the Internet of Things.
The TPI protocol extends MQTT proposing the form of the messages that you 
could send. Protocols are used to specify how to communicate and works 
like a language, imagining how hard is to communicate without a 
"language" the [WiserUFBA Research Group](http://wiser.dcc.ufba.br)  develops the TPI Protocol to be 
fast, lightweight and extremely simple with this characterisct it's easy 
to know why use TPI plus MQTT is so recommended.

### One last question... What is TATU?
**TATU** is the brazillian name of the armadillo that is an small animal 
with a heavy armor, that could be translated to our context as a small 
thing that is secure and so versatible, possibiliting the integration 
with a lot of thigs. **TATU** is an acronym for **T**he **A**cessible **T**hing
**U**niverse.

## Driver MQTT

This driver implement a virtual communication between the service and the 
final device.

When you instantiate a `new DriverMQTT(...params...)` you 
are saying that this object is a virtual representation of the final 
device and with this representation you could control normally the device.

### Features
Some of the features present on this driver:
- Change the value of an actuator
- Read the value of a sensor
- Get properties from the device like the IP
- Edit the properties of the device

### Downloading the class
You have three ways of include this class on your project the first is 
clone this repository and include the `import driver.DriverMQTT;` putting 
or the src or the compiled version (you choose), the second is 
downloading this zip from **[this link](https://wiki.dcc.ufba.br/pub/
SmartUFBA/ProjectLogo/MQTTDriver.zip)** that contains everything needed to 
the driver works, and the last and the beautifull way is adding the dependency
of this project in your POM file in the **Maven** style, for this you could
go for **[this repository](https://github.com/WiserUFBA/wiser-mvn-repo)** and
take a look of how to add the dependencies to your pom, basically you only need
to follow this model.
```xml
<project>
    ...
    <repositories>
        <repository>
            <id>wiser-!TYPE!</id>
            <url>https://github.com/WiserUFBA/wiser-mvn-repo/raw/master/!TYPE!</url>
        </repository>
    </repositories>

    <dependencies>
        ...
        <dependency>
            <groupId>br.ufba.dcc.wiser</groupId>
            <artifactId>drivermqtt</artifactId>
            <version> !VERSION!  </version>
        </dependency>
        ...
    </dependencies>
    ...
</project>

```
Replace !VERSION! with the number version that you want and replace !TYPE!
with the type you want, `releases` for the release and `snapshots` for the
snapshots.

## Configuring and using the Driver MQTT
This driver is implemented using a MQTT Blocking API. When you create a 
new driver is like you are creating a new connection to an specific 
device so we will call this object "device".
So to create a new "device" you just need to do this.
```java
/* A way to construct the connection */
DriverMQTT lamp_device = new DriverMQTT("server_ip",1883,"user","pass","all","lamp");
/* Another variant omitting the error topic and the authentication */
DriverMQTT rfid_device = new DriverMQTT("server_ip", 1883, "rfid");
/* This variant only need the name of the device and is useful if 
   the service and the server is located on the same machine        */
DriverMQTT temp_device = new DriverMQTT("temp");
```
What the first constructor do is create a new connection to the device 
called "lamp" using the brooker located on "localhost" with the port 
"1883" (this is the standard port), authenticating with user:pass and 
publishing errors on the topic "allERROR" (the topic error always end 
with the word "ERROR").

### Controlling the device
The TPI protocol have 4 standard methods they are:
- **GET** : Get some information from the device
- **SET** : Set the value of some variable on the device
- **EDIT** : Edit the properties of the device
- **POST** : Retrieve information of the above methods in JSON format

The methods above have on the maximum 3 variants:
- **ALL** : Only for GET, retrieve all info from the device in JSON
- **STATE** : Modify or receive the logical value of something
- **INFO** : Modify or receive the compost value of something

All the methods of the TPI receive a POST message confirming if the command has worked.

The variants extends the methods of the TPI on this form `<method> <variant> <params>`. 
Example: **GET ALL**.

### Examples

#### Get the state of a temperature sensor
```java
	temp_device.getInfo("temperature");
```

#### Turn on a lamp
```java
	lamp_device.setInfo("lamp", 1);
```

#### Edit the IP of the device
```java
	device.editInfo("IP","192.168.0.1");
```

#### Get all info from a device
```java
	device.getAll();
```

### Some observation about this driver
For this driver work properly it's recomended that you use
the TPI Interpreter module available in our organization repositories. It's important
understand the values that you are sending too, for example editState needs a String
that correspond the property and the value need to be an int value, the same occurs on
the setInfo that receive the "lamp" string and the number 1 which represents ON.

## Changelog

- V-1.0 : Created this project, all of the features listed above in a blocking style.
- V-1.1 : Corrected some issues, include support for java 1.7, 
          migration to maven started.
- V-1.2 : All the project migrated to maven, no more issues with libs,
          package this library in a service is easy to do.

--
<p align="center">
	Developed by </br>
  <img src="https://wiki.dcc.ufba.br/pub/SmartUFBA/ProjectLogo/wiserufbalogo.jpg"/>
</p>
