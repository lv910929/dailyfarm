package com.hitotech.dailyfarm.wxapi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import com.hitotech.dailyfarm.R;
import com.hitotech.dailyfarm.application.ContextApplication;
import com.hitotech.dailyfarm.data.Constant;
import com.hitotech.dailyfarm.utils.MD5;
import com.hitotech.dailyfarm.utils.Util;
import com.tencent.mm.sdk.constants.Build;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.modelpay.PayReq;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by Lv on 2016/4/3.
 */
public class SocialWechatHandler {

    private static final int THUMB_SIZE = 150;
    private static final String SDCARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();

    //检查微信是否安装
    public static boolean isWXAppInstalled() {
        boolean result = false;
        if (ContextApplication.api.isWXAppInstalled()) {
            result = true;
        }
        return result;
    }

    //检查微信是否支持
    public static boolean checkWXAppSupport() {
        boolean result = false;
        int wxSdkVersion = ContextApplication.api.getWXAppSupportAPI();
        if (wxSdkVersion >= ContextApplication.TIMELINE_SUPPORTED_VERSION) {
            result = true;
        }
        return result;
    }

    //检查微信支付是否支持
    public static boolean checkWXPaySupport() {
        boolean result = false;
        int wxSdkVersion = ContextApplication.api.getWXAppSupportAPI();
        if (wxSdkVersion >= Build.PAY_SUPPORTED_SDK_INT) {
            result = true;
        }
        return result;
    }

    //调用微信，申请用户授权
    public static void registerToWX() {

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
    public static void sendTextToWX(String text, boolean isWX) {
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
    public static void sendImgToWX(Context context, boolean isWX) {
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
    public static void sendWebPageToWX(Context context, String title, String description,String url, boolean isWX) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
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
    //生成预付单信息
    public static String genProductArgs(Map<String,String> prepareMap) {
        StringBuffer xml = new StringBuffer();
        try {
            String	nonceStr = genNonceStr();
            xml.append("</xml>");
            List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
            packageParams.add(new BasicNameValuePair("appid", Constant.APP_ID));
            packageParams.add(new BasicNameValuePair("body", "APP pay test"));//描述
            packageParams.add(new BasicNameValuePair("mch_id", Constant.MCH_ID));
            packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
            packageParams.add(new BasicNameValuePair("notify_url", "http://121.40.35.3/test"));
            packageParams.add(new BasicNameValuePair("out_trade_no",prepareMap.get("out_trade_no")));
            packageParams.add(new BasicNameValuePair("spbill_create_ip","127.0.0.1"));
            packageParams.add(new BasicNameValuePair("total_fee", prepareMap.get("total_fee")));
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));
            String sign = genAppSign(packageParams);
            packageParams.add(new BasicNameValuePair("sign", sign));
            String xmlstring =toXml(packageParams);
            return xmlstring;
        } catch (Exception e) {
            return null;
        }
    }

    //发送支付请求
    public static void sendPayReq(Map<String,String> resultunifiedorder){
        PayReq payReq = new PayReq();
        payReq.appId = Constant.APP_ID;
        payReq.partnerId = resultunifiedorder.get("partnerId");
        payReq.prepayId = resultunifiedorder.get("prepayId");
        payReq.packageValue = "Sign=WXPay";
        payReq.nonceStr = resultunifiedorder.get("noncestr");
        payReq.timeStamp = resultunifiedorder.get("timestamp");
        payReq.sign = resultunifiedorder.get("sign");
        ContextApplication.api.sendReq(payReq);
    }

    private static String toXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<"+params.get(i).getName()+">");
            sb.append(params.get(i).getValue());
            sb.append("</"+params.get(i).getName()+">");
        }
        sb.append("</xml>");
        Log.e("orion","----"+sb.toString());
        return sb.toString();
    }

    public static String genAppSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constant.API_KEY);
        String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        return appSign;
    }

    private static String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private static long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }

    private static String buildTransaction(String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

}
