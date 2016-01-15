#include <Arduino.h>
#include "Timer.h"

Timer::Timer(unsigned long delay) {
  _delay = delay;
  reset();
}

bool Timer::time() {
  return (millis() - _t0) >= _delay;
}

void Timer::reset() {
  _t0 = millis();
}
