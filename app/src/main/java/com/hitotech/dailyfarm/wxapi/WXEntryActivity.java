package com.hitotech.dailyfarm.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import com.google.gson.Gson;
import com.hitotech.dailyfarm.application.ContextApplication;
import com.hitotech.dailyfarm.data.Constant;
import com.hitotech.dailyfarm.entity.Token;
import com.hitotech.dailyfarm.utils.DialogUtil;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.Call;

/**
 * Created by Lv on 2016/4/4.
 */
public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private String code = "";

    private void handleIntent(Intent paramIntent) {
        ContextApplication.api.handleIntent(paramIntent, this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
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
        finish();
    }

    @Override
    public void onResp(BaseResp baseResp) {
        switch (baseResp.errCode) {
            case BaseResp.ErrCode.ERR_OK://用户同意
                code = ((SendAuth.Resp) baseResp).code; //即为所需的code
                System.out.println("微信确认登录返回的code：" + code);
                finish();
                break;
            case BaseResp.ErrCode.ERR_AUTH_DENIED://用户拒绝授权

                break;
            case BaseResp.ErrCode.ERR_USER_CANCEL://用户取消

                break;
        }
    }

    private void getOpenId() {
        if (!code.equals("")) {
            OkHttpUtils
                    .get()
                    .url(Constant.WX_LOGIN_URL)
                    .addParams("appid", Constant.APP_ID)
                    .addParams("secret", Constant.APP_SECRETT)
                    .addParams("code",code)
                    .addParams("grant_type","authorization_code")
                    .build()
                    .execute(new StringCallback() {
                        @Override
                        public void onError(Call call, Exception e) {
                            Log.e("error:",e.getMessage());
                        }

                        @Override
                        public void onResponse(String response) {
                            Log.i("response:",response);
                            parseData(response);
                        }
                    });
        }
    }

    private void refreshToken(){

    }

    private void parseData(String JsonData){
        try {
            JSONObject jsonObject = new JSONObject(JsonData);
            if (jsonObject.get("errcode")==null){//说明授权成功
                DialogUtil.showSuccessDialog(WXEntryActivity.this,"授权登陆成功!");
                Token token=new Gson().fromJson(JsonData,Token.class);
            }else {//说明授权失败
                DialogUtil.showErrorDialog(WXEntryActivity.this,"授权登录失败！");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
