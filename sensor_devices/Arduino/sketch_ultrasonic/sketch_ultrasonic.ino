#include <Arduino.h>
#include <XBee.h>
#include <SoftwareSerial.h>
#include "Ultrasonic.h"
#include "SensorComm.h"

const byte type = 1;
XBeeAddress64 addr64 = XBeeAddress64(0x0013A200, 0x4063AF4F);

SensorComm scomm(2, 3, type, &addr64);

Ultrasonic ultrasonic(7);
long RangeInCentimeters;

void setup()
{
  Serial.begin(9600);
  scomm.begin();
  Serial.println(F("Initialized."));
}

void loop()
{
  long RangeInCentimeters;
  ultrasonic.DistanceMeasure();// get the current signal time;
  RangeInCentimeters = ultrasonic.microsecondsToCentimeters();//convert the time to centimeters
  scomm.put(RangeInCentimeters);
  //Serial.println(F("Time: "));
  //Serial.println(millis());
  //Serial.print(RangeInCentimeters);//0~400cm
  //Serial.println(F(" cm"));
  delay(500);
}
