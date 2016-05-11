package com.hitotech.dailyfarm.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.google.gson.Gson;
import com.hitotech.dailyfarm.activity.MainActivity;
import com.hitotech.dailyfarm.application.ContextApplication;
import com.hitotech.dailyfarm.data.Constant;
import com.hitotech.dailyfarm.entity.Token;
import com.hitotech.dailyfarm.entity.Union;
import com.hitotech.dailyfarm.utils.DialogUtil;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import okhttp3.Call;

/**
 * Created by Lv on 2016/4/4.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private String code = "";

    private SVProgressHUD progressHUD;

    private void handleIntent(Intent paramIntent) {
        ContextApplication.api.handleIntent(paramIntent, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        // TODO Auto-generated method stub
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        SendMessageToWX res;
        if (baseResp instanceof SendMessageToWX.Resp) {
            finish();
            return;
        }
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK://用户同意
                code = ((SendAuth.Resp) baseResp).code; //即为所需的code
                getOpenId();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝授权
                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消
                break;
        }
        finish();
    }

    private void getOpenId() {
        if (!code.equals("")) {
            DialogUtil.showHubWaitDialog(WXEntryActivity.this, "登录中...");
            OkHttpUtils
                    .post()
                    .url(Constant.WX_LOGIN_TOKEN)
                    .addParams("appid", Constant.APP_ID)
                    .addParams("secret", Constant.APP_SECRET)
                    .addParams("code", code)
                    .addParams("grant_type", "authorization_code")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            Log.e("error:", e.getMessage());
                            DialogUtil.hideHubWaitDialog();
                            DialogUtil.showErrorDialog(WXEntryActivity.this, "授权登录失败！");
                        }

                        @Override
                        public void onResponse(String response) {
                            Log.i("response:", response);
                            DialogUtil.hideHubWaitDialog();
                            Token token = parseToken(response);
                            getUnionId(token);
                        }
                    });
        } else {
            finish();
        }
    }

    private Token parseToken(String JsonData) {
        Token token = null;
        if (!JsonData.contains("errcode")) {
            token = new Gson().fromJson(JsonData, Token.class);
        } else {//说明授权失败
            DialogUtil.showErrorDialog(WXEntryActivity.this, "授权登录失败！");
            finish();
        }
        return token;
    }

    private void getUnionId(Token token) {
        if (token != null) {
            DialogUtil.showHubWaitDialog(WXEntryActivity.this, "登录中...");
            OkHttpUtils
                    .post()
                    .url(Constant.WX_LOGIN_UNION)
                    .addParams("access_token", token.getAccess_token())
                    .addParams("openid", token.getOpenid())
                    .addParams("lang", "zh_CN")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            Log.e("error:", e.getMessage());
                            DialogUtil.hideHubWaitDialog();
                            DialogUtil.showErrorDialog(WXEntryActivity.this, "授权登录失败！");
                            finish();
                        }

                        @Override
                        public void onResponse(String response) {
                            Log.i("response:", response);
                            DialogUtil.hideHubWaitDialog();
                            parseUnionId(response);
                        }
                    });
        }
    }

    private void parseUnionId(String JsonData) {
        Union union = null;
        if (!JsonData.contains("errcode")) {
            union = new Gson().fromJson(JsonData, Union.class);
            sendMsg(union);
        } else {//说明授权失败
            DialogUtil.showErrorDialog(WXEntryActivity.this, "授权登录失败！");
            finish();
        }
    }

    private void sendMsg(Union union) {
        Message message = null;
        message = MainActivity.handler.obtainMessage(0);
        message.obj = union;
        message.sendToTarget();
        finish();
    }
}
