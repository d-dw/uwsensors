#include <Arduino.h>
#include <SoftwareSerial.h>
#include <XBee.h>
#include "SensorComm.h"

SensorComm::SensorComm(int txpin, int rxpin, byte type, XBeeAddress64* addr64)
  : xbSerial(txpin, rxpin), xbee(), _type(type), daddr(addr64), packetptr(0),
  dataptr(0), seqnum(0), command(COMMAND_EMPTY), commandParam1(0)
{
}

void SensorComm::begin(void)
{
  pinMode(RADIODTRPIN, OUTPUT);
  xbSerial.begin(57600);
  xbee.setSerial(xbSerial);
  setupPacket();
  // Wait until radio is associated with coordinator
  uint8_t aiCmd[] = {'A', 'I'};
  AtCommandRequest atReq = AtCommandRequest(aiCmd);
  AtCommandResponse atResp = AtCommandResponse();
  do {
    delay(1000);
    int ret = sendAtCommand(&atReq, &atResp);
    if (ret != 0)
      continue;
    //Serial.print("AI value: ");
    //Serial.print(atResp.getValue()[0], HEX);
  } while (atResp.getValueLength() != 1 || atResp.getValue()[0] != 0);
}

void SensorComm::put(long data)
{
  packetsbuf[packetptr].data[dataptr++] = data;
  // Check if packet is full of data
  if (dataptr >= MAXDATAPTR) {
    //Serial.println("Filled a packet!");
    dataptr = 0;
    // Increase packet pointer and check if max has been reached
    if (++packetptr >= MAXPACKETPTR) {
      // Send packets if buffer full
      Tx64Request tx;
      // Wake up radio
      digitalWrite(RADIODTRPIN, LOW);
      // Radio takes 20ms to wake up
      delay(20);
      for (int i = 0; i < MAXPACKETPTR; i++) {
        tx = Tx64Request(*daddr, (uint8_t *)&packetsbuf[i], sizeof(XBPacket));
        xbee.send(tx);
        delay(50);
        recvCommandPkt();
        //Serial.println("tx sent");
      }
      recvCommandPkt();
      // Put radio to sleep to save power
      digitalWrite(RADIODTRPIN, HIGH);
      packetptr = 0;
    }
    SensorComm::setupPacket();
  }
}

byte SensorComm::getCommandAndClear() {
  byte ret = command;
  command = COMMAND_EMPTY;
  return ret;
}

unsigned long SensorComm::getCommandParam1() {
  return commandParam1;
}

void SensorComm::setupPacket() {
  packetsbuf[packetptr].type = _type;
  packetsbuf[packetptr].seqnum = seqnum++;
  packetsbuf[packetptr].stime = millis();
  memset(packetsbuf[packetptr].data, 0, sizeof(packetsbuf[packetptr].data));
}

int SensorComm::recvCommandPkt() {
  xbee.readPacket(1000);
  while (xbee.getResponse().isAvailable()) {
    XBCommandPacket* cmdPkt;
    if (xbee.getResponse().getApiId() == RX_16_RESPONSE) {
      Rx16Response resp;
      xbee.getResponse().getRx16Response(resp);
      cmdPkt = reinterpret_cast<XBCommandPacket*>(resp.getData());
    }
    if (xbee.getResponse().getApiId() == RX_64_RESPONSE) {
      Rx64Response resp;
      xbee.getResponse().getRx64Response(resp);
      cmdPkt = reinterpret_cast<XBCommandPacket*>(resp.getData());
    }
    if (cmdPkt->h1 == 0x6B && cmdPkt->h2 == 0x57) {
      if (cmdPkt->type == COMMAND_SLEEP) {
        command = COMMAND_SLEEP;
        commandParam1 = cmdPkt->params.ulongary[0];
        return COMMAND_SLEEP;
      }
    }
    xbee.readPacket();
  }
  return 0;
}

int SensorComm::sendAtCommand(AtCommandRequest* atReq, AtCommandResponse* atResp) {
  // send the command
  xbee.send(*atReq);

  // wait up to 5 seconds for the status response
  if (xbee.readPacket(5000)) {
    // should be an AT command response
    if (xbee.getResponse().getApiId() == AT_COMMAND_RESPONSE) {
      xbee.getResponse().getAtCommandResponse(*atResp);
    }
  } else {
    // at command failed
    if (xbee.getResponse().isError()) {
      return 1;
    }
    else {
      return 2;
    }
  }
  return 0;
}
