package edu.osu.cse5469.hackcellular;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;


public class MainActivity extends Activity {
 
//    private DataService dataService;

    private TextView attackType;
    private Spinner attackSpinner;
//    private TextView voiceattack;
//    private Spinner voicespinner;
//
//    private TextView protocolattack;
//    private Spinner protocolspinner;

    private Button startAttack;


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
        myadapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mylist);
        myadapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        myspinner.setAdapter(myadapter);
        return myspinner;
    }

    private void buildUI(){
        setContentView(R.layout.activity_welcome);

        startAttack = (Button)findViewById(R.id.startattack);

        attackType = (TextView)findViewById(R.id.attack_hint);
        attackSpinner =(Spinner)findViewById(R.id.attack_spinner);
        setSpinner(attackSpinner, this.getResources().getStringArray(R.array.attackChoose));

//        voiceattack = (TextView)findViewById(R.id.voice_attack);
//        voicespinner =(Spinner)findViewById(R.id.voice_attack_spinner);
//        setSpinner(voicespinner, this.getResources().getStringArray(R.array.voicechoose));
//
//        protocolattack = (TextView)findViewById(R.id.protocol_attack);
//        protocolspinner =(Spinner)findViewById(R.id.protocol_attack_spinner);
//        setSpinner(protocolspinner, this.getResources().getStringArray(R.array.protocolchoose));
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
                    case 1:
                        intent.setClass(MainActivity.this, PingPangActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent.setClass(MainActivity.this, Back3GActivity.class);
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
