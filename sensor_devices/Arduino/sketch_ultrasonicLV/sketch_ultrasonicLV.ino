#include <Arduino.h>
#include <XBee.h>
#include <SoftwareSerial.h>
#include "UltrasonicLV.h"
#include "SensorComm.h"

const byte type = 2;
XBeeAddress64 addr64 = XBeeAddress64(0x0013A200, 0x40DAF053);

SensorComm scomm(2, 3, type, &addr64);

UltrasonicLV ultrasonic(7);
long RangeInInches;

void setup()
{
  Serial.begin(9600);
  scomm.begin();
  Serial.println(F("Initialized."));
}

void loop()
{
  long avgrange = 0;
  for (int i = 0; i < 5; i++) {
    ultrasonic.DistanceMeasure();
    RangeInInches = ultrasonic.microsecondsToInches();
    avgrange += RangeInInches;
    delay(65);
  }
  avgrange = avgrange / 5;
  scomm.put(avgrange);
  Serial.println(F("Time: "));
  Serial.println(millis());
  Serial.print(avgrange);//0-255
  Serial.println(F(" in"));
}
