#include <Arduino.h>
#include <XBee.h>
#include <SoftwareSerial.h>
// Requires SharpIR external library
#include <SharpIR.h>
#include "SensorComm.h"

const byte type = 3;
XBeeAddress64 addr64 = XBeeAddress64(0x0013A200, 0x40DAF053);

SensorComm scomm(2, 3, type, &addr64);

SharpIR SharpIR(A0, 20150);
long rangemm;

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
    rangemm = SharpIR.distance();
    avgrange += rangemm;
    delay(96);
  }
  avgrange = avgrange / 5;
  scomm.put(avgrange);
  Serial.println(F("Time: "));
  Serial.println(millis());
  Serial.print(avgrange);//0-1500
  Serial.println(F(" mm"));
}
