package edu.osu.cse5469.hackcellular;

/**
 * Created by GJ on 10/8/2015.
 */

import android.app.Service;
import android.content.Intent;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;

/**
 * Created by GJ on 10/8/2015.
 */
public class DataService extends Service {
    private final String AttQueryCode = "*3282#";
    DataSet datausage = new DataSet();
    DataServiceIBinder dataserviceIBinder = new DataServiceIBinder();
    private long local_data;

    // Thread to query data usage in local and operator
    Thread querythread = new Thread(new Runnable() {

        @Override
        public void run() {
            while (true) {
                long timeStamp = System.currentTimeMillis();
                local_data = getLocalData();
                getOperatorData();
                try {
                    wait(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//              VolumeData tmpData = new VolumeData(timeStamp, local_data, operator_data);
//                datausage.addData(tmpData);
            }
        }
    });
    private long operator_data;

    @Override
    public IBinder onBind(Intent intent) {                                                              //this will be performed on Activity calling bindService()
        return dataserviceIBinder;
    }

    public void onCreate() {
        super.onCreate();
        ToastUtils.showToast("");
    }

    // Send USSD code to query ATT post-paid data usage
    private void sendUSSDCode(String ussdCode) {
        String encodedHash = Uri.encode("#");
        ussdCode = ussdCode.replaceAll("#", encodedHash);
        Intent ussdIntent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussdCode));
        ussdIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(ussdIntent);
    }

    // Get data usage from operator
    private void getOperatorData() {
        sendUSSDCode(AttQueryCode);
    }

    // Get data usage from local
    private long getLocalData() {
        return (TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes());
    }

    public VolumeData get() {
        return datausage.getData();
    }

    public DataSet play() {
        return datausage;
    }

    public class DataServiceIBinder extends Binder {                                                //this is the service interface returned to Activity on binding
        public DataService getService() {
////            MyServiceActivity.vh.sendMessage(MyServiceActivity.createMessage(
////                    MyServiceActivity.UPDATE_VIEW,
////                    "BindServiceWithIBinder.MyIBinder.getService()"));
            return DataService.this;
        }
    }


}
