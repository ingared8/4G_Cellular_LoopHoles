__author__ = 'ingared'

import socket
import sys
from SkypeCalling import SkypeCallAttack

def createServerSocket():

    s= socket.socket(socket.AF_INET,type=socket.SOCK_DGRAM)

    HOST = 'localhost'    # Symbolic name, meaning all available interfaces
    PORT = 5502  # Arbitrary non-privileged port

    print 'Socket created'

    #Bind socket to local host and port
    try:
        s.bind((HOST, PORT))
    except socket.error as msg:
        print 'Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
        sys.exit()

    print 'Socket bind complete'
    return s

# Initialize the connection to start the attack
s = createServerSocket()

print 'Socket now listening'
data, addr  = s.recvfrom(100)
print 'Received packet from ' + addr[0] + ':' + str(addr[1])
print data
s.sendto('Starting',addr)

# Initialize the skype
attack = SkypeCallAttack()
count = 0
while (count < 4):
    count = attack.skypeAttack(data,count)

print 'Socket now listening'
# Initialize the connection to stop the attack
data, addr = s.recvfrom(1024)
print 'Received packet from ' + addr[0] + ':' + str(addr[1])
s.sendto("Stopping", addr)
print "Closing the socket"
s.close()