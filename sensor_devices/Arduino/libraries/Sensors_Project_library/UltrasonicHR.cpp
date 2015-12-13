#include <Arduino.h>
#include "UltrasonicHR.h"

UltrasonicHR::UltrasonicHR(int pin)
{
  _pin = pin;
  pinMode(_pin, INPUT);
}

void UltrasonicHR::DistanceMeasure(void)
{
  duration = pulseIn(_pin, HIGH);
}

long UltrasonicHR::microsecondsToCentimeters(void)
{
  return duration/10;
}

long UltrasonicHR::microsecondsToInches(void)
{
  return duration/25.4; 
}
