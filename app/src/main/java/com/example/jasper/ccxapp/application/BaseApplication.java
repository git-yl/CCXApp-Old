package com.example.jasper.ccxapp.application;

import android.app.Application;
import android.content.Context;

import com.example.jasper.ccxapp.db.CreateMessageDB;
import com.example.jasper.ccxapp.util.InitSourceFolderUtil;
import cn.bmob.v3.Bmob;
import cn.jpush.im.android.api.JMessageClient;


/**
 * Created by Jasper on 2017/3/21.
 */

public class BaseApplication extends Application {

    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        Bmob.initialize(this, "31f4f13611f6d6904ee94ab4ff3095aa");//初始化Bmob

        // You can enable debug mode in developing state. You should close debug mode when release.
        JMessageClient.setDebugMode(false);
        JMessageClient.init(this,true);

        //创建资源文件目录
        InitSourceFolderUtil.createFileDirs();

        //创建本地数据库
        CreateMessageDB createMessageDB = new CreateMessageDB(getApplicationContext());
        createMessageDB.open();

        mContext = getApplicationContext();
    }



    public static Context getContext(){
        return mContext;
    }
}
