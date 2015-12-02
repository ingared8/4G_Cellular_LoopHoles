package edu.osu.cse5469.hackcellular;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
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
    private Button DownloadButton;
    private Button CallButton;
    private SurfaceView surfaceView;

    // Function parameters
    private boolean bindPoint = true;
    private Timer timer = new Timer();
    private GraphPainter graphPainter;
    private DataSet dataSet = new DataSet();
    final DownloadTask downloadTask = new DownloadTask(Back3GActivity.this);
    private final static int INTERVAL = 1000;
    int init=0;
//    ProgressDialog mProgressDialog;

    /****************************** UI PART *********************************/


    private void bindUI(){
        setContentView(R.layout.activity_back3g);
        DownloadButton = (Button) findViewById(R.id.button1_back3G);
        CallButton = (Button) findViewById(R.id.button2_back3G);
        surfaceView = (SurfaceView)findViewById(R.id.surfaceView_back3G);
    }

    /****************************** Function PART *********************************/
    TimerTask speedtask= new TimerTask(){
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

    public void Attack(){

        DownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadTask.execute("http://mirrors.koehn.com/ubuntureleases/14.04.3/ubuntu-14.04.3-desktop-amd64.iso");
                if (bindPoint) {
                    bindPoint = false;
                    timer.schedule(speedtask, 1000, INTERVAL);
                }
            }
        });

        CallButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:6149409911"));
                startActivity(intent);
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
            //  mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            //  mProgressDialog.setIndeterminate(false);
            //  mProgressDialog.setMax(100);
            //  mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            //   mProgressDialog.dismiss();
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


    /****************************** Lifecycle PART *********************************/

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bindUI();
        graphPainter = new GraphPainter(surfaceView, "KBps", null);
        Attack();

//        mProgressDialog = new ProgressDialog(Back3GActivity.this);
//        mProgressDialog.setMessage("A message");
//        mProgressDialog.setIndeterminate(true);
//        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        mProgressDialog.setCancelable(true);
//        mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
//            @Override
//            public void onCancel(DialogInterface dialog) {
//                downloadTask.cancel(true);
//            }
//        });
//        downloadTask.execute("http://web.cse.ohio-state.edu/~kannan/cse3461-5461/Cse3461.E.LAN.10-01-2014-part1.pdf");

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

}
