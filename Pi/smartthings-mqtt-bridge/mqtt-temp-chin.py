#!/usr/bin/python

import time
import paho.mqtt.publish as publish
from temperature_chin import *

Broker = '192.168.1.34'
auth = {
    'username': 'user2',
    'password': 'bar',
}

pub_topic = 'smartthings/Chin Temperature MQTT/temperature'

while True:
    temp = read_temp()
    if temp is not None:
        publish.single(pub_topic, str(temp),
                hostname=Broker)
    time.sleep(60)
