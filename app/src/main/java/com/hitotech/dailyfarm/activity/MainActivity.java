package com.hitotech.dailyfarm.activity;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.Toast;

import com.hitotech.dailyfarm.R;
import com.hitotech.dailyfarm.data.Constant;
import com.hitotech.dailyfarm.utils.DialogUtil;
import com.hitotech.dailyfarm.utils.JavaScriptObject;
import com.hitotech.dailyfarm.webview.MyWebChromeClient;
import com.hitotech.dailyfarm.webview.MyWebViewClient;
import com.hitotech.dailyfarm.wxapi.SocialWechatHandler;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private WebView webView;
    private Button testJSButton;

    private JavaScriptObject javaScriptObject;
    private MyWebViewClient myWebViewClient;
    private MyWebChromeClient myWebChromeClient;

    private Handler handler = new Handler();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        javaScriptObject = new JavaScriptObject(this);
        webView= (WebView) findViewById(R.id.webView);
        testJSButton = (Button) findViewById(R.id.btn_test_js);
        setWebView();
        webView.loadUrl(Constant.URL_STRING);
        testJSButton.setOnClickListener(this);
    }

    private void setWebView(){
        myWebChromeClient = new MyWebChromeClient();
        myWebViewClient = new MyWebViewClient(MainActivity.this);
        WebSettings webSettings = webView.getSettings();
        // 设置WebView属性，能够执行Javascript脚本
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        // 设置可以访问文件
        webSettings.setAllowFileAccess(true);
        // 设置支持缩放
        webSettings.setBuiltInZoomControls(false);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        // 启用数据库
        webSettings.setDatabaseEnabled(true);
        String dir = this.getApplicationContext()
                .getDir("database", MODE_PRIVATE).getPath();
        // 启用地理定位
        webSettings.setGeolocationEnabled(true);
        // 设置定位的数据库路径
        webSettings.setGeolocationDatabasePath(dir);
        webSettings.setDomStorageEnabled(true);
        webView.setWebViewClient(myWebViewClient);
        webView.setWebChromeClient(myWebChromeClient);
        webView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return true;
            }
        });
        if(Build.VERSION.SDK_INT >= 19) {
            webView.getSettings().setLoadsImagesAutomatically(true);
        } else {
            webView.getSettings().setLoadsImagesAutomatically(false);
        }
        addJavascriptInterface();
    }

    private void addJavascriptInterface(){
        webView.addJavascriptInterface(new JavaScriptObject(this),"bluet");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_test_js:
                // 传递参数调用
                //webView.loadUrl("javascript:client_callback(" + javaScriptObject.client_callback() + ")");
                //DialogUtil.showSuccessDialog(MainActivity.this, "支付成功！");
                //DialogUtil.showShareDialog(MainActivity.this);
                if (SocialWechatHandler.isWXAppInstalled()){
                    if (SocialWechatHandler.checkWXAppSupport()){
                        //DialogUtil.showShareDialog(MainActivity.this);
                        SocialWechatHandler.registerToWX();
                    }else {
                        DialogUtil.showInfoDialog(MainActivity.this,"提示","抱歉，您的微信不支持分享功能，请先升级");
                    }
                }else {
                    DialogUtil.showInfoDialog(MainActivity.this,"提示","您尚未安装微信,请先安装微信");
                }

                break;
        }
    }
    /**
     * 重写返回键点击事件
     */
    private long mExitTime;

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                if ((System.currentTimeMillis() - mExitTime) > 2000) {
                    Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show();
                    mExitTime = System.currentTimeMillis();
                } else {
                    finish();
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
