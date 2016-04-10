package com.hitotech.dailyfarm.application;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.hitotech.dailyfarm.data.Constant;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.zhy.http.okhttp.OkHttpUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by Lv on 2016/4/3.
 */
public class ContextApplication extends Application{

    public static final int TIMELINE_SUPPORTED_VERSION = 0x21020001;

    private static ContextApplication instance;

    private static Handler mHandler;

    public static IWXAPI api;

    public static ContextApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initBase();
        setOkHttp();
        api = WXAPIFactory.createWXAPI(this, Constant.APP_ID);
        ContextApplication.api.registerApp(Constant.APP_ID);
    }

    private void initBase() {
        instance = this;
        mHandler = new Handler();
    }

    public static Handler getHandler() {
        if (mHandler == null) {
            mHandler = new Handler();
        }
        return mHandler;
    }

    private void setOkHttp(){
        OkHttpUtils.getInstance().setConnectTimeout(15 * 1000, TimeUnit.MILLISECONDS);
        OkHttpUtils.getInstance().setReadTimeout(20 * 1000, TimeUnit.MILLISECONDS);
        OkHttpUtils.getInstance().setWriteTimeout(20 * 1000, TimeUnit.MILLISECONDS);
        //使用https，但是默认信任全部证书
        OkHttpUtils.getInstance().setCertificates();
    }

    //版本名
    public static String getVersionName(Context context) {
        return getPackageInfo(context).versionName;
    }

    //版本号
    public static int getVersionCode(Context context) {
        return getPackageInfo(context).versionCode;
    }

    private static PackageInfo getPackageInfo(Context context) {
        PackageInfo pi = null;
        try {
            PackageManager pm = context.getPackageManager();
            pi = pm.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_CONFIGURATIONS);
            return pi;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pi;
    }

}
