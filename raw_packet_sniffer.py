__author__ = 'ingared'

import socket
import os
import struct
import udp
import ip
import ctypes
#from ICMPHeader import ICMP

# host to listen on
HOST = ''
PORT = 5501

# build UDP_Packet
def create_UDP_packet(sport,dport,data):
    udp_packet = udp.Packet()
    udp_packet.sport = sport
    udp_packet.dport = dport
    udp_packet.data = data
    return udp_packet

# build IP packet
def build_ip_packet(src, dst, ttl, udp_data):
    ip_packet = ip.Packet()
    ip_packet.src = src
    ip_packet.dst = dst
    ip_packet.ttl = ttl
    ip_packet.data = udp_data
    packet = ip.assemble(ip_packet, 0)
    return packet

def main():
    sniffer = socket.socket(socket.AF_INET, socket.SOCK_RAW, socket.IPPROTO_ICMP)
    sniffer.bind(( HOST, PORT ))
    sniffer.setsockopt(socket.IPPROTO_IP, socket.IP_HDRINCL, 1)

    while 1:
        packet = sniffer.recvfrom(65565)
        raw_buffer = packet[0]
        ip_header = raw_buffer[0:20]
        iph = struct.unpack('!BBHHHBBH4s4s' , ip_header)

        # Create our IP structure
        version_ihl = iph[0]
        version = version_ihl >> 4
        ihl = version_ihl & 0xF
        p1 = iph[1]
        p2 = iph[2]
        p3 = iph[3]
        p4 = iph[4]
        iph_length = ihl * 4
        ttl = iph[5]
        protocol = iph[6]
        something = iph[7]
        s_addr = socket.inet_ntoa(iph[8]);
        d_addr = socket.inet_ntoa(iph[9]);

        print 'IP -> Version:' + str(version) + ', Header Length:' + str(ihl) + \
        ', TTL:' + str(ttl) + ', Protocol:' + str(protocol) + ', Source:'\
         + str(s_addr) + ', Destination:' + str(d_addr)

        print "TTL of the packet desired is ", str(ttl)
        new_ttl = ttl
        ip_header_sending = struct.pack('!BBHHHBBH4s4s',version_ihl,p1,p2,p3,p4,new_ttl,protocol,something,d_addr,s_addr)
        print ip_header_sending
        #raw_buffer[0:20] = ip_header_sending

        sniffer.sendto(raw_buffer,packet[1])
        sniffer.sendto()

        # Create our ICMP structure
        #buf = raw_buffer[iph_length:iph_length + ctypes.sizeof(ICMP)]
        #icmp_header = ICMP(buf)

        #print "ICMP -> Type:%d, Code:%d" %(icmp_header.type, icmp_header.code) + '\n'

if __name__ == '__main__':
    main()