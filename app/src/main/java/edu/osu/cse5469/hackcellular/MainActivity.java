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
import android.widget.Button;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends Activity {
 
    private DataService dataService;

    EditText ttl_value;
    Button set_ttl;
    int read_ttl_value;

    private ServiceConnection dataServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {                        //connect Service
            dataService = ((DataService.DataServiceIBinder) (service)).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {                 //disconnect Service
            dataService = null;
        }
    };

    private void bindService() {                                                                     //bind service and call onBind() in Service
        Intent intent = new Intent("com.homer.bind.bindService");
        bindService(intent, dataServiceConnection, Context.BIND_AUTO_CREATE);                    // bindService
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        set_ttl=(Button)findViewById(R.id.set_ttl);
        ttl_value = (EditText)findViewById(R.id.ttl_value);

        set_ttl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String str = ttl_value.getText().toString();
                if (str.length()!=0) {
                    read_ttl_value = Integer.parseInt(str);
                } else {
                    read_ttl_value = 0;
                }
                Log.d("debug", "" + read_ttl_value);
            }
        });

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
