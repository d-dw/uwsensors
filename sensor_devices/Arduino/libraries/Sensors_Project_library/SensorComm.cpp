#include <Arduino.h>
#include <SoftwareSerial.h>
#include <XBee.h>
#include "SensorComm.h"

SensorComm::SensorComm(int txpin, int rxpin, byte type, XBeeAddress64* addr64)
  : xbSerial(txpin, rxpin), xbee(), _type(type), daddr(addr64), packetptr(0), dataptr(0), seqnum(0)
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
    if (++packetptr == MAXPACKETPTR) {
      // Send packets if buffer full
      Tx64Request tx;
      // Wake up radio
      digitalWrite(RADIODTRPIN, LOW);
      // Radio takes 20ms to wake up
      delay(20);
      for (int i = 0; i < MAXPACKETPTR + 1; i++) {
        tx = Tx64Request(*daddr, (uint8_t *)&packetsbuf[i], sizeof(XBPacket));
        xbee.send(tx);
        //Serial.println("tx sent");
      }
      // Put radio to sleep to save power
      digitalWrite(RADIODTRPIN, HIGH);
      packetptr = 0;
    }
    SensorComm::setupPacket();
  }
}

void SensorComm::setupPacket() {
  packetsbuf[packetptr].type = _type;
  packetsbuf[packetptr].seqnum = seqnum++;
  packetsbuf[packetptr].stime = millis();
  memset(packetsbuf[packetptr].data, 0, sizeof(packetsbuf[packetptr].data));
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
