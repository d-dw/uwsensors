#include <Arduino.h>
#include "UltrasonicLV.h"

UltrasonicLV::UltrasonicLV(int pin)
{
  _pin = pin;
  pinMode(_pin, INPUT);
}

void UltrasonicLV::DistanceMeasure(void)
{
  duration = pulseIn(_pin, HIGH);
}

long UltrasonicLV::microsecondsToCentimeters(void)
{
  return duration/57.87;
}

long UltrasonicLV::microsecondsToInches(void)
{
  return duration/147; 
}
