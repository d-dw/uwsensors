#ifndef TIMER_H
#define TIMER_H

class Timer {
  public:
    Timer(unsigned long delay);
    bool time();
    void reset();
  private:
    unsigned long _delay;
    unsigned long _t0;
};

#endif
