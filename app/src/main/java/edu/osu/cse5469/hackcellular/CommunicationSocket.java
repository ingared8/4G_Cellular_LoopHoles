package edu.osu.cse5469.hackcellular;

import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by fengyuhui on 15/11/26.
 */
public class CommunicationSocket {

    private DatagramSocket client;
    private InetAddress intetServerAddr;
    private String ip;
    private int port;
    int size = 1000;

    private CommunicationSocket() {
        try {
            client = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public CommunicationSocket (String ip, int port) {
        this();
        this.ip = ip;
        this.port = port;
        try {
            intetServerAddr = InetAddress.getByName(ip);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void setReceivePacketSize(int size) {
        this.size = size;
    }

    public boolean flush() {
        int timeout = 200;
        boolean result = false;
        while(true) {
            String receiveInfo = receive(timeout);
            if (receiveInfo.equals("Timeout")){
                break;
            }
            if (receiveInfo.length() > 0) {
                Log.d("debug", "Memory filled with " + receiveInfo.length());
                result = true;
            }
        }
        return result;
    }

    public String receive() {
        String result = "";
        try {
            byte[] inData = new byte[size];
            DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
            client.receive(inPacket);
            result = new String(inPacket.getData(), inPacket.getOffset(), inPacket.getLength());
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public String receive(int timeout) {
        String result = "";
        try {
            client.setSoTimeout(timeout);
            byte[] inData = new byte[size];
            DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
            try {
                client.receive(inPacket);
                result = new String(inPacket.getData(), inPacket.getOffset(), inPacket.getLength());
            } catch (InterruptedIOException e) {
                Log.d("debug", "Timeout! No packet received.");
                return "Timeout";
            }
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public void send(String info) {
        DatagramPacket sendPacket = new DatagramPacket(info.getBytes(), info.length(), intetServerAddr, port);
        try {
            client.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
