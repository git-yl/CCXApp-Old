package com.example.jasper.ccxapp.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

/**
 * Created by Administrator on 2017/6/2 0002.
 */

public class ShowProcessUtil {
    private static ProgressDialog progressDialog;

    public static boolean showProgressDialog(Activity activity, String title, String message) {
        //判断是否联网
        ConnectivityManager cwjManager=(ConnectivityManager) activity.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(cwjManager.getActiveNetworkInfo() != null) {
            boolean ifConnect = cwjManager.getActiveNetworkInfo().isAvailable();
            if (!ifConnect) {
                Toast.makeText(activity.getApplicationContext(), "暂未联网，该功能不可用", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                //显示等待框
                progressDialog = ProgressDialog.show(activity, title, message, true, false);
                progressDialog.show();
                return true;
            }
            //未联网时提示
        }else {
            Toast.makeText(activity.getApplicationContext(), "暂未联网，该功能不可用", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public static void hideProgressDialog() {
        //隐藏等待框
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
