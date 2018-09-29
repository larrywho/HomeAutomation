#!/usr/bin/python
import os
import glob
import time
import subprocess

os.system('modprobe w1-gpio')
os.system('modprobe w1-therm')

base_dir = '/sys/bus/w1/devices/'
device_folder = glob.glob(base_dir + '28*')[0]
device_file = device_folder + '/w1_slave'

def read_temp_raw():
   f = open(device_file, 'r')
   lines = f.readlines()
   f.close()
   return lines
def read_temp_raw_hang_fix():
   catdata = subprocess.Popen(['cat',device_file], stdout=subprocess.PIPE, stder
r=subprocess.PIPE)
   out,err = catdata.communicate()
   out_decode = out.decode('utf-8')
   lines = out_decode.split('\n')
   return lines

def read_temp():
   lines = read_temp_raw()
   while lines[0].strip()[-3:] != 'YES':
      time.sleep(0.2)
      lines = read_temp_raw()
   equals_pos = lines[1].find('t=')
   if equals_pos != -1:
      temp_string = lines[1][equals_pos+2:]
      temp_c = float(temp_string) / 1000.0
      temp_f = temp_c * 9.0 / 5.0 + 32.0
      #return temp_c, temp_f
      return temp_f

if True:
   print(read_temp())


# DS18B20 Author Gravatar Image SIMON MONK
# Although the DS18B20 just looks like a regular transistor, there is actually q
uite a lot going on inside.
# The chip includes the special 1-wire serial interface as well as control logic
 and the temperature sensor itself.
# Its output pin sends digital messages and Raspbian/Occidentalis includes an in
terface to read those messages. You can experiment with the device from the comm
and line or over SSH (see Lesson 6), before we run the full program.
# Add OneWire support
# Start by adding the following line to /boot/config.txt
# You can edit that file with nano by running sudo nano /boot/config.txt and the
n scrolling to the bottom and typing it there
# Copy Code

# dtoverlay=w1-gpio
# Then reboot with sudo reboot. When the Pi is back up and you're logged in agai
n, type the commands you see below into a terminal window. When you are in the '
devices' directory, the directory starting '28-' may have a different name, so c
d to the name of whatever directory is there.
# Copy Code

# sudo modprobe w1-gpio
# sudo modprobe w1-therm
# cd /sys/bus/w1/devices
# ls
# cd 28-xxxx (change this to match what serial number pops up)
# cat w1_slave
#  learn_raspberry_pi_modprobe.png
# The interface is a little unreliable, but fortunately it tells us if there is
a valid temperature to read. It's like a file, so all we have to do is read
# The response will either have YES or NO at the end of the first line. If it is
 yes, then the temperature will be at the end of the second line, in 1/000 degre
es C. So, in the example above, the temperature is actually read as 20.687 and t
hen 26.125 degrees C.
# If you have more than one Sensor connected, you'll see multiple 28-xxx files.
Each one will have the unique serial number so you may want to plug one in at a
time, look at what file is created, and label the sensor!
