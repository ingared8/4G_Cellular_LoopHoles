package edu.osu.cse5469.hackcellular;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.reflect.Array;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created by GJ on 11/28/2015.
 */


public class GraphPainter {
    DataToPaint dataToPaint;
    private SurfaceHolder surfaceHolder;
    private SurfaceView surface;
    public Vector<Vector<Float>>  arrays;
    public Vector<Vector<Paint>>  paint;

    private int heightCanvas;
    private int widthCanvas;

    private int lengthXAxis;
    private int lengthYAxis;
    private int offsetAxis;

    private int interval;

    private int labelMode=0;
    private int xTicks=30;
    private int yTicks=5;
    private Paint axisPaint=new Paint(), textPaint=new Paint();
    private int textLength;
    private Timer timer = new Timer();

    private Vector<String> lables;

    class surfaceCreateThread extends Thread{
        public void run(){

                Canvas canvas = null;
                SurfaceHolder tmpsurfaceHolder=surface.getHolder();

                canvas = tmpsurfaceHolder.lockCanvas();// lock canvas for drawing and retrieving params
                retrieveSize(canvas);
                tmpsurfaceHolder.unlockCanvasAndPost(canvas);
                //  Log.v("Canvas", heightCanvas+" "+widthCanvas);
            }
        }


    private void bindsurfaceCallBack(){
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

    public GraphPainter(DataToPaint dataToPaint){
        this.dataToPaint=dataToPaint;
        arrays=dataToPaint.arrays;
        paint=dataToPaint.paint;
        surface=dataToPaint.surface;
        surfaceHolder=surface.getHolder();
        bindsurfaceCallBack();

        axisPaint.setColor(Color.argb(255, 0, 0, 0));
        axisPaint.setStrokeWidth(3);
        textPaint.setColor(Color.argb(255, 0, 0, 0));

        interval=dataToPaint.interval;
        lables=dataToPaint.labels;
        labelMode=dataToPaint.labelMode;
        if(labelMode==0){
            lables.add(1,"");
            lables.add(1,"");
            lables.add(1,"");
            lables.add(1,"");
        }
        yTicks=lables.size()-1;

        if (paint==null){
            paint=new Vector<Vector<Paint>>();
            paint.add(new Vector<Paint>());
            paint.add(new Vector<Paint>());

            Paint dataPaint1=new Paint();
            dataPaint1.setColor(Color.argb(255, 0, 0, 255));
            dataPaint1.setStrokeWidth(3);

            Paint barPaint1=new Paint();
            barPaint1.setColor(Color.argb(180, 0, 0, 255));
            barPaint1.setStrokeWidth(3);

            paint.get(0).add(dataPaint1);
            paint.get(0).add(barPaint1);
            paint.get(0).add(null);

            Paint dataPaint2=new Paint();
            dataPaint2.setColor(Color.argb(255, 255, 0, 0));
            dataPaint2.setStrokeWidth(3);
            dataPaint2.setStyle(Paint.Style.STROKE);

            Paint barPaint2=new Paint();
            barPaint2.setColor(Color.argb(180, 255, 0, 0));
            barPaint2.setStrokeWidth(5);

            paint.get(1).add(dataPaint2);
            paint.get(1).add(null);
            paint.get(1).add(barPaint2);
        }
    }

    public void retrieveSize(Canvas canvas){
        heightCanvas=canvas.getHeight();
        widthCanvas=canvas.getWidth();
        offsetAxis=widthCanvas/50;
        textLength=widthCanvas/30;
        textPaint.setTextSize(widthCanvas / 40);
        lengthXAxis=widthCanvas-2*offsetAxis-textLength;
        lengthYAxis=heightCanvas-2*offsetAxis;
        canvas.drawColor(Color.argb(255, 230, 230, 230));
    }

    private void drawAxis(Canvas canvas){

        canvas.drawColor(Color.argb(255, 230, 230, 230));

        int xstart=offsetAxis+textLength;
        int ystart=offsetAxis+lengthYAxis;

        canvas.drawLine(xstart, offsetAxis, xstart, ystart, axisPaint);
        canvas.drawLine(xstart, ystart, xstart + lengthXAxis, ystart, axisPaint);

        for (int i=0;i<=yTicks;i++){
            canvas.drawLine(xstart, ystart - lengthYAxis / yTicks * i, xstart + offsetAxis, ystart - lengthYAxis / yTicks * i, axisPaint);
            canvas.drawText(lables.get(i), offsetAxis / 8, ystart - lengthYAxis / yTicks * i + offsetAxis/2, textPaint);
        }

        for (int i=0;i<xTicks;i++){
            canvas.drawLine(xstart + lengthXAxis / xTicks * i, ystart, xstart + lengthXAxis / xTicks * i, ystart - offsetAxis, axisPaint);
        }


    }

    private void drawData(Canvas canvas){
        float lastx=0,lasty=0;
        float largestData=-1;

        for (int k=0; k<arrays.size(); k++){
            for(int i=(arrays.get(k).size()-xTicks)>0?(arrays.get(k).size()-xTicks):0;i<arrays.get(k).size();i++) {
                float usage=arrays.get(k).get(i);
                largestData=largestData>usage?largestData:usage;}
        }

        if (labelMode==0){
            for(int i=1;i<6;i++) {
                canvas.drawText(String.format("%.2f", (float) largestData/5 * i), 2 * offsetAxis + textLength, offsetAxis+lengthYAxis-i*lengthYAxis/5+offsetAxis, textPaint);
            }
        }

        for (int k=0; k<arrays.size(); k++){
            Vector<Float> FinalDraw=new Vector<Float>();
            for(int i=(arrays.get(k).size()-xTicks)>0?(arrays.get(k).size()-xTicks):0;i<arrays.get(k).size();i++) {
               float usage=arrays.get(k).get(i);
                FinalDraw.add(usage);
            }

            for(int i=0;i<FinalDraw.size();i++) {

                float tmpx=offsetAxis+lengthXAxis/xTicks*i+textLength;
                float tmpy=offsetAxis+lengthYAxis-((float)FinalDraw.get(i)/(float)largestData)*lengthYAxis;


                canvas.drawCircle(tmpx, tmpy, 5, paint.get(k).get(0));
                if(i!=0&&paint.get(k).get(1)!=null) canvas.drawLine(tmpx, offsetAxis+lengthYAxis, tmpx, tmpy, paint.get(k).get(1));
                if(i!=0&&paint.get(k).get(2)!=null) canvas.drawLine(lastx,lasty,tmpx,tmpy,paint.get(k).get(2));
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
        drawData(canvas);}
        if(canvas!=null)surfaceHolder.unlockCanvasAndPost(canvas);
    }

    TimerTask task= new TimerTask(){
        public void run() {
            draw();
        }
    };
    public void schedule(){

        timer.schedule(task,1000,interval);
    }
    public void cancel(){
        task.cancel();
    }
}
