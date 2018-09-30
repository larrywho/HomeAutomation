#!/usr/bin/python

import glob
import time
import paho.mqtt.publish as publish
from temperature import *

Broker = '192.168.1.34'
auth = {
    'username': 'user2',
    'password': 'bar',
}

pub_topic = 'smartthings/Virtual Temperature/temperature'

#base_dir = '/sys/bus/w1/devices/'
#device_folder = glob.glob(base_dir + '28-*')[0]
#device_file = device_folder + '/w1_slave'

#def read_temp():
#    valid = False
#    temp = 0
#    with open(device_file, 'r') as f:
#        for line in f:
#            if line.strip()[-3:] == 'YES':
#                valid = True
#            temp_pos = line.find(' t=')
#            if temp_pos != -1:
#                temp = float(line[temp_pos + 3:]) / 1000.0

#    if valid:
#        return temp
#    else:
#        return None


while True:
    temp = read_temp()
    if temp is not None:
        publish.single(pub_topic, str(temp),
                hostname=Broker)
    time.sleep(60)
