package edu.osu.cse5469.hackcellular;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
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

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class PingPangActivity extends AppCompatActivity {

    // UI parameters
    private Button startAttack;
    private Button stopAttack;
    private EditText phoneNum;
    private TextView textInfo;

    // UI parameters' variable
    private String serverAddr;
    private String info = "";

    // Function parameters
    private boolean bindPoint = true;
    private NetStatSet netStatSet = new NetStatSet();

    private static final int PORTNUM = 5502;
    private static final int START_SIGNAL = 1;
    private static final int STOP_SIGNAL = 2;


    /****************************** Network Status PART *********************************/
    public int type2Name(int type) {
        // http://www.androidchina.net/2471.html
        switch (type) {
            case 0: return 0;   // Unknown
            case 1: return 1;   // 2G
            case 2: return 1;
            case 3: return 2;   // 3G
            case 4: return 1;
            case 5: return 2;
            case 6: return 2;
            case 7: return 1;
            case 8: return 2;
            case 9: return 2;
            case 10: return 2;
            case 11: return 1;
            case 12: return 2;
            case 13: return 3;  // 4G
            case 14: return 2;
            case 15: return 2;
            default: return 4;  // ERR
        }
    }

    public int findCellularStatus() {
        ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        int mobile_type = networkInfo.getSubtype();
        Log.d("debug", "Mobile type: "+Integer.toString(mobile_type));
        return type2Name(mobile_type);
    }

    /****************************** SurfaceView PART *********************************/
    private SurfaceHolder surfaceHolder;
    private SurfaceView surfaceView;
    private Paint textPaint = new Paint();
    private Paint netPaint=new Paint();
    private Paint axisPaint=new Paint();
    private Paint netBarPaint =new Paint();

    private int heightCanvas;
    private int widthCanvas;
    private int lengthXAxis;
    private int lengthYAxis;
    private int offsetAxis;
    private int wordLength;
    private int xSplit = 30;

    public void bindsurfaceCallBack() {
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceCreateThread surfaceThread = new surfaceCreateThread();
                surfaceThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    class surfaceCreateThread extends Thread {
        @Override
        public void run() {
            super.run();
            Canvas canvas = null;
            synchronized (surfaceHolder) {
                canvas = surfaceHolder.lockCanvas();
                retrieveSize(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void retrieveSize(Canvas canvas) {
        heightCanvas = canvas.getHeight();
        widthCanvas = canvas.getWidth();
        offsetAxis = widthCanvas/50;
        wordLength = widthCanvas/30;
        textPaint.setTextSize(widthCanvas/30);
        lengthXAxis = widthCanvas-2*offsetAxis- wordLength;
        lengthYAxis = heightCanvas-2*offsetAxis;
        canvas.drawColor(Color.argb(255, 230, 230, 230));
    }


    private Timer timer = new Timer();
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Canvas canvas = null;
            synchronized (surfaceHolder) {
                canvas = surfaceHolder.lockCanvas();

                axisPaint.setColor(Color.argb(255, 0, 0, 0));
                axisPaint.setStrokeWidth(3);

                netPaint.setColor(Color.argb(255, 255, 0, 0));
                netPaint.setStrokeWidth(3);
                netPaint.setStyle(Paint.Style.STROKE);

                netBarPaint.setColor(Color.argb(180, 255, 0, 0));
                netBarPaint.setStrokeWidth(5);

                textPaint.setColor(Color.argb(255, 0, 0, 0));

                if(canvas!=null){
                    retrieveSize(canvas);
                    drawAxies(axisPaint, canvas);
                    drawData(netPaint, canvas);
                }

                if(canvas!=null) surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    };

    public void drawAxies(Paint axisPaint,Canvas canvas) {
        canvas.drawColor(Color.argb(255, 230, 230, 230));
        int x_start = offsetAxis+ wordLength;
        int y_start = offsetAxis+lengthYAxis;
        canvas.drawLine(x_start, offsetAxis, x_start, y_start, axisPaint);
        canvas.drawLine(x_start, y_start, x_start+lengthXAxis, y_start, axisPaint);
        for (int i=0;i<=5;i++){
            canvas.drawLine(x_start, y_start-lengthYAxis/5*i, x_start+offsetAxis, y_start-lengthYAxis/5*i, axisPaint);
        }

        for (int i=0;i<xSplit;i++){
            canvas.drawLine(x_start + lengthXAxis/xSplit*i, y_start, x_start+lengthXAxis/xSplit*i, y_start-offsetAxis, axisPaint);
        }
    }

    public void drawData(Paint netPaint, Canvas canvas) {
        Date date = new Date();
        netStatSet.add(new NetStatus(date.getTime(), findCellularStatus()));
        Log.d("debug", "Status is now: " + Integer.toString(findCellularStatus()));
        float scale = 5;
        float last_x=0, last_y=0;

        canvas.drawText("Unknown", 2*offsetAxis+wordLength, offsetAxis+lengthYAxis+offsetAxis, textPaint);
        canvas.drawText("2G", 2 * offsetAxis + wordLength, offsetAxis + lengthYAxis - lengthYAxis / scale + offsetAxis, textPaint);
        canvas.drawText("3G", 2*offsetAxis+wordLength, offsetAxis+lengthYAxis-2*lengthYAxis/scale+offsetAxis, textPaint);
        canvas.drawText("LTE", 2 * offsetAxis + wordLength, offsetAxis + lengthYAxis - 3 * lengthYAxis / scale + offsetAxis, textPaint);
        canvas.drawText("ERR", 2 * offsetAxis + wordLength, offsetAxis + lengthYAxis - 4 * lengthYAxis / scale + offsetAxis, textPaint);

        int startPlace = (netStatSet.size()-xSplit>0)?netStatSet.size()-xSplit:0;
        for(int i=startPlace; i<netStatSet.size(); i++) {
            float tmp_x = offsetAxis+lengthXAxis/xSplit*(i-startPlace)+ wordLength ;
            float tmp_y = offsetAxis+lengthYAxis-((float) netStatSet.get(i).getStatus()/scale)*lengthYAxis;
            canvas.drawCircle(tmp_x, tmp_y, 5, netPaint);
            if(i!=startPlace)  canvas.drawLine(last_x, last_y, tmp_x, tmp_y, netBarPaint);
            last_x = tmp_x;
            last_y = tmp_y;
        }
    }

    /****************************** Function PART *********************************/
    /*
    * Handler for info exchange between UI and Thread
    */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == START_SIGNAL){
                textInfo.setText((String) msg.obj);
            }
            if(msg.what == STOP_SIGNAL){
                textInfo.setText((String) msg.obj);
            }
        }
    };

//    private void tcpSocket() {
//        Socket attackSocket;
//        DataOutputStream out;
//
//        try {
//            InetAddress serverAddr = InetAddress.getByName(this.serverAddr);
//            attackSocket = new Socket(serverAddr, PORT);
//            out = new DataOutputStream(attackSocket.getOutputStream());
//            out.writeUTF(info);
//            out.flush();
//
//            attackSocket.close();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void udpSocket() {
//        DatagramSocket attackSocket;
//        DatagramPacket outPacket;
//
//        try {
//            InetAddress serverAddr = InetAddress.getByName(this.serverAddr);
//            attackSocket = new DatagramSocket();
//            outPacket = new DatagramPacket(info.getBytes(), info.length(), serverAddr, PORT);
//            attackSocket.send(outPacket);
//        } catch (SocketException e) {
//            e.printStackTrace();
//        } catch (UnknownHostException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//
//    }

    /*
    * Attack button listener
    */
    class AttackClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String phone = phoneNum.getText().toString();

            info = phone;
            new SendfeedbackJob().execute();

            Message sendMsg = Message.obtain();
            sendMsg.obj = "Ping-Pang Attack Start. Please pay attention to the network status icon at the top and the graph";
            sendMsg.what = START_SIGNAL;
            handler.sendMessage(sendMsg);
            Log.d("debug", "Attack signal has sent.");

            if(bindPoint) {
                bindPoint = false;
                timer.schedule(task, 5000, 5000);
            }
        }
    }

    /*
     * Stop attack button listener
     */
    class StopClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String hint;
            if(serverAddr !="") {
                info = "STOP";
                new SendfeedbackJob().execute();
                hint = "Ping-Pang Attack Stop. Press the Attack button to re-start the attack";
            }
            else {
                hint = "Please start attack first";
            }

            Message sendMsg = Message.obtain();
            sendMsg.obj = hint;
            sendMsg.what = STOP_SIGNAL;
            handler.sendMessage(sendMsg);
            Log.d("debug", "Stop signal has sent.");

        }
    }

    private class SendfeedbackJob extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
//            tcpSocket();
//            udpSocket();
            CommunicationSocket communicationSocket = new CommunicationSocket(serverAddr, PORTNUM);
            communicationSocket.sendPacket(info);
            return null;
        }
    }

    /****************************** UI PART *********************************/

    public void bindUI(){
        setContentView(R.layout.activity_ping_pang);

        // UI bind
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView_PingPang);
        surfaceHolder = surfaceView.getHolder();
        startAttack = (Button) findViewById(R.id.startAttack_pingpang);
        stopAttack = (Button) findViewById(R.id.stopAttack_pingpang);
        phoneNum = (EditText) findViewById(R.id.phone_pingpang);
        textInfo = (TextView) findViewById(R.id.Hint_PingPang);

        // Intent content bing
        Intent intent = getIntent();
        serverAddr = intent.getStringExtra("severAddr");
        Log.d("debug", " " + serverAddr);
    }

    /****************************** Lifecycle PART *********************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindUI();
        bindsurfaceCallBack();                                                                      // Plot part

        startAttack.setOnClickListener(new AttackClickListener());
        stopAttack.setOnClickListener(new StopClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ping_pang, menu);
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
