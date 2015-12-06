package edu.osu.cse5469.hackcellular;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.support.v7.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {
 

    private TextView attackType;
    private Spinner attackSpinner;

    private Button startAttack;
    private Switch ipSwitch;
    private EditText desIP;

    private String info = "";
    private String serverAddr;
    private boolean switchable;
    private static final int PORTNUM = 5500;

//    private ServiceConnection dataServiceConnection = new ServiceConnection() {
//
//        @Override
//        public void onServiceConnected(ComponentName name, IBinder service) {                       //connect Service
//            dataService = ((DataService.DataServiceIBinder) (service)).getService();
//        }
//
//        @Override
//        public void onServiceDisconnected(ComponentName name) {                 //disconnect Service
//            dataService = null;
//        }
//    };
//
//    private void bindService() {                                                                    //bind service and call onBind() in Service
//        final Intent intent = new Intent(this,DataService.class);
//        bindService(intent, dataServiceConnection, Context.BIND_AUTO_CREATE);                       // bindService
//    }

    private Spinner setSpinner (Spinner myspinner,String[] mylist){
        ArrayAdapter<String> myadapter;
        myadapter = new ArrayAdapter<String>(this, R.layout.my_spinner, mylist);
        myadapter.setDropDownViewResource(R.layout.my_spinner);
        myspinner.setAdapter(myadapter);
        return myspinner;
    }

    private void buildUI(){
        setContentView(R.layout.activity_welcome);

        startAttack = (Button)findViewById(R.id.startattack);

        attackType = (TextView)findViewById(R.id.attack_hint);
        attackSpinner =(Spinner)findViewById(R.id.attack_spinner);
        setSpinner(attackSpinner, this.getResources().getStringArray(R.array.attackChoose));

        ipSwitch = (Switch)findViewById(R.id.switch2);
        desIP = (EditText) findViewById(R.id.edited_ip2);

        switchable = false;

        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ohio);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);





//        voiceattack = (TextView)findViewById(R.id.voice_attack);
//        voicespinner =(Spinner)findViewById(R.id.voice_attack_spinner);
//        setSpinner(voicespinner, this.getResources().getStringArray(R.array.voicechoose));
//
//        protocolattack = (TextView)findViewById(R.id.protocol_attack);
//        protocolspinner =(Spinner)findViewById(R.id.protocol_attack_spinner);
//        setSpinner(protocolspinner, this.getResources().getStringArray(R.array.protocolchoose));
    }

    private void startattack(final Button mybutton, final Spinner myspinner){



        ipSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    //desIP.setFocusableInTouchMode(false);
                    desIP.setVisibility(buttonView.INVISIBLE);
                    switchable = true;
                } else {
                    //desIP.setFocusableInTouchMode(true);
                    desIP.setVisibility(buttonView.VISIBLE);
                    switchable = false;

                }

            }
        });

        mybutton.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                if (switchable){
                    serverAddr = "127.0.0.1";
                }else{
                    serverAddr = desIP.getText().toString();
                }

               // Log.d("serveraddr"," "+serverAddr);
                Intent intent = new Intent();
                switch (myspinner.getSelectedItemPosition()) {
                    case 0:
                        info = "1";
                        new SendFeedBackJob().execute();
                        intent.setClass(MainActivity.this, TTLActivity.class);
                        intent.putExtra("severAddr", serverAddr);
                        startActivity(intent);
                        break;
                    case 1:
                        info = "2";
                        new SendFeedBackJob().execute();
                        intent.setClass(MainActivity.this, PingPangActivity.class);
                        intent.putExtra("severAddr", serverAddr);
                        startActivity(intent);
                        break;
                    case 2:
                        info = "3";
                        new SendFeedBackJob().execute();
                        intent.setClass(MainActivity.this, Back3GActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });

    }

    private class SendFeedBackJob extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            CommunicationSocket communicationSocket;
            communicationSocket = new CommunicationSocket(serverAddr, PORTNUM);
            communicationSocket.sendPacket(info);
            return null;
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
        startattack(startAttack, attackSpinner);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
