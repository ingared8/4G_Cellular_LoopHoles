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
    private SurfaceHolder surfaceHolder;
    private SurfaceView surface;
    public Vector<Vector<Float>>  arrays;
    DataToPaint dataToPaint;

    private int heightCanvas;
    private int widthCanvas;

    private int lengthXAxis;
    private int lengthYAxis;
    private int offsetAxis;

    private int labelMode=0;
    private int xTicks=30;
    private int yTicks=5;
    private Paint axisPaint,textPaint;
    private int textLength;
    private Timer timer = new Timer();

    private Vector<String> lables;

    public GraphPainter(DataToPaint dataToPaint){
        this.dataToPaint=dataToPaint;
        arrays=dataToPaint.arrays;
        surface=dataToPaint.surface;
        surfaceHolder=surface.getHolder();

        lables=dataToPaint.lables;
        labelMode=dataToPaint.labelMode;
        if(labelMode==0){
            lables.add(1,"");
            lables.add(1,"");
            lables.add(1,"");
            lables.add(1,"");
        }
        yTicks=lables.size()-1;
    }

    public void retrieveSize(Canvas canvas){
        heightCanvas=canvas.getHeight();
        widthCanvas=canvas.getWidth();
        offsetAxis=widthCanvas/50;
        textLength=widthCanvas/30;
        textPaint.setTextSize(widthCanvas / 30);
        lengthXAxis=widthCanvas-2*offsetAxis-textLength;
        lengthYAxis=heightCanvas-2*offsetAxis;
    }

    private void drawAxies(Canvas canvas){

        canvas.drawColor(Color.argb(255, 230, 230, 230));

        int xstart=offsetAxis+textLength;
        int ystart=offsetAxis+lengthYAxis;

        canvas.drawLine(xstart, offsetAxis, xstart, ystart, axisPaint);
        canvas.drawLine(xstart, ystart, xstart + lengthXAxis, ystart, axisPaint);

        for (int i=0;i<=yTicks;i++){
            canvas.drawLine(xstart, ystart - lengthYAxis / yTicks * i, xstart + offsetAxis, ystart - lengthYAxis / yTicks * i, axisPaint);
            canvas.drawText(lables.get(i), offsetAxis / 8, ystart - lengthYAxis / yTicks * i + offsetAxis, textPaint);
        }

        for (int i=0;i<xTicks;i++){
            canvas.drawLine(xstart + lengthXAxis / xTicks * i, ystart, xstart + lengthXAxis / xTicks * i, ystart - offsetAxis, axisPaint);
        }
    }

    private void drawData(Canvas canvas){



        for (int k=0; k<arrays.size(); k++){
            Vector<Float> FinalDraw=new Vector<Float>();
            float largestData=-1;
            for(int i=(arrays.get(k).size()-xTicks)>0?(arrays.get(k).size()-xTicks):0;i<arrays.get(k).size();i++) {
               float usage=arrays.get(k).get(i);
                FinalDraw.add(usage);
                largestData=largestData>usage?largestData:usage;
            }

            if (labelMode==0){
                for(int i=1;i<6;i++) {
                    canvas.drawText(String.format("%.2f", (float) largestData/5 * i), 2 * offsetAxis + textLength, offsetAxis+lengthYAxis-i*lengthYAxis/5+offsetAxis, textPaint);
                }
            }

            for(int i=0;i<FinalDraw.size();i++) {
                float tmpx=offsetAxis+lengthXAxis/xTicks*i+textLength;
                float tmpy=offsetAxis+lengthYAxis-((float)FinalDraw.get(i)/(float)largestData)*lengthYAxis;
    //            canvas.drawCircle(tmpx, tmpy, 5, localdataPaint);
    //            if(i!=0) canvas.drawLine(tmpx, offsetAxis+lengthYAxis, tmpx, tmpy, userbarPaint);
            }
        }
    }

    private synchronized void draw(){
        Canvas canvas=surfaceHolder.lockCanvas();
        if(canvas!=null){
        retrieveSize(canvas);
        drawAxies(canvas);
        drawData(canvas);}
        if(canvas!=null)surfaceHolder.unlockCanvasAndPost(canvas);
    }

    TimerTask task= new TimerTask(){
        public void run() {
            draw();
        }
    };
    public void schedule(){
        timer.schedule(task,5000,dataToPaint.interval);
    }
}
