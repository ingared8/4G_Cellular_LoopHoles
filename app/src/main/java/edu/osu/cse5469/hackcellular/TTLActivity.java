package edu.osu.cse5469.hackcellular;

import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
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

    private Button sendSocketButton;
    private EditText desIP;
    private EditText desPort;
    private EditText ttlTime;
    private TextView textHint;
    private String serverAddr;
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
//        Handler writetextHandle = new Handler(){
//                public void run(){
//
//            }
//        };
//        writetextHandle.

        sendSocketButton = (Button) findViewById(R.id.sendButton);
        desIP = (EditText) findViewById(R.id.edited_ip);
        desPort = (EditText) findViewById(R.id.edited_port);
        ttlTime = (EditText) findViewById(R.id.edited_ttl);
        textHint = (TextView) findViewById(R.id.textHint);

        sendSocketButton.setOnClickListener(this);

        // Listen to the response msg
        new Thread(){
            @Override
            public void run() {
                super.run();
                while(true) {
                    try {
                        DatagramSocket listener = new DatagramSocket(listenPort);
                        byte[] inData = new byte[1024];
                        DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
                        listener.receive(inPacket);
                        result = new String(inPacket.getData(), inPacket.getOffset(), inPacket.getLength());
                        if (result.length() > 0) {
                            textHint.setText("Please reduce your TTL");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }

    /*
     * Convert String to int
     */
    public int toInt(String s){
        int result=0;
        for(int i=0; i<s.length(); i++){
            if(s.charAt(i)>=48 && s.charAt(i)<=57){
                result = result*10 + (s.charAt(i)-48);
            }
            else return 0;
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        serverAddr = desIP.getText().toString();
        Log.d("debug", " "+serverAddr);
        String tmp = desPort.getText().toString();
        portNum = toInt(tmp);
        tmp = ttlTime.getText().toString();
        ttl = toInt(tmp);

        Log.d("debug", ""+tmp);

        new SendfeedbackJob().execute();
        textHint.setText("Msg has been sent");
    }

    /*
     * To protect prevent the error of network operating on main thread.
     */
    private class SendfeedbackJob extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            try {
                // Send UDP packet
                client = new DatagramSocket();
                InetAddress intetServerAddr = InetAddress.getByName(serverAddr);
                String sendData = Integer.toString(ttl);
                System.out.print(sendData);
                DatagramPacket sendPacket = new DatagramPacket(sendData.getBytes(), sendData.length(), intetServerAddr, portNum);
                Log.d("debug", " " + sendData );
                Log.d("debug", " " + sendData.length() );
                Log.d("debug", " " + portNum );
                Log.d("debug", " " + serverAddr);
                client.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
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
