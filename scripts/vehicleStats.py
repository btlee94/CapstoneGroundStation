from dronekit import connect, VehicleMode, time
import sys
import time

# Connect to UDP endpoint (and wait for default attributes to accumulate)
target = sys.argv[1] if len(sys.argv) >= 2 else '127.0.0.1:14555'
#print 'Connecting to ' + target + '...'
vehicle = connect(target, wait_ready=False)

#vehicle.parameters['ARMING_CHECK']=-9

#print "Waiting for arming..."

#while not vehicle.armed:
#	time.sleep(4)

#Allows vehicle home location to be read
cmds = vehicle.commands
cmds.download()
cmds.wait_ready()

while True:

# Get all vehicle attributes (state)
	print "Vehicle state: "
	print "Relative Altitude: %s" % vehicle.location.global_relative_frame.alt
	print "Battery Percent: %s" % vehicle.battery.level
	print "Groundspeed: %s" % vehicle.groundspeed
	print "Heading: %s" % vehicle.heading
	print "Mode: %s" % vehicle.mode.name
	print "Longitude: %s" % vehicle.location.global_frame.lon
	print "Latitude: %s" % vehicle.location.global_frame.lat
	if vehicle.home_location is not None:
                print "Home Longitude: %s" % vehicle.home_location.lon
                print "Home Latitude: %s" % vehicle.home_location.lat
        print "end"
	time.sleep(2)
	sys.stdout.flush()




vehicle.close()
#print "Done."

