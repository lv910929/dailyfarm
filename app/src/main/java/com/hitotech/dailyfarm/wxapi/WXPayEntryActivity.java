package com.hitotech.dailyfarm.wxapi;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.hitotech.dailyfarm.R;
import com.hitotech.dailyfarm.activity.MainActivity;
import com.hitotech.dailyfarm.application.ContextApplication;
import com.tencent.mm.sdk.constants.ConstantsAPI;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;

public class WXPayEntryActivity extends AppCompatActivity implements IWXAPIEventHandler {

    private static final String TAG = "WXPayEntryActivity";

    private Toolbar toolbar;

    private void handleIntent(Intent paramIntent) {
        ContextApplication.api.handleIntent(paramIntent, this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wxpay_entry);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        initToolBar();
        handleIntent(getIntent());
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
        handleIntent(intent);
    }

    @Override
    public void onReq(BaseReq baseReq) {

    }

    @Override
    public void onResp(BaseResp baseResp) {
        Log.d(TAG, "onPayFinish, errCode = " + baseResp.errCode);
        if (baseResp.getType()== ConstantsAPI.COMMAND_PAY_BY_WX){
            switch (baseResp.errCode){
                case BaseResp.ErrCode.ERR_OK:
                    showDialog("支付成功！",1);
                    break;
                default:
                    showDialog("支付失败！",2);
                    break;
            }
        }
    }

    private void showDialog(String message, final int resultCode){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage(message);
        builder.setCancelable(false);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Message message = null;
                message = MainActivity.handler.obtainMessage(resultCode);
                message.sendToTarget();
                finish();
            }
        });
        builder.show();
    }
}
