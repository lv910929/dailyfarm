package com.hitotech.dailyfarm.wxapi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.hitotech.dailyfarm.R;
import com.hitotech.dailyfarm.data.Constant;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

public class WXPayEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    private IWXAPI api;

    private Toolbar toolbar;

    SVProgressHUD progressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxpay_entry);
        api = WXAPIFactory.createWXAPI(this, Constant.APP_ID);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        initToolBar();
        api.handleIntent(getIntent(), this);
    }

    private void initToolBar(){
        setTitle("支付结果");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d(TAG, "onPayFinish, errCode = " + baseResp.errCode);
        if (baseResp.getType()== ConstantsAPI.COMMAND_PAY_BY_WX){
            if (progressHUD==null){
                progressHUD = new SVProgressHUD(WXPayEntryActivity.this);
                if (baseResp.errCode==0) {
                    progressHUD.showSuccessWithStatus("恭喜您，支付成功！");
                }else {
                    progressHUD.showErrorWithStatus(baseResp.errStr);
                }
            }
        }
    }
}
