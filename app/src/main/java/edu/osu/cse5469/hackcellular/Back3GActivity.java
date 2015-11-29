package edu.osu.cse5469.hackcellular;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.SurfaceHolder;
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
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;


/**
 * Created by W on 11/14/2015.
 */



public class Back3GActivity extends Activity {

    private Button DownloadButton;
    private Button CallButton;
    private boolean bindPoint = true;

    PlotData throughputPlotData =new PlotData();
    PlotData throughputPlotDataSettmp =new PlotData();
    int init=0;



    ProgressDialog mProgressDialog;
    final DownloadTask downloadTask = new DownloadTask(Back3GActivity.this);

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
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
          //  mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
          //  mProgressDialog.setIndeterminate(false);
         //   mProgressDialog.setMax(100);
         //   mProgressDialog.setProgress(progress[0]);
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
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
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

    /*******bindUI
     *
     */

    private void bindUI(){
        setContentView(R.layout.activity_back3g);
        DownloadButton = (Button) findViewById(R.id.button1_back3G);
        CallButton = (Button) findViewById(R.id.button2_back3G);

        surface = (SurfaceView)findViewById(R.id.surfaceView_back3G);
        surfaceHolder = surface.getHolder();

    }


    /***********surface******************
     */

    private SurfaceHolder surfaceHolder;
    private SurfaceView surface;
    private Paint localdataPaint=new Paint(),opdataPaint=new Paint(),axisPaint=new Paint(),opbarPaint=new Paint(),userbarPaint=new Paint(),textPaint=new Paint();
    private int heightCanvas;
    private int widthCanvas;
    private int xSplit=30;
    private int lengthXAxis;
    private int lengthYAxis;
    private int wordlength;

    private int offsetAxis;
    private Timer timer = new Timer();
    private Timer timer1 = new Timer();
    TimerTask speedtask= new TimerTask(){
        long RxIni=0;
        long TxIni=0;
        long tmpRx;
        long tmpTx;
        public void run() {
            if(init<=3){
                RxIni= TrafficStats.getMobileRxBytes();
                TxIni=TrafficStats.getMobileTxPackets();
                init++;
            }
            else{
                tmpRx= TrafficStats.getTotalRxBytes();
                tmpTx= TrafficStats.getTotalTxPackets();
               throughputPlotDataSettmp.addData(new VolumeData(System.currentTimeMillis(),tmpRx+tmpTx-RxIni-TxIni,0));
                Log.d("Flow",tmpRx+tmpTx-RxIni-TxIni+"");
                RxIni=tmpRx;
                TxIni=tmpTx;
        }
        }
    };

