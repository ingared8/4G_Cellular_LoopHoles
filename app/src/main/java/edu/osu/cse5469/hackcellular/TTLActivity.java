package edu.osu.cse5469.hackcellular;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fengyuhui on 15/10/9.
 * Data charging activity to show the TTL attack
 */

public class TTLActivity extends AppCompatActivity  {

    private Button sendSocketButton;
    private EditText desIP;
    private EditText desPort;
    private EditText ttlTime;
    private TextView textHint;
    private String serverAddr;
    private int portNum;
    private String ttl;
    private final static int SERVER_MSG = 1;
    private DataService dataService;
    //private Socket client =null;
    private DatagramSocket client;
    private final int listenPort = 5501;
    private ServiceConnection dataServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {                       //connect Service
            dataService = ((DataService.DataServiceIBinder) (service)).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {                 //disconnect Service
            dataService = null;
        }
    };

    /*
    Variables for drawing
     */
    private SurfaceHolder surfaceHolder;
    private SurfaceView surface;
    private Paint localdataPaint=new Paint(),opdataPaint=new Paint(),axisPaint=new Paint();
    private Matrix bgMatrix;
    private int heightCanvas;
    private int widthCanvas;
    private int offsetAxis;
    private Timer timer = new Timer();
    TimerTask task;



    /*
     * Handler for info exchange between UI and Thread
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == SERVER_MSG){
                textHint.setText((String) msg.obj);
            }
        }
    };

    private void drawAxies(Paint axisPaint,Canvas canvas){
        canvas.drawLine(offsetAxis, offsetAxis, offsetAxis, heightCanvas/2-offsetAxis, axisPaint);
        canvas.drawLine(offsetAxis, heightCanvas/2-offsetAxis,widthCanvas-offsetAxis, heightCanvas/2-offsetAxis, axisPaint);
        int lengthAxis=widthCanvas-2*offsetAxis;
        for (int i=0;i<=30;i++){
            canvas.drawLine(offsetAxis+lengthAxis/30*i, heightCanvas/2-offsetAxis,offsetAxis+lengthAxis/30*i, heightCanvas/2-2*offsetAxis, axisPaint);
        }
    }

    private void drawData(Paint localdataPaint,Paint opdataPaint,Canvas canvas){
        DataSet dataSet=dataService.datausage;
        if(dataSet.size()>1);
        for(int i=2; i<=dataSet.size();i++)
        {
            canvas.drawCircle(,,,);
            canvas.drawCircle(,,,);
        }
    }


    private void bindService() {                                                                    //bind service and call onBind() in Service
        final Intent intent = new Intent(this,DataService.class);
        bindService(intent, dataServiceConnection, Context.BIND_AUTO_CREATE);                       // bindService
    }

    /*
     *bind UI with functions
     */
    private void bindUI(){
        setContentView(R.layout.activity_ttl);
        sendSocketButton = (Button) findViewById(R.id.sendButton);
        desIP = (EditText) findViewById(R.id.edited_ip);
        desPort = (EditText) findViewById(R.id.edited_port);
        ttlTime = (EditText) findViewById(R.id.edited_ttl);
        textHint = (TextView) findViewById(R.id.textHint);
        sendSocketButton.setOnClickListener(new SendClickListener());
        surface = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surface.getHolder();
    }

    /*
    Listen to the response msg
    */
    private void startListenerThread(){
        new Thread(){
            @Override
            public void run() {
                super.run();
                String result = "";
                while(true) {
                    try {
                        DatagramSocket listener = new DatagramSocket(listenPort);
                        byte[] inData = new byte[1024];
                        DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
                        listener.receive(inPacket);
                        result = new String(inPacket.getData(), inPacket.getOffset(), inPacket.getLength());
                        if (result.length() > 0) {
                            Message msg = Message.obtain();                                         // Using Handler to exchange message to UI
                            msg.obj = "Please reduce your TTL";
                            msg.what = SERVER_MSG;
                            handler.sendMessage(msg);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }.start();
    }
    class surfaceCreateThread extends Thread{
        private SurfaceHolder holder ;
        public surfaceCreateThread(SurfaceHolder holder){
            this.holder = holder;
        }
        public void run(){
            Canvas canvas = null;
            synchronized (holder) {
                canvas = holder.lockCanvas();// lock canvas for drawing and retrieving params
                heightCanvas=canvas.getHeight();
                widthCanvas=canvas.getWidth();
                holder.unlockCanvasAndPost(canvas);
                offsetAxis=widthCanvas/80;



//                canvas = holder.lockCanvas();
//                int[] buffer=new int[32];
//                //int k=0;
//                for (int i = 0; i < 500; i++) {
//                    for (int j = 0; j < buffer.length; j++) {
//                        buffer[j]=(int) ((Math.sin((j)*1.5*Math.PI/32)*0.1+Math.sin((i)*1.5*Math.PI/1000))*(WIDTH/2-X_OFFSET)/2);
//                    }
//                    int a=-9999,b=9999;
//                    for (int j = 0; j < buffer.length; j++) {
//                        if(buffer[j]>a) a=buffer[j];
//                        if(buffer[j]<b) b=buffer[j];
//                    }
//                    for (int j = 0; j < buffer.length; j++) {
//                        Paint newPaint=new Paint();
//                        newPaint.setColor(Color.argb((int)((buffer[j]-b)/(float)(a-b)*100),110, 181, 229));
//                        canvas.drawCircle(buffer[j]+WIDTH/2,i,2, newPaint);
//                    }
//
//                }
//                holder.unlockCanvasAndPost(canvas);




                // drawBack(surfaceHolder);

                Log.v("Canvas", heightCanvas+" "+widthCanvas);

            }}};


    private void bindsurfaceCallBack(){
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceCreateThread mThread = new surfaceCreateThread(holder);
                mThread.start();
            }

                @Override
            public void surfaceDestroyed(SurfaceHolder holder){
                    // TODO Auto-generated method stub

            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindUI();
        bindsurfaceCallBack();
        //bindService();
        startListenerThread();

        task = new TimerTask(){
            public void run() {
                Canvas canvas = null;
                synchronized (surfaceHolder) {
                    canvas = surfaceHolder.lockCanvas();
                    axisPaint.setColor(Color.argb(255, 255, 255, 255));
                    axisPaint.setStrokeWidth(3);
                    drawAxies(axisPaint,canvas);
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        };
        timer.schedule(task, 1000,1000);




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

    /*
    * Send button listener
    */
    class SendClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            serverAddr = desIP.getText().toString();
            Log.d("debug", " " + serverAddr);
            String tmp = desPort.getText().toString();
            portNum = toInt(tmp);
            ttl = ttlTime.getText().toString();

            Log.d("debug", "" + tmp);

            new SendfeedbackJob().execute();
            textHint.setText("Msg has been sent");
        }
    };
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
                DatagramPacket sendPacket = new DatagramPacket(ttl.getBytes(), ttl.length(), intetServerAddr, portNum);
                client.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
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
