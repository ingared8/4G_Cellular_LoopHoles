package edu.osu.cse5469.hackcellular;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;


import static java.lang.Thread.sleep;

/**
 * Created by fengyuhui on 15/10/9.
 * Data charging activity to show the TTL attack
 */

public class TTLActivity extends AppCompatActivity  {

    // UI parameters
    private Button sendSocketButton;
    private EditText ttlTime;
    private EditText volume;
    private Switch switch1;
    private TextView textHint;
    private String serverAddr;
    private SurfaceView surfaceView;

    // UI parameters' variable
    private String ttl_manual;
    private String ttl;
    private String volume_manual;
    private String attackVolume;
    private boolean switchDefaultIndex;

    // Function parameters
    private boolean bindPoint = true;
    private CommunicationSocket communicationSocket;
    private GraphPainter graphPainter;
    private DataService dataService;

    private final static int PORTNUM = 5555;
    private final static int SERVER_MSG = 1;
    private final static int TTL_MSG = 2;
    private final static int INTERVAL = 10000;

    /****************************** UI PART *********************************/

    /*
    *bind UI with functions
    */
    private void bindUI(){
        setContentView(R.layout.activity_ttl);

        // UI bind
        sendSocketButton = (Button) findViewById(R.id.sendButton);
        ttlTime = (EditText) findViewById(R.id.edited_ttl);
        volume = (EditText)findViewById(R.id.edited_volume);
        switch1 = (Switch)findViewById(R.id.switch1);
        textHint = (TextView) findViewById(R.id.textHint);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView_TTL);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ohio);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);


        // Intent content bing
        Intent intent = getIntent();
        serverAddr = intent.getStringExtra("severAddr");
        Log.d("debug", " " + serverAddr);

        // Configuration in default mode
        ttlTime.setFocusableInTouchMode(false);
        volume.setFocusableInTouchMode(false);
        switchDefaultIndex = true;
        defaultOrManual();
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

    private void defaultOrManual(){
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
    class AttackClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            ttl_manual = ttlTime.getText().toString();
            volume_manual = volume.getText().toString();

            if(bindPoint) {
//                bindService();
            }
            new SendFeedBackJob().execute();
        }
    }

    /*
     * To protect prevent the error of network operating on main thread.
     */
    private class SendFeedBackJob extends AsyncTask<String, Void, String>{

        @Override
        protected String doInBackground(String... params) {

            // Update UI to show attack stacking
            Message sendMsg = Message.obtain();
            sendMsg.obj = "Start Attacking...";
            sendMsg.what = SERVER_MSG;
            handler.sendMessage(sendMsg);

            if(switchDefaultIndex){
                // Default attack
                ttl = "30";
                attackVolume = "3";
            }
            else {
                // Manual attack
                ttl = ttl_manual;
                attackVolume = volume_manual;
            }

            communicationSocket.flush();
            // Wait 3 seconds to avoid conflicting with the calling USSD code
            try {
                sleep(INTERVAL/2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // In default mode, do TTL probing to find the valid ttl
            if(switchDefaultIndex) {
                Boolean ttlValid = false;
                while(!ttlValid) {
                    Log.d("debug", "TTL is " + ttl);
                    String probeInfo = ttl + ",0";
                    communicationSocket.sendPacket(probeInfo);
                    if(communicationSocket.flush()) {
                        ttl = Integer.toString(Integer.parseInt(ttl) - 1);
                    } else{
                        ttlValid = true;
                    }
                }
                // Update most recent valid ttl to EditText
                Message ttlMsg = Message.obtain();
                ttlMsg.obj = ttl;
                ttlMsg.what = TTL_MSG;
                handler.sendMessage(ttlMsg);
            }

            // Start attack
            Log.d("debug", "TTL is " + ttl);
            String attackInfo = ttl + "," + attackVolume;
            communicationSocket.sendPacket(attackInfo);

            // Check attack Valid or not
            String textViewShow = "";
            if(communicationSocket.flush()) {
                textViewShow = attackVolume+"MB Attack Start, with TTL: " + ttl + ". But this is not a valid attack";
            } else {
                textViewShow = attackVolume+"MB Attack Start, with TTL: " + ttl + ".";
            }
            Message msg = Message.obtain();
            msg.obj = textViewShow;
            msg.what = SERVER_MSG;
            handler.sendMessage(msg);

//
//            // Start plot graph
//            while(bindPoint) {
//                try {
//                    sleep(INTERVAL);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
//                Log.d("Debug", "Start plotting");
//
//                if(dataService.getData().getLastData() != null) {
//                    Log.d("Debug", "Plotting");
//                    graphPainter.schedule(dataService.getData(), INTERVAL);
//                    bindPoint = false;
//                } else {
//                    Log.d("Debug", "Wait plotting");
//                    try {
//                        sleep(INTERVAL);
//                    } catch (InterruptedException e1) {
//                        e1.printStackTrace();
//                    }
//                }
//            }

            return null;
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

    /****************************** Lifecycle PART *********************************/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bindUI();

        graphPainter = new GraphPainter(surfaceView, "MB", null);
        communicationSocket = new CommunicationSocket(serverAddr, PORTNUM);

        // TTL activity communication protocol: "TTL, Attack Volume"
        sendSocketButton.setOnClickListener(new AttackClickListener());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        new stopJob().execute();
        graphPainter.cancel();
    }
}
