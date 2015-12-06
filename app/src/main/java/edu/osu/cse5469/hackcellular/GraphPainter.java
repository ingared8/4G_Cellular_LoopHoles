package edu.osu.cse5469.hackcellular;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created by GJ on 11/28/2015.
 */


public class GraphPainter {
    private SurfaceHolder surfaceHolder;
    private SurfaceView surface;
    private DataSet dataSet;
    private Vector<Vector<Paint>>  paint;

    private int heightCanvas;
    private int widthCanvas;

    private int lengthXAxis;
    private int lengthYAxis;
    private int offsetAxis;

    private boolean labelMode = true;
    private int xTicks = 30;
    private int yTicks;
    private Paint axisPaint=new Paint(), textPaint=new Paint();
    private int textLength;
    private Timer timer = new Timer();

    private Vector<String> labels;

    class surfaceCreateThread extends Thread{
        public void run(){

                Canvas canvas = null;
                SurfaceHolder tmpsurfaceHolder = surface.getHolder();

                canvas = tmpsurfaceHolder.lockCanvas();// lock canvas for drawing and retrieving params
                retrieveSize(canvas);
                tmpsurfaceHolder.unlockCanvasAndPost(canvas);
                //  Log.v("Canvas", heightCanvas+" "+widthCanvas);
            }
        }

    public GraphPainter(SurfaceView surface, Vector<String> labels, Vector<Vector<Paint>> paint) {
        this.labels = labels;
        labelMode = false;
        helper(surface, paint);
    }

    public GraphPainter(SurfaceView surface, String unit, Vector<Vector<Paint>> paint) {
        labels = new Vector<String>();
        labels.add("0");
        labels.add("");
        labels.add("");
        labels.add("");
        labels.add("");
        labels.add(unit);
        helper(surface, paint);
    }

    public void helper(SurfaceView surface, Vector<Vector<Paint>> paint) {
        this.paint = paint;
        this.surface = surface;
        this.surfaceHolder = this.surface.getHolder();
        bindsurfaceCallBack();

        axisPaint.setColor(Color.argb(255, 0, 0, 0));
        axisPaint.setStrokeWidth(3);
        textPaint.setColor(Color.argb(255, 0, 0, 0));
        yTicks = labels.size()-1;
        if (this.paint==null){
            this.paint = new Vector<Vector<Paint>>();
            this.paint.add(new Vector<Paint>());
            this.paint.add(new Vector<Paint>());

            Paint dataPaint1 = new Paint();
            dataPaint1.setColor(Color.argb(255, 255, 0, 0));
            dataPaint1.setStrokeWidth(3);
            dataPaint1.setStyle(Paint.Style.STROKE);

            Paint barPaint1 = new Paint();
            barPaint1.setColor(Color.argb(180, 255, 0, 0));
            barPaint1.setStrokeWidth(5);

            this.paint.get(0).add(dataPaint1);
            this.paint.get(0).add(null);
            this.paint.get(0).add(barPaint1);

            Paint dataPaint2 = new Paint();
            dataPaint2.setColor(Color.argb(255, 0, 0, 255));
            dataPaint2.setStrokeWidth(3);

            Paint barPaint2 = new Paint();
            barPaint2.setColor(Color.argb(180, 0, 0, 255));
            barPaint2.setStrokeWidth(3);

            this.paint.get(1).add(dataPaint2);
            this.paint.get(1).add(barPaint2);
            this.paint.get(1).add(null);
        }
    }

