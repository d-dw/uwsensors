# UW HCDE Spatial Sensors

Research Project within University of Washington Human-Centered Design and Engineering Department (HCDE)

The uwsensors project is a platform for collecting and analyzing spatial data using sensors powered by Arduino-compatible microcontrollers and 802.15.4 wireless radios.

The initial goal for this project is to help small-business owners collect spatial analytics and insights based on foot traffic.

## Repository

All embedded sensor code and gateway code is currently organized in this single repository. 

* router: Router contains code for the central coodinating gateway (in Java) and initial processing pipeline (in Python). All TCP networking is implemented using ZeroMQ. Current deployment target is Raspberry Pi 2.
* sensor_devices: Sensor Devices contains code for the various types of individual sensors (in C++).

## Wiring Diagrams and Radio Configuration

* TODO

## Compilation and Installation

* TODO
