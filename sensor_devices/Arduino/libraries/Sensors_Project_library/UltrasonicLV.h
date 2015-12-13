#ifndef ULTRASONICLV_H
#define ULTRASONICLV_H

class UltrasonicLV
{
  public:
    UltrasonicLV(int pin);
    void DistanceMeasure(void);
    long microsecondsToCentimeters(void);
    long microsecondsToInches(void);
  private:
    int _pin;//pin number of Arduino that is connected with SIG pin of Ultrasonic Ranger.
    long duration;// the Pulse time received;
};

#endif
