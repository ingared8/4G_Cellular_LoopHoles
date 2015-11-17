__author__ = 'ingared'

import socket
import sys
import time

s= socket.socket(socket.AF_INET,type=socket.SOCK_DGRAM)

HOST = ''    # Symbolic name, meaning all available interfaces
PORT = 5504  # Arbitrary non-privileged port

print 'Socket created'

# Connect the socket to the port where the server is listening
server_address = ('localhost', 5502)
print 'connecting to %s port %s' % server_address

print 'Sending Message for Starting the attack '
message = 'Phone_number_you_want_to_use_including_plus_and_code'
s.sendto(message,server_address)

data, addr = s.recvfrom(100)
print 'Received ack from Server "%s"' %data

time.sleep(200)

print 'Sending Message for Stopping the attack '
message = 'stop'
s.sendto(message, server_address)

data, addr = s.recvfrom(100)
print 'Received ack from Server "%s" for stopping "%s"' %(addr[0],data)
print "Closing the socket"
s.close()