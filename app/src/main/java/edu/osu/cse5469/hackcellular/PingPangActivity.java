package edu.osu.cse5469.hackcellular;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class PingPangActivity extends AppCompatActivity {

    // UI parameters
    private Button startAttack;
    private Button stopAttack;
    private EditText phoneNum;
    private TextView textInfo;
    private SurfaceView surfaceView;

    // UI parameters' variable
    private String serverAddr;
    private String info = "";

    // Function parameters
    private boolean bindPoint = true;
    private GraphPainter graphPainter;
    private DataSet dataSet = new DataSet();
    private Timer timer = new Timer();


    private static final int INTERVAL = 5000;
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
//        Log.d("debug", "Mobile type: "+Integer.toString(mobile_type));
        return type2Name(mobile_type);
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

    TimerTask dataUpdateTask= new TimerTask() {

        @Override
        public void run() {
            Date date = new Date();
            DataUnit dataUnit = new DataUnit();
            dataUnit.setTimeStamp(date.getTime());
            dataUnit.addData("Mobile Network Status", (float) findCellularStatus());
            Log.d("debug", "Mobile network status is: " + findCellularStatus());
            dataSet.add(dataUnit);
        }
    };

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
                timer.schedule(dataUpdateTask, 1000, INTERVAL);
                graphPainter.schedule(dataSet, INTERVAL);
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

    private class stopJob extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {
            CommunicationSocket stopScoket = new CommunicationSocket(serverAddr, PORTNUM);
            stopScoket.sendPacket("KILL");
            return null;
        }
    }

    private class SendfeedbackJob extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
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
        startAttack = (Button) findViewById(R.id.startAttack_pingpang);
        stopAttack = (Button) findViewById(R.id.stopAttack_pingpang);
        phoneNum = (EditText) findViewById(R.id.phone_pingpang);
        textInfo = (TextView) findViewById(R.id.textHint_pingpong);

        // Intent content bing
        Intent intent = getIntent();
        serverAddr = intent.getStringExtra("severAddr");
        Log.d("debug", " " + serverAddr);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ohio_white);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

    }

    /****************************** Lifecycle PART *********************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindUI();

        Vector<String> labels = new Vector<String>();
        labels.add("OTH");
        labels.add("2G");
        labels.add("3G");
        labels.add("LTE");
        labels.add("ERR");
        graphPainter = new GraphPainter(surfaceView, labels, null);
        startAttack.setOnClickListener(new AttackClickListener());
        stopAttack.setOnClickListener(new StopClickListener());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_pingpong, menu);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new stopJob().execute();
        graphPainter.cancel();
        dataUpdateTask.cancel();
    }
}
