package edu.osu.cse5469.hackcellular;

/**
 * Created by GJ on 10/8/2015.
 */

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.TrafficStats;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Date;

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
        public void  run() {
            while (true) {
                //long timeStamp = System.currentTimeMillis();
                //local_data = getLocalData();
                getOperatorData();
                synchronized(this){
//                try {
//                    wait(10000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }}
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }}

//              VolumeData tmpData = new VolumeData(timeStamp, local_data, operator_data);
//                datausage.addData(tmpData);
            }
        }
    });
    private long operator_data;

    private void registerReceiver(){                                                                  //Register SMS broadcast receiver in the service
        IntentFilter SmsIntent = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        SmsIntent.setPriority(999);                                                                     //What does this mean?
        registerReceiver(SMSReceiver, SmsIntent);
    }

    private BroadcastReceiver SMSReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            String phonenum = "";
            String str = "";
            String pwd_str = "";
            //this.abortBroadcast();
            if (bundle != null)
            {
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                String phoneNum = "";
                String smsData ="";
                for (int i = 0; i < msgs.length; i++)
                {
                    long currentTime = System.currentTimeMillis();

                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

                    if (phoneNum.length()==0 || phoneNum.equalsIgnoreCase(msgs[i].getOriginatingAddress()) == true) {
                        phoneNum = msgs[i].getOriginatingAddress();
                        smsData += msgs[i].getMessageBody().toString();
                    } else {
                        handleDataUsageResponseSMS(phoneNum,smsData);
                        phoneNum = msgs[i].getOriginatingAddress();
                        smsData = msgs[i].getMessageBody().toString();
                    }
                }
                if (phoneNum.length()>0 && smsData.length()>0) {
                    handleDataUsageResponseSMS(phoneNum,smsData);
                }
            }
        }
    };

    void handleDataUsageResponseSMS(String phoneNum, String smsData){
        if(phoneNum.equals("104"));
        Date date=new Date();
        long operatorData= (long) (Float.parseFloat(smsData.substring(smsData.indexOf("[You]:")+6,smsData.indexOf('\n',smsData.indexOf("[You]:"))-1))*1024*1024);
        long localData=(TrafficStats.getMobileTxBytes()+TrafficStats.getMobileRxBytes());
        datausage.addData(new VolumeData(date.getTime(), localData, operatorData));
       // ToastUtils.showToast(MainActivity.this,"" + localData+operatorData,100);
        Log.d("usage", "" + localData + "###" + operatorData);
    }

    @Override
    public IBinder onBind(Intent intent) {                                                              //this will be performed on Activity calling bindService()
        registerReceiver();
        querythread.start();
        return dataserviceIBinder;
    }

//    public void show(){Log.d("aaaaaaaa","bbbbbbbbbbbb");}

    public void onCreate() {
        super.onCreate();
      //  ToastUtils.showToast("");
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
//        public void showgg(){
//            show();
//        }
    }


}