    private void bindsurfaceCallBack() {
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                surfaceCreateThread mThread = new surfaceCreateThread();
                mThread.start();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                // TODO Auto-generated method stub

            }
        });
    }

    private void retrieveSize(Canvas canvas){
        heightCanvas = canvas.getHeight();
        widthCanvas = canvas.getWidth();
        offsetAxis = widthCanvas/50;
        textLength = widthCanvas/30;
        textPaint.setTextSize(widthCanvas / 40);
        lengthXAxis = widthCanvas-2*offsetAxis-textLength;
        lengthYAxis = heightCanvas-2*offsetAxis;
        canvas.drawColor(Color.argb(255, 230, 230, 230));
    }

    private void drawAxis(Canvas canvas){

        canvas.drawColor(Color.argb(255, 230, 230, 230));

        int xstart = offsetAxis+textLength;
        int ystart = offsetAxis+lengthYAxis;

        canvas.drawLine(xstart, offsetAxis, xstart, ystart, axisPaint);
        canvas.drawLine(xstart, ystart, xstart + lengthXAxis, ystart, axisPaint);

        for (int i=0; i<=yTicks; i++){
            canvas.drawLine(xstart, ystart - lengthYAxis / yTicks * i, xstart + offsetAxis, ystart - lengthYAxis / yTicks * i, axisPaint);
            canvas.drawText(labels.get(i), offsetAxis / 8, ystart - lengthYAxis / yTicks * i + offsetAxis/2, textPaint);
        }

        for (int i=0; i<xTicks; i++){
            canvas.drawLine(xstart + lengthXAxis / xTicks * i, ystart, xstart + lengthXAxis / xTicks * i, ystart - offsetAxis, axisPaint);
        }


    }

    private void drawData(Canvas canvas){
        float lastx=0,lasty=0;
        float largestData = -1;

        int dimension = dataSet.getLastData().getDataDimension();

        if (labelMode){
            for (int k=0; k<dimension; k++){
                for(int i=(dataSet.size()-xTicks)>0?(dataSet.size()-xTicks):0; i<dataSet.size(); i++) {
                    float usage = dataSet.getData(i).getData().get(k);
                    largestData = largestData>usage?largestData:usage;
                }
            }
            for(int i=1;i<6;i++) {
                canvas.drawText(String.format("%.2f", (float) largestData/5 * i), 2 * offsetAxis + textLength, offsetAxis+lengthYAxis-i*lengthYAxis/5+offsetAxis, textPaint);
            }
            if(largestData==0)largestData=1;
        }
        else {
            largestData = (float) yTicks;
        }

        for (int k=0; k<dimension; k++){
            Vector<Float> FinalDraw=new Vector<Float>();
            for(int i=(dataSet.size()-xTicks)>0?(dataSet.size()-xTicks):0; i<dataSet.size(); i++) {
               float usage = dataSet.getData(i).getData().get(k);
                FinalDraw.add(usage);
            }

            for(int i=0; i<FinalDraw.size(); i++) {

                float tmpx = offsetAxis+lengthXAxis/xTicks*i+textLength;
                float tmpy = offsetAxis+lengthYAxis-((float)FinalDraw.get(i)/(float)largestData)*lengthYAxis;

                canvas.drawCircle(tmpx, tmpy, 5, paint.get(k).get(0));
                if(i!=0 && paint.get(k).get(1)!=null) canvas.drawLine(tmpx, offsetAxis+lengthYAxis, tmpx, tmpy, paint.get(k).get(1));
                if(i!=0 && paint.get(k).get(2)!=null) canvas.drawLine(lastx,lasty,tmpx,tmpy,paint.get(k).get(2));
                lastx=tmpx;
                lasty=tmpy;
            }
        }
    }

    private synchronized void draw(){
        Canvas canvas=surfaceHolder.lockCanvas();
        if(canvas!=null){
            retrieveSize(canvas);
            drawAxis(canvas);
            drawData(canvas);
        }
        if(canvas!=null) surfaceHolder.unlockCanvasAndPost(canvas);
    }

    TimerTask task = new TimerTask(){
        public void run() {
            draw();
        }
    };
    public void schedule(DataSet dataSet, int interval){
        this.dataSet = dataSet;
        timer.schedule(task, 1000, interval);
    }
    public void cancel(){
        task.cancel();
    }
}
