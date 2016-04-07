package com.hitotech.dailyfarm.wxapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.hitotech.dailyfarm.R;
import com.hitotech.dailyfarm.application.ContextApplication;
import com.hitotech.dailyfarm.data.Constant;
import com.hitotech.dailyfarm.utils.Util;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;

/**
 * Created by Lv on 2016/4/3.
 */
public class SocialWechatHandler {

    private static final int THUMB_SIZE = 150;
    private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String WEB_PAGE_URL = "http:\\www.baidu.com";

    //检查微信是否安装
    public static boolean isWXAppInstalled(){
        boolean result = false;
        if (ContextApplication.api.isWXAppInstalled()) {
            result = true;
        }
        return result;
    }

    //检查微信是否支持
    public static boolean checkWXAppSupport(){
        boolean result = false;
        int wxSdkVersion = ContextApplication.api.getWXAppSupportAPI();
        if (wxSdkVersion >= ContextApplication.TIMELINE_SUPPORTED_VERSION) {
            result = true;
        }
        return result;
    }

    //调用微信，申请用户授权
    public static void registerToWX(){
        ContextApplication.api.registerApp(Constant.APP_ID);
        SendAuth.Req req = new SendAuth.Req();
        //授权读取用户信息
        req.scope = "snsapi_userinfo";
        //自定义信息
        req.state = buildTransaction("register");
        //向微信发送请求
        ContextApplication.api.sendReq(req);
    }

    /**
     * 发送文本到微信
     */
    public static void sendTextToWX(String text,boolean isWX){
        // 初始化一个WXTextObject对象
        WXTextObject textObj = new WXTextObject();
        textObj.text = text;
        // 用WXTextObject对象初始化一个WXMediaMessage对象
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // 发送文本类型的消息时，title字段不起作用
        // msg.title = "Will be ignored";
        msg.description = text;
        // 构造一个Req
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text"); // transaction字段用于唯一标识一个请求
        req.message = msg;
        req.scene = isWX ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        // 调用api接口发送数据到微信
        ContextApplication.api.sendReq(req);
    }

    /**
     * 发送图片到微信
     */
    public static void sendImgToWX(Context context,boolean isWX){
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        WXImageObject imgObj = new WXImageObject(bmp);
        WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = imgObj;
        Bitmap thumbBmp = Bitmap.createScaledBitmap(bmp, THUMB_SIZE, THUMB_SIZE, true);
        bmp.recycle();
        msg.thumbData = Util.bmpToByteArray(thumbBmp, true);  // 设置缩略图
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = isWX ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        ContextApplication.api.sendReq(req);
    }

    /**
     * 发送web到微信
     */
    public static void sendWebPageToWX(Context context,String title,String description,boolean isWX){
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = WEB_PAGE_URL;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;
        Bitmap thumb = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
        msg.thumbData = Util.bmpToByteArray(thumb, true);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = isWX ? SendMessageToWX.Req.WXSceneSession : SendMessageToWX.Req.WXSceneTimeline;
        ContextApplication.api.sendReq(req);
    }

    private static String buildTransaction(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

}
