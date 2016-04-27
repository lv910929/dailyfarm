package com.hitotech.dailyfarm.webview;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.webkit.JavascriptInterface;

import com.google.gson.JsonObject;
import com.hitotech.dailyfarm.activity.MainActivity;
import com.hitotech.dailyfarm.activity.QrScanActivity;
import com.hitotech.dailyfarm.entity.Union;
import com.hitotech.dailyfarm.utils.DialogUtil;
import com.hitotech.dailyfarm.utils.StringUtil;
import com.hitotech.dailyfarm.wxapi.SocialWechatHandler;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Lv on 2016/4/3.
 */
public class JavaScriptObject {

    Context mContext;

    public JavaScriptObject(Context mContext) {
        this.mContext = mContext;
    }

    /**
     * 微信登陆
     * type 暂时没用，以后区分其他登录
     */
    @JavascriptInterface
    public void loginaction(String type) {
        if (SocialWechatHandler.isWXAppInstalled()) {
            if (SocialWechatHandler.checkWXAppSupport()) {
                SocialWechatHandler.registerToWX();
            } else {
                DialogUtil.showInfoDialog(mContext, "提示", "抱歉，您的微信不支持登录功能，请先升级");
            }
        } else {
            DialogUtil.showInfoDialog(mContext, "提示", "您尚未安装微信,请先安装微信");
        }
    }

    @JavascriptInterface
    public JsonObject otherLoginBack(Union union) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("code", "success");
        jsonObject.addProperty("openid", union.getOpenid());
        jsonObject.addProperty("unionid", union.getUnionid());
        return jsonObject;
    }

    /*
     * JS调用android的实现微信分享
     * */
    @JavascriptInterface
    public void shareaction(String shareMsg) {
        Map<String, String> shareMap = parseShareMsg(shareMsg);
        String title = shareMap.get("title");
        String content = shareMap.get("content");
        String url = shareMap.get("url");
        if (SocialWechatHandler.isWXAppInstalled()) {
            if (SocialWechatHandler.checkWXAppSupport()) {
                DialogUtil.showShareDialog(mContext, title, content, url);
            } else {
                DialogUtil.showInfoDialog(mContext, "提示", "抱歉，您的微信不支持分享功能，请先升级");
            }
        } else {
            DialogUtil.showInfoDialog(mContext, "提示", "您尚未安装微信,请先安装微信");
        }
    }

    private Map<String, String> parseShareMsg(String shareMsg) {

        Map<String, String> shareMap = new HashMap<>();
        if (!shareMsg.equals("")) {
            String[] shareStrings = shareMsg.split("&");
            String[] titleStrings = shareStrings[0].split("=");
            shareMap.put("title", StringUtil.decodeString(titleStrings[1]));
            String[] contentStrings = shareStrings[1].split("=");
            shareMap.put("content", StringUtil.decodeString(contentStrings[1]));
            String[] urlStrings = shareStrings[2].split("=");
            shareMap.put("url", StringUtil.decodeString(urlStrings[1]));
        }
        return shareMap;
    }

    /**
     * js调用android摄像头
     */
    @JavascriptInterface
    public void scanAction() {
        Intent intent = new Intent(mContext, QrScanActivity.class);
        ((Activity) mContext).startActivityForResult(intent, MainActivity.QRSCAN_REQUEST_CODE);
    }

    public JsonObject qrcodeback(String content) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("code", "success");
        jsonObject.addProperty("resultContent", content);
        return jsonObject;
    }

    //微信支付方法
    @JavascriptInterface
    public void payaction(String payContent) {
        if (SocialWechatHandler.isWXAppInstalled()) {
            if (SocialWechatHandler.checkWXPaySupport()){
                Map<String, String> payMap = parsePayMap(payContent);
                SocialWechatHandler.sendPayReq(payMap);
            }else {
                DialogUtil.showInfoDialog(mContext,"提示","抱歉，您的微信不支持支付功能，请先升级");
            }
        } else {
            DialogUtil.showInfoDialog(mContext, "提示", "您尚未安装微信,请先安装微信");
        }
    }

    //解析js传来的微信支付字符串
    private Map<String, String> parsePayMap(String payContent) {
        Map<String, String> payMap = new HashMap<>();
        if (!payContent.equals("") && payContent.contains("&")) {
            String[] payParameters = payContent.split("&");
            for (int i = 0; i < payParameters.length; i++) {
                String[] payParameterDetails = payParameters[i].split("=");
                payMap.put(payParameterDetails[0],payParameterDetails[1]);
            }
        }
        return payMap;
    }

    //微信支付回调
    public JsonObject payBack(int resultCode) {
        JsonObject jsonObject = new JsonObject();
        if (resultCode==1){
            jsonObject.addProperty("code", "success");
        }else {
            jsonObject.addProperty("code", "error");
        }
        return jsonObject;
    }

}
