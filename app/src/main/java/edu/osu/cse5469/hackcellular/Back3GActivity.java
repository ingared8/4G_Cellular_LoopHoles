package edu.osu.cse5469.hackcellular;


import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.v7.app.ActionBar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by W on 11/14/2015.
 */



public class Back3GActivity extends AppCompatActivity {


    // UI parameters
    private Button downloadButton;
    private Button callButton;
    private Button resetGraphButton;
    private SurfaceView surfaceView;
    private TextView textInfo;

    // Function parameters
    private boolean bindPoint = true;
    private Timer timer = new Timer();
    private GraphPainter graphPainter;
    private DataSet dataSet = new DataSet();
    final DownloadTask downloadTask = new DownloadTask(Back3GActivity.this);
    private final static int INTERVAL = 1000;
    int init=0;

    private static final int DOWNLOAD_SIGNAL = 1;
    private static final int ATTACK_SIGNAL = 2;

    /****************************** UI PART *********************************/


    private void bindUI(){
        setContentView(R.layout.activity_back3g);
        downloadButton = (Button) findViewById(R.id.button1_back3G);
        callButton = (Button) findViewById(R.id.button2_back3G);
        resetGraphButton = (Button) findViewById(R.id.reset_Back3G);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView_back3G);
        textInfo = (TextView) findViewById(R.id.textHint_back3g);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setLogo(R.mipmap.ohio_white);
        actionBar.setDisplayUseLogoEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

    }

    /****************************** Function PART *********************************/
    TimerTask speedTask = new TimerTask(){
        long RxIni=0;
        long TxIni=0;
        long tmpRx;
        long tmpTx;
        public void run() {
            if(init<=2){
                RxIni = TrafficStats.getTotalRxBytes();
                TxIni = TrafficStats.getTotalTxPackets();
                init++;
            }
            else{
                tmpRx = TrafficStats.getTotalRxBytes();
                tmpTx = TrafficStats.getTotalTxPackets();

                Date date = new Date();
                DataUnit dataUnit = new DataUnit();
                dataUnit.setTimeStamp(date.getTime());
                dataUnit.addData("Network Speed", (float) (tmpRx + tmpTx - RxIni - TxIni)/1000);
                Log.d("Flow", tmpRx + tmpTx - RxIni - TxIni + "");
                dataSet.add(dataUnit);

                if(init == 3) {
                    graphPainter.schedule(dataSet, INTERVAL);
                }

                RxIni=tmpRx;
                TxIni=tmpTx;
                init++;
            }
        }
    };

    /*
     * Handler for info exchange between UI and Thread
     */
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == DOWNLOAD_SIGNAL){
                textInfo.setText((String) msg.obj);
            }
            if(msg.what == ATTACK_SIGNAL){
                textInfo.setText((String) msg.obj);
            }
        }
    };

    public void Attack(){

        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bindPoint) {
                    bindPoint = false;
                    downloadTask.execute("http://mirrors.koehn.com/ubuntureleases/14.04.3/ubuntu-14.04.3-desktop-amd64.iso");
                    timer.schedule(speedTask, 1000, INTERVAL);
                    Message sendMsg = Message.obtain();
                    sendMsg.obj = "File downloading, to press ATTACK button to start attack, and pay attention to the network status icon at the top.";
                    sendMsg.what = DOWNLOAD_SIGNAL;
                    handler.sendMessage(sendMsg);
                }
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:6149409911"));
                startActivity(intent);

                Message sendMsg = Message.obtain();
                sendMsg.obj = "Attack has happened, see the throughput drop down on the graph. Press ATTACK button to start another attack";
                sendMsg.what = ATTACK_SIGNAL;
                handler.sendMessage(sendMsg);
            }
        });

    }

    class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
            mWakeLock.acquire();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            if (result != null)
                Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
            else
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode() + " " + connection.getResponseMessage();
                }

                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream("/sdcard/TestF.pdf");

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }
            return null;
        }
    }

    /*
     * Reset graph listener
     */
    class ResetClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(dataSet.getLastData() != null) {
                DataUnit lastData = dataSet.getLastData();
                dataSet.clear();
                dataSet.add(lastData);
            }
        }
    }

    /****************************** Lifecycle PART *********************************/

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindUI();
        graphPainter = new GraphPainter(surfaceView, "KBps", null);
        Attack();
        resetGraphButton.setOnClickListener(new ResetClickListener());
    }





    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        MenuInflater inflater = getMenuInflater();
        getMenuInflater().inflate(R.menu.menu_back3g, menu);
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
        downloadTask.cancel(true);
        speedTask.cancel();
        graphPainter.cancel();
    }
}
