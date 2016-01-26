#include <Arduino.h>
#include "Timer.h"

Timer::Timer(unsigned long delay) {
  _delay = delay;
  reset();
}

bool Timer::time() {
  unsigned long now = millis();
  if (now - _t0 >= _delay) {
    _t0 = now;
    return true;
  }
  return false;
}

void Timer::reset() {
  _t0 = millis();
}
