import os
import time
import sys
from stat import *

x = 10
num = 0
while (x > 0):

    print "Connecting to target..."
    print "Waiting for arming..."
    print "Vehicle state:"
    print " Relative Altitude: %s" %num 
    print " Velocity: %s" % num
    print " Battery Percent: %s" % num
    print " Groundspeed: %s" % num
    print " Airspeed: %s" % num
    print " Mode: %s" % num
    print "Done."
    time.sleep(2)
    x = x - 1
    num = num + 1
    sys.stdout.flush()
