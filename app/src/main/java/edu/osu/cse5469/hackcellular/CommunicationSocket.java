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
    private InetAddress inetServerAddr;
    private String serverAddr;
    private int port;
    int size = 1000;

    private CommunicationSocket() {
        try {
            client = new DatagramSocket();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public CommunicationSocket (String serverAddr, int port) {
        this();
        this.serverAddr = serverAddr;
        this.port = port;
        try {
            inetServerAddr = InetAddress.getByName(serverAddr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    public void setReceivePacketSize(int size) {
        this.size = size;
    }

    // Flush memory, return true if there is anything in the receive memory, otherwise return false
    public boolean flush() {
        int timeout = 500;
        boolean result = false;
        while(true) {
            String receiveInfo = receivePacket(timeout);
            if (receiveInfo.equals("Timeout")){
                Log.d("debug", "Memory has been empty.");
                break;
            }
            if (receiveInfo.length() > 0) {
                Log.d("debug", "Memory filled with " + receiveInfo.length());
                result = true;
            }
        }
        return result;
    }

    // Receive message with blocking the process
    public String receivePacket() {
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

    // Receive message with a time interval. If do not receive anything, return "Timeout"
    public String receivePacket(int timeout) {
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

    public void sendPacket(String info) {
        DatagramPacket sendPacket = new DatagramPacket(info.getBytes(), info.length(), inetServerAddr, port);
        try {
            client.send(sendPacket);
//            Log.d("debug", "Sending message: " + info);
//            Log.d("debug", "Server's IP: " + serverAddr);
//            Log.d("debug", "Server's port: " + Integer.toString(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
