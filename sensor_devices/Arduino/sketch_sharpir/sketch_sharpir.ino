#include <Arduino.h>
#include <XBee.h>
#include <SoftwareSerial.h>
// Requires SharpIR external library
#include <SharpIR.h>
#include "SensorComm.h"
#include "Timer.h"

#define SENSOR_POWER_PIN 8

XBeeAddress64 addr64 = XBeeAddress64(0x0013A200, 0x40DAF053);

SensorComm scomm(2, 3, TYPE_IR_SHARP, &addr64);

SharpIR SharpIR(A0, 20150);
long rangemm;
long avgrange = 0;
byte count = 0;

void setup()
{
  Serial.begin(9600);
  scomm.begin();
  t100.reset();
  pinMode(SENSOR_POWER_PIN, OUTPUT);
  digitalWrite(SENSOR_POWER_PIN, HIGH);
  Serial.println(F("Initialized."));
}

void loop()
{
  if (t100.time()) {
    rangemm = SharpIR.distance();
    avgrange += rangemm;
    count++;
    if (count >= 5) {
      avgrange = avgrange / 5;
      scomm.put(avgrange);
      Serial.println(F("Time: "));
      Serial.println(millis());
      Serial.print(avgrange);
      Serial.println(F(" mm"));
      if (scomm.getCommandAndClear() == COMMAND_SLEEP) {
        // Turn off sensor
        digitalWrite(SENSOR_POWER_PIN, LOW);
        // Delay for CommandParam1 milliseconds
        delay(scomm.getCommandParam1());
        // Turn on sensor
        digitalWrite(SENSOR_POWER_PIN, HIGH);
      }
      avgrange = 0;
      count = 0;
    }
  }
}
