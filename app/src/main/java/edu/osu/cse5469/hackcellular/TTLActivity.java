package edu.osu.cse5469.hackcellular;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.net.Socket;

public class TTLActivity extends AppCompatActivity implements View.OnClickListener {

    private Button bt;
    private EditText desIP;
    private EditText desPort;
    private EditText ttlTime;
    private String ipAdr;
    private int portNum;
    private int ttl;

    private Socket client = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttl);

        bt = (Button) findViewById(R.id.sendButton);
        desIP = (EditText) findViewById(R.id.edited_ip);
        desPort = (EditText) findViewById(R.id.edited_port);
        ttlTime = (EditText) findViewById(R.id.edited_ttl);

        ipAdr = desIP.getText().toString();
        String tmp = desPort.getText().toString();
        portNum = Integer.parseInt(tmp);
        tmp = ttlTime.getText().toString();
        ttl = Integer.parseInt(tmp);

        bt.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

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
