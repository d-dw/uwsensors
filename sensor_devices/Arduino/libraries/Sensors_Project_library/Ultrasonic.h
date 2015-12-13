/***************************************************************************/  
//  Function: Measure the distance to obstacles in front and print the distance
//        value to the serial terminal.The measured distance is from 
//        the range 0 to 400cm(157 inches).
//  Hardware: Ultrasonic Range sensor
//  Arduino IDE: Arduino-1.0
//  Author:  LG   
//  Date:    Jan 17,2013
//  Version: v1.0 modified by FrankieChu
//  by www.seeedstudio.com
//
//  This library is free software; you can redistribute it and/or
//  modify it under the terms of the GNU Lesser General Public
//  License as published by the Free Software Foundation; either
//  version 2.1 of the License, or (at your option) any later version.
//
//  This library is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//  Lesser General Public License for more details.
//
//  You should have received a copy of the GNU Lesser General Public
//  License along with this library; if not, write to the Free Software
//  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
//
/*****************************************************************************/
#ifndef ULTRASONIC_H
#define ULTRASONIC_H

class Ultrasonic
{
  public:
    Ultrasonic(int pin);
    void DistanceMeasure(void);
    long microsecondsToCentimeters(void);
    long microsecondsToInches(void);
  private:
    int _pin;//pin number of Arduino that is connected with SIG pin of Ultrasonic Ranger.
    long duration;// the Pulse time received;
};

#endif
