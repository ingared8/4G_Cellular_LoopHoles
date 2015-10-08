package edu.osu.cse5469.hackcellular;

/**
 * Created by GJ on 10/8/2015.
 */

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

package jasperg.demodataservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by GJ on 10/8/2015.
 */
public class DataService extends Service {
    Thread querythread=new Thread();
    DataSet datausage=new DataSet();
    DataServiceIBinder dataserviceIBinder=new DataServiceIBinder();

    public class DataServiceIBinder extends Binder {
        public DataService getService() {
////            MyServiceActivity.vh.sendMessage(MyServiceActivity.createMessage(
////                    MyServiceActivity.UPDATE_VIEW,
////                    "BindServiceWithIBinder.MyIBinder.getService()"));
            return DataService.this;
        }
    }

    public IBinder onBind(Intent intent){
        return dataserviceIBinder;
    }

    public void onCreate() {
        super.onCreate();

        ToastUtils.makeText(this, "show media player").show();
    }

    public VolumeData get() {
        return datausage.getData();
    }

    public DataSet play() {
        return datausage;
    }
}
