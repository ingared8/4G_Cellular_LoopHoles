package edu.osu.cse5469.hackcellular;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
 
    private DataService dataService;

    private TextView dataattack;
    private Spinner dataspinner;
    private TextView voiceattack;
    private Spinner voicespinner;

    private TextView protocolattack;
    private Spinner protocolspinner;

    private Button startattack;


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

    private Spinner setSpinner (Spinner myspinner,String[] mylist){
        ArrayAdapter<String> myadapter;
        myadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mylist);
        myadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myspinner.setAdapter(myadapter);
        return myspinner;
    }

    private void buildUI(){
        setContentView(R.layout.activity_welcome);

        startattack = (Button)findViewById(R.id.startattack);

        dataattack = (TextView)findViewById(R.id.data_attack);
        dataspinner =(Spinner)findViewById(R.id.data_attack_spinner);
        setSpinner(dataspinner, this.getResources().getStringArray(R.array.datachoose));

        voiceattack = (TextView)findViewById(R.id.voice_attack);
        voicespinner =(Spinner)findViewById(R.id.voice_attack_spinner);
        setSpinner(voicespinner, this.getResources().getStringArray(R.array.voicechoose));

        protocolattack = (TextView)findViewById(R.id.protocol_attack);
        protocolspinner =(Spinner)findViewById(R.id.protocol_attack_spinner);
        setSpinner(protocolspinner, this.getResources().getStringArray(R.array.protocolchoose));
    }

    private void startattack(Button mybutton, final Spinner myspinner){
        mybutton.setOnClickListener(new Button.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent();
                switch(myspinner.getSelectedItemPosition()){
                    case 0:
                        intent.setClass(MainActivity.this, TTLActivity.class);
                        startActivity(intent);
                        break;
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildUI();
        startattack(startattack,dataspinner);

//        dataspinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String chose=MainActivity.this.getResources().getStringArray(R.array.datachoose)[position];
//            }
//        });

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
