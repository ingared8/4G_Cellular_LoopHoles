__author__ = 'ingared'

import socket
import sys
import os
from SkypeCalling import SkypeCallAttack
import select
from subprocess import call
global codeList

codeList= []
Code_A = "2"
Code_B = "1"

import threading
import subprocess

class SkpyeCall(threading.Thread):
    def __init__(self):
        self.stdout = None
        self.stderr = None
        threading.Thread.__init__(self)
        self.__stop = threading.Event()

    def run(self):
        p = subprocess.call(["Python","StartAttack.py"],
                             shell=False)
        self.stdout, self.stderr = p.communicate()

class TTL(threading.Thread):
    def __init__(self):
        self.stdout = None
        self.stderr = None
        threading.Thread.__init__(self)

    def run(self):
        p = subprocess.call(["./EchoAndAttack"],
                             shell=False)

        self.stdout, self.stderr = p.communicate()


def createServerSocket():

    s= socket.socket(socket.AF_INET,type=socket.SOCK_DGRAM)

    HOST = ''    # Symbolic name, meaning all available interfaces
    PORT = 5500  # Arbitrary non-privileged port

    print 'Server: Socket created'

    #Bind socket to local host and port
    try:
        s.bind((HOST, PORT))
    except socket.error as msg:
        print 'Server: Bind failed. Error Code : ' + str(msg[0]) + ' Message ' + msg[1]
        sys.exit()

    print 'Server: Socket bind complete'
    return s

def actionA(addr):

    """
    Script for attack A

    Check for whether the port is able to listen on 5502
    Start for the StartAttack.py

    """
    print "Script for Skpe Call Attack " , addr

    try:
         call(["sudo", "fuser", "-n","udp","-k" ,"5502"])
    except:
        pass

    print " All processes on port no 5502 are suspended for SkpyeCall Attack"

    call(["python","StartAttack.py"])

    print " The Skype Attack Server got suspended due to Activity"


def actionB(addr):

    """
    Script for attack A

    Check for whether the port is able to listen on 5502
    Start for the StartAttack.py

    """
    print "Script for TTL Attack ", addr

    try:
         call(["sudo", "fuser", "-n","udp","-k" ,"5555"])
    except:
        pass

    call(["./EchoAndAttack"])

    print " The TTl Attack Server got suspended."

def actionC(addr):

    """
    Script for attack A

    Check for whether the port is able to listen on 5502
    Start for the StartAttack.py

    """
    print "Script for action C" + addr


# Initialize the connection to start the attack
s = createServerSocket()
print 'Server: Socket now listening'

s.setblocking(0)

# Keep on listening
while (True):
    ready = select.select([s], [], [], 1)
    if ready[0]:
        # Initialize the connection to stop the attack
        data, addr = s.recvfrom(1024)
        print 'Server: Received packet from ' + addr[0] + ':' + str(addr[1])
        s.sendto(data, addr)

        if (data == Code_A ):

            try:
                actionA(addr)
            except :
                print " Exception in A"

        elif (data == Code_B ):
            try:
                actionB(addr)
            except:
                print " Exception in B"

        else :
            print ("Server: Improper attack request")

print "Closing the socket"
s.close()
