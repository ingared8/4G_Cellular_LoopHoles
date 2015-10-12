package edu.osu.cse5469.hackcellular;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Toast工具类
 * @author WikerYong   Email:<a href="#">yw_312@foxmail.com</a>
 * @version 2012-5-21 下午9:21:01
 */
public class ToastUtils {

    private static Toast mToast;

    public static void showToast(Context context, String msg, int duration) {
        // if (mToast != null) {
        // mToast.cancel();
        // }
        // mToast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        if (mToast == null) {
            mToast = Toast.makeText(context, msg, duration);
        } else {
            mToast.setText(msg);
        }
        mToast.show();
    }
}  