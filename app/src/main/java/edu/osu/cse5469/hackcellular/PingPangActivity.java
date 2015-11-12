package edu.osu.cse5469.hackcellular;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;

import java.util.Timer;
import java.util.TimerTask;

public class PingPangActivity extends AppCompatActivity {

    private Button button;

    /****************************** Network Status PART *********************************/

    public String type2Name(int type) {
        // http://www.androidchina.net/2471.html
        switch (type) {
            case 0: return "UNKNOWN";
            case 1: return "2G";
            case 2: return "2G";
            case 3: return "3G";
            case 4: return "2G";
            case 5: return "3G";
            case 6: return "3G";
            case 7: return "2G";
            case 8: return "3G";
            case 9: return "3G";
            case 10: return "3G";
            case 11: return "2G";
            case 12: return "3G";
            case 13: return "4G";
            case 14: return "3G";
            case 15: return "3G";
            default: return "ERR";
        }
    }

    public String findCellularStatus(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        int mobile_type = networkInfo.getSubtype();
        return type2Name(mobile_type);
    }

    /****************************** SurfaceView PART *********************************/
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Paint textPaint = new Paint();

    private int heightCanvas;
    private int widthCanvas;
    private int lengthXAxis;
    private int lengthYAxis;
    private int offsetAxis;
    private int wordlength;

    public void bindsurfaceCallBack() {
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceCreateThread surfaceThread = new surfaceCreateThread();
                surfaceThread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    class surfaceCreateThread extends Thread {
        @Override
        public void run() {
            super.run();
            Canvas canvas = null;
            synchronized (surfaceHolder) {
                canvas = surfaceHolder.lockCanvas();
                retrieveSize(canvas);
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    public void retrieveSize(Canvas canvas) {
        heightCanvas = canvas.getHeight();
        widthCanvas = canvas.getWidth();
        offsetAxis = widthCanvas/50;
        wordlength = widthCanvas/30;
        textPaint.setTextSize(widthCanvas/30);
        lengthXAxis=widthCanvas-2*offsetAxis-wordlength;
        lengthYAxis=heightCanvas-2*offsetAxis;
        canvas.drawColor(Color.argb(255, 230, 230, 230));
    }


    private Timer timer = new Timer();
    private TimerTask task = new TimerTask() {
        @Override
        public void run() {
            Canvas canvas = null;
            synchronized (surfaceHolder) {
                canvas = surfaceHolder.lockCanvas();

                if(canvas!=null) surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    };

    /****************************** Lifecycle PART *********************************/

    public void bindUI(){
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView_PingPang);
        surfaceHolder = surfaceView.getHolder();
        button = (Button) findViewById(R.id.button_PingPang);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        bindUI();
        bindsurfaceCallBack();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ping_pang);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ping_pang, menu);
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
