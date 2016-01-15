#include <Arduino.h>
#include <XBee.h>
#include <SoftwareSerial.h>
#include "UltrasonicLV.h"
#include "SensorComm.h"
#include "Timer.h"

const byte type = 2;
XBeeAddress64 addr64 = XBeeAddress64(0x0013A200, 0x40DAF053);

SensorComm scomm(2, 3, type, &addr64);

UltrasonicLV ultrasonic(7);
long RangeInInches;
long avgrange = 0;
byte count = 0;

Timer t100 = Timer(100);

void setup()
{
  Serial.begin(9600);
  scomm.begin();
  t100.reset();
  Serial.println(F("Initialized."));
}

void loop()
{
  if (t100.time()) {
    ultrasonic.DistanceMeasure();
    RangeInInches = ultrasonic.microsecondsToInches();
    avgrange += RangeInInches;
    count++;
    if (count >= 5) {
      avgrange = avgrange / 5;
      scomm.put(avgrange);
      Serial.println(F("Time: "));
      Serial.println(millis());
      Serial.print(avgrange);
      Serial.println(F(" in"));
      avgrange = 0;
      count = 0;
    }
  }
}
