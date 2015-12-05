__author__ = 'ingared'

import socket
import sys
from SkypeCalling import SkypeCallAttack
import select

def createServerSocket():

    s= socket.socket(socket.AF_INET,type=socket.SOCK_DGRAM)

    HOST = ''    # Symbolic name, meaning all available interfaces
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
attack = SkypeCallAttack()

while (True):
    s = createServerSocket()
    print 'Socket now listening'

    data, addr  = s.recvfrom(100)

    if (data == "KILL"):
        print "AT MAIN --> KILLING THE SKPE SEREVR"
        exit(0)

    if ( data != "STOP"):
        print 'Received packet from ' + addr[0] + ':' + str(addr[1])
        print data
        s.sendto('Starting',addr)

        # Initialize the skype
        count = 0
        max_attack = 10
        s.setblocking(0)
        while (count < max_attack):
            ready = select.select([s], [], [], 1)
            if ready[0]:
                # Initialize the connection to stop the attack
                data, addr = s.recvfrom(1024)
                if ( data == "KILL"):
                    print "IN BETWEEN--> KILLING THE SKPE SEREVR"
                    exit(0)
                elif( data == "STOP"):
                    print 'Received packet from ' + addr[0] + ':' + str(addr[1])
                    s.sendto("Stopping", addr)
                    break
                else:
                    print("Unknown code message received is " , )
            else:
                count = attack.skypeAttack(data,count)

        print "Closing the socket"
        s.close()
    else:
        print "Closing the socket"
        s.close()