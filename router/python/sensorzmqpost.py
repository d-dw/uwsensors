#!/usr/bin/env python2

# Temporary POST client used to upload sensor data
import os
import time
import sys
import zmq
import signal
import urllib
import urllib2

context = zmq.Context().instance()
socket = context.socket(zmq.SUB)
socket.connect("tcp://127.0.0.1:5556")
socket.setsockopt(zmq.SUBSCRIBE,'')
print "UW Sensor Project Test HTTP POST Client Started"

def signal_handler(signum, frame):
  context.destroy()
  sys.exit(0)

signal.signal(signal.SIGINT, signal_handler)

class SensorValues(dict):
  def __missing__(self, key):
    self[key] = []
    return self[key]

values = SensorValues()

while (not socket.closed):
  for k in values:
    if (len(values[k]) > 99):
      postdata = {"sensor_id":k, "value":values[k]}
      req = urllib2.Request("upload_post.php", urllib.urlencode(postdata))
      resp = urllib2.urlopen(req)
      print "POSTResponse: " + resp.read()
      values[k] = []

  try:
    string = socket.recv_string(flags=zmq.NOBLOCK)
    print string
    header, loc, addr, typ, seq, t, d = string.split(' ', 6)
    values[loc + typ].append(t + ":" + d)
  except zmq.ZMQError:
    time.sleep(0.01)
    continue
