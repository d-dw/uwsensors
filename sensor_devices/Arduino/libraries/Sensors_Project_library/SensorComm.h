#ifndef SENSORCOMM_H
#define SENSORCOMM_H

const int MAXPACKETPTR = 9;
const int MAXDATAPTR = 21;
const int RADIODTRPIN = 9;

// 100 Byte XB Packet
typedef struct XBPacket {
  XBPacket() : h1(0x6B), h2(0x57), vers(1) {}
  const byte h1;
  const byte h2;
  const byte vers;
  byte type;
  unsigned long seqnum;
  unsigned long stime;
  long data[22];
} XBPacket;

class SensorComm
{
  public:
    SensorComm(int txpin, int rxpin, byte type, XBeeAddress64* addr64);
    void begin(void);
    void put(long);
  private:
    SoftwareSerial xbSerial;
    XBee xbee;
    const byte _type;
    XBeeAddress64* daddr;
    XBPacket packetsbuf[9];
    XBeeAddress64 destaddr;
    int packetptr;
    int dataptr;
    unsigned long seqnum;
    void setupPacket();
    int sendAtCommand(AtCommandRequest* atReq, AtCommandResponse* atResp);
};

#endif
