package edu.osu.cse5469.hackcellular;

import android.graphics.Paint;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Vector;

/**
 * Created by GJ on 11/28/2015.
 */
public class DataToPaint {
        public Vector<Vector<Float>>  arrays;
        public Vector<Vector<Paint>>  paint;
        public Vector<String> labels;
        public int labelMode=0;
        public SurfaceView surface;
        public int interval;

        public DataToPaint(){
                labels=new Vector<String>();
                arrays=new Vector<Vector<Float>>();
                paint=null;
        }
};
