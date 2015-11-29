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
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.Date;
import java.util.Vector;

/**
 * Created by GJ on 10/8/2015.
 */
public class DataService extends Service {
    private final String AttQueryCode = "*3282#";
    private DataSet dataSet = new DataSet();
    DataServiceIBinder dataserviceIBinder = new DataServiceIBinder();

    /****************************** Getter PART *********************************/

    public DataSet getData() {
        return this.dataSet;
    }

    /****************************** Function PART *********************************/
    private BroadcastReceiver SMSReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            SmsMessage[] msgs = null;
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                msgs = new SmsMessage[pdus.length];
                String phoneNum = "";
                String smsData ="";
                for (int i = 0; i < msgs.length; i++) {
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

    void handleDataUsageResponseSMS(String phoneNum, String smsData) {
        if(phoneNum.equals("104"));
        Date date=new Date();
        long operatorData = (long) (Float.parseFloat(smsData.substring(smsData.indexOf("[You]:")+6,smsData.indexOf('\n',smsData.indexOf("[You]:"))-1).replace(",",""))*1024*1024);
        long localData = getLocalData();


        DataUnit dataUnit = new DataUnit();
        dataUnit.setTimeStamp(date.getTime());
        dataUnit.addData("Local Data", (float) localData);
        dataUnit.addData("Operator Data", (float) operatorData);
        dataSet.add(dataUnit);

        Log.d("usage raw", "localData：" + (float) localData / 1024 / 1024 + "     operatorData：" + (float) operatorData / 1024 / 1024);

    }

    // Thread to query data usage in local and operator
    Thread querythread = new Thread(new Runnable() {

        @Override
        public void  run() {
            while (true) {
                getOperatorData();
                synchronized(this){
                    try {
                        Thread.sleep(60000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
    });

    private void registerReceiver(){                                                                  //Register SMS broadcast receiver in the service
        IntentFilter SmsIntent = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        SmsIntent.setPriority(999);                                                                     //What does this mean?
        registerReceiver(SMSReceiver, SmsIntent);
    }

    // Get data usage from operator
    private void getOperatorData() {
        sendUSSDCode(AttQueryCode);
    }

    // Send USSD code to query ATT post-paid data usage
    private void sendUSSDCode(String ussdCode) {
        String encodedHash = Uri.encode("#");
        ussdCode = ussdCode.replaceAll("#", encodedHash);
        Intent ussdIntent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + ussdCode));
        ussdIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(ussdIntent);
    }

    // Get data usage from local
    private long getLocalData() {
        return (TrafficStats.getMobileTxBytes() + TrafficStats.getMobileRxBytes());
    }


    /****************************** Lifecycle PART *********************************/

    @Override
    public IBinder onBind(Intent intent) {                                                              //this will be performed on Activity calling bindService()
        registerReceiver();
        querythread.start();
        return dataserviceIBinder;
    }

    public class DataServiceIBinder extends Binder {                                                //this is the service interface returned to Activity on binding
        public DataService getService() {

            return DataService.this;
        }

    }

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(SMSReceiver);
    }


}