    TimerTask task= new TimerTask(){
        public void run() {
            Canvas canvas = null;
            synchronized (surfaceHolder) {
                //  if(surfaceHolder==null)  {surface = (SurfaceView)findViewById(R.id.surfaceView);surfaceHolder = surface.getHolder();}
                canvas = surfaceHolder.lockCanvas();


                axisPaint.setColor(Color.argb(255, 0, 0, 0));
                axisPaint.setStrokeWidth(3);
                textPaint.setColor(Color.argb(255, 0, 0, 0));

                /*****画笔
                 *
                 */
                localdataPaint.setColor(Color.argb(255, 0, 0, 255));
                localdataPaint.setStrokeWidth(3);

                opdataPaint.setColor(Color.argb(255, 255, 0, 0));
                opdataPaint.setStrokeWidth(3);
                opdataPaint.setStyle(Paint.Style.STROKE);

                userbarPaint.setColor(Color.argb(180, 0, 0, 255));
                userbarPaint.setStrokeWidth(3);
                opbarPaint.setColor(Color.argb(180, 255, 0, 0));
                opbarPaint.setStrokeWidth(5);

                if(canvas!=null){
                    retrieveSize(canvas);
                    drawAxies(canvas);
                    drawData(canvas);
                }

                if(canvas!=null) surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    };

    private void drawAxies(Canvas canvas){
        canvas.drawColor(Color.argb(255, 230, 230, 230));
        int xstart=offsetAxis+wordlength;
        int ystart=offsetAxis+lengthYAxis;
        canvas.drawLine(xstart, offsetAxis, xstart, ystart, axisPaint);
        canvas.drawLine(xstart, ystart,xstart+lengthXAxis, ystart, axisPaint);
        canvas.drawText("MBps", offsetAxis/8, 2 * offsetAxis, textPaint);
        canvas.drawText("0",xstart/3,ystart,textPaint);
        for (int i=0;i<=5;i++){
            canvas.drawLine(xstart, ystart - lengthYAxis / 5 * i, xstart + offsetAxis, ystart - lengthYAxis / 5 * i, axisPaint);
        }

        for (int i=0;i<xSplit;i++){
            canvas.drawLine(xstart + lengthXAxis / xSplit * i, ystart, xstart + lengthXAxis / xSplit * i, ystart - offsetAxis, axisPaint);
        }
    }

    private void drawData(Canvas canvas){
        DataSet throughputDataSet=new DataSet();

        long largestData=-1;

        for(int i=(throughputPlotDataSettmp.size()-xSplit)>1?(throughputPlotDataSettmp.size()-xSplit):1;i< throughputPlotDataSettmp.size();i++) {
            long usage=(throughputPlotDataSettmp.getData(i).getOperator_data()- throughputPlotDataSettmp.getData(0).getOperator_data());
            long uselessusage=0;

            throughputPlotData.addData(new VolumeData(throughputPlotDataSettmp.getData(i).getTimeStamp(),usage,uselessusage));
            largestData=largestData>usage?largestData:usage;
            Log.d("flow2",usage+"");
        }
        for(int i=1;i<6;i++) {
            canvas.drawText(String.format("%.2f", (float) largestData / 1024 / 1024/5*i), 2 * offsetAxis + wordlength, offsetAxis+lengthYAxis-i*lengthYAxis/5+offsetAxis, textPaint);
        }
 //       float lastx=0,lasty=0;
        for(int i=0;i< throughputPlotData.size();i++) {
            float tmpx=offsetAxis+lengthXAxis/xSplit*i+wordlength;
            float tmpyLocal=offsetAxis+lengthYAxis-((float) throughputPlotData.getData(i).getLocal_data()/(float)largestData)*lengthYAxis;
            canvas.drawCircle(tmpx, tmpyLocal, 5, localdataPaint);
//            canvas.drawCircle(tmpx,tmpyOP,8,opdataPaint);
            if(i!=0) canvas.drawLine(tmpx, offsetAxis+lengthYAxis, tmpx, tmpyLocal, userbarPaint);
//            if(i!=0)  canvas.drawLine(lastx,lasty, tmpx,tmpyOP, opbarPaint);
//            lastx=tmpx;
//            lasty=tmpyOP;
        }
    }

     public void retrieveSize(Canvas canvas){

        heightCanvas=canvas.getHeight();
        widthCanvas=canvas.getWidth();
        offsetAxis=widthCanvas/50;
        wordlength=widthCanvas/30;
        textPaint.setTextSize(widthCanvas/30);
        lengthXAxis=widthCanvas-2*offsetAxis-wordlength;
        lengthYAxis=heightCanvas-2*offsetAxis;
        //canvas.drawColor(Color.argb(255, 230, 230, 230));

    }


    /*Attack

     */


    public void Attack(){

        DownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             downloadTask.execute("http://mirrors.koehn.com/ubuntureleases/14.04.3/ubuntu-14.04.3-desktop-amd64.iso");
                if (bindPoint) {
                    bindPoint = false;
                    timer.schedule(task, 1000, 1000);
                    timer1.schedule(speedtask,1000,1000);
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



    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        surface = (SurfaceView)findViewById(R.id.surfaceView_back3G);
        bindUI();
        Attack();

        /*这一部分是新的GraphPainter的示例
        setContentView(R.layout.activity_back3g);
        final DataToPaint dataToPaint=new DataToPaint();

        dataToPaint.surface=(SurfaceView)findViewById(R.id.surfaceView_back3G);
        dataToPaint.interval=1000;

        dataToPaint.labels.add("0");
        dataToPaint.labels.add("MBps");

        Vector<Float> data1=new Vector<Float>();
        data1.add(new Float(3.14));
        data1.add(new Float(2));
        data1.add(new Float(1));
        data1.add(new Float(1.5));
        data1.add(new Float(6));
        dataToPaint.arrays.add(data1);

        Vector<Float> data2=new Vector<Float>();
        data2.add(new Float(4.5));
        data2.add(new Float(7));
        data2.add(new Float(6));
        data2.add(new Float(3));
        data2.add(new Float(9));
        dataToPaint.arrays.add(data2);

        final GraphPainter Painter=new GraphPainter(dataToPaint);

        DownloadButton = (Button) findViewById(R.id.button1_back3G);
        DownloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Painter.schedule();
            }
        });
        */

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


    // execute this when the downloader must be fired


}



