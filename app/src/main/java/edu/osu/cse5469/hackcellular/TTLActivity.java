package edu.osu.cse5469.hackcellular;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by fengyuhui on 15/10/9.
 * Data charging activity to show the TTL attack
 */

public class TTLActivity extends AppCompatActivity implements View.OnClickListener {

    private Button sendSocket;
    private EditText desIP;
    private EditText desPort;
    private EditText ttlTime;
    private TextView textHint;
    private String ipAddr;
    private int portNum;
    private int ttl;
    private String result = "";

    //private Socket client =null;

    private DatagramSocket client;
    private final int listenPort = 5501;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttl);

        sendSocket = (Button) findViewById(R.id.sendButton);
        desIP = (EditText) findViewById(R.id.edited_ip);
        desPort = (EditText) findViewById(R.id.edited_port);
        ttlTime = (EditText) findViewById(R.id.edited_ttl);
        textHint = (TextView) findViewById(R.id.textHint);

        ipAddr = desIP.getText().toString();
        String tmp = desPort.getText().toString();
        portNum = Integer.parseInt(tmp);
        tmp = ttlTime.getText().toString();
        ttl = Integer.parseInt(tmp);

        sendSocket.setOnClickListener(this);

        // Listen to the response msg
        new Thread(){
            @Override
            public void run() {
                super.run();
                try {
                    DatagramSocket listener = new DatagramSocket(listenPort);
                    byte[] inData = new byte[1024];
                    DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
                    listener.receive(inPacket);
                    result = new String(inPacket.getData(), inPacket.getOffset(), inPacket.getLength());
                    if(result.length()>0){
                        textHint.setText("Please reduce your TTL");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }.start();
    }

    @Override
    public void onClick(View v) {
        try {
            // Send UDP packet
            client = new DatagramSocket();
            InetAddress serverAddr = InetAddress.getByName(ipAddr);
            byte[] sendData = intToByteArray(ttl);
            byte[] receiveData = new byte[1024];
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverAddr, portNum);
            client.send(sendPacket);
            textHint.setText("Msg has been sent");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Convert int to byte
    public static byte[] intToByteArray(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)((i >> 24) & 0xFF);
        result[1] = (byte)((i >> 16) & 0xFF);
        result[2] = (byte)((i >> 8) & 0xFF);
        result[3] = (byte)(i & 0xFF);
        return result;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ttl, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
