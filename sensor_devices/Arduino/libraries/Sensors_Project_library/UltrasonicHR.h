#ifndef ULTRASONICHR_H
#define ULTRASONICHR_H

class UltrasonicHR
{
  public:
    UltrasonicHR(int pin);
    void DistanceMeasure(void);
    long microsecondsToCentimeters(void);
    long microsecondsToInches(void);
  private:
    int _pin;//pin number of Arduino that is connected with SIG pin of Ultrasonic Ranger.
    long duration;// the Pulse time received;
};

#endif
