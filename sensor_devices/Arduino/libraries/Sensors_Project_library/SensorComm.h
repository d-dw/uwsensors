#ifndef SENSORCOMM_H
#define SENSORCOMM_H

#define MAXPACKETPTR 9
#define MAXDATAPTR 22
#define RADIODTRPIN 9

#define TYPE_ULTRASONIC 1
#define TYPE_ULTRASONIC_LV 2
#define TYPE_ULTRASONIC_HR 3
#define TYPE_IR_SHARP 4

#define COMMAND_EMPTY 0x00
#define COMMAND_SLEEP 0x01

// 100 Byte XB Packet
typedef struct XBPacket {
  XBPacket() : h1(0x6B), h2(0x57), vers(1) {}
  const byte h1;
  const byte h2;
  const byte vers;
  byte type;
  unsigned long seqnum;
  unsigned long stime;
  long data[MAXDATAPTR];
} XBPacket;

// 100 Byte Command Packet Received From Gateway
typedef struct XBCommandPacket {
  byte h1; // Should be 0x6B
  byte h2; // Should be 0x57
  byte vers;
  byte type;
  union {
    byte byteary[96];
    unsigned long ulongary[24];
  } params;
} XBCommandPacket;

class SensorComm
{
  public:
    SensorComm(int txpin, int rxpin, byte type, XBeeAddress64* addr64);
    void begin(void);
    void put(long);
    byte getCommandAndClear();
    unsigned long getCommandParam1();
  private:
    SoftwareSerial xbSerial;
    XBee xbee;
    const byte _type;
    XBeeAddress64* daddr;
    XBPacket packetsbuf[MAXPACKETPTR];
    XBeeAddress64 destaddr;
    int packetptr;
    int dataptr;
    unsigned long seqnum;
    byte command;
    unsigned long commandParam1;

    void setupPacket();
    int sendAtCommand(AtCommandRequest* atReq, AtCommandResponse* atResp);
    int recvCommandPkt();
};

#endif
