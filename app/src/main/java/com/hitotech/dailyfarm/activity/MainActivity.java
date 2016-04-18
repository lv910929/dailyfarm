package com.hitotech.dailyfarm.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.hitotech.dailyfarm.R;
import com.hitotech.dailyfarm.data.Constant;
import com.hitotech.dailyfarm.entity.Union;
import com.hitotech.dailyfarm.utils.IHandler;
import com.hitotech.dailyfarm.webview.JavaScriptObject;
import com.hitotech.dailyfarm.webview.MyWebChromeClient;
import com.hitotech.dailyfarm.webview.MyWebViewClient;

import java.util.Map;

public class MainActivity extends AppCompatActivity implements IHandler {

    public static final Integer QRSCAN_REQUEST_CODE = 100;

    private WebView webView;

    private JavaScriptObject javaScriptObject;
    private MyWebViewClient myWebViewClient;
    private MyWebChromeClient myWebChromeClient;

    public static android.os.Handler handler;

    private Map<String, String> resultunifiedorder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        javaScriptObject = new JavaScriptObject(this);
        webView = (WebView) findViewById(R.id.webView);
        setWebView();
        webView.loadUrl(Constant.URL_STRING);
        initHandler();
    }

    private void setWebView() {
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
        if (Build.VERSION.SDK_INT >= 19) {
            webView.getSettings().setLoadsImagesAutomatically(true);
        } else {
            webView.getSettings().setLoadsImagesAutomatically(false);
        }
        addJavascriptInterface();
    }

    private void addJavascriptInterface() {
        webView.addJavascriptInterface(new JavaScriptObject(this), "bluet");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == QRSCAN_REQUEST_CODE) {
            Bundle bundle = data.getExtras(); //data为B中回传的Intent
            String content = bundle.getString("resultContent");//str即为回传的值
            Toast.makeText(MainActivity.this,content,Toast.LENGTH_SHORT).show();
            Log.e("qrcodeback--------",javaScriptObject.qrcodeback(content).toString());
            webView.loadUrl("javascript:qrcodeback('" + javaScriptObject.qrcodeback(content).toString()+"')");
        }
    }

    private void initHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0://微信登录成功
                        Union union = (Union) msg.obj;
                        if (union != null) {
                            Toast.makeText(MainActivity.this,"授权登录成功！",Toast.LENGTH_SHORT).show();
                            webView.loadUrl("javascript:otherLoginBack('"+
                                    javaScriptObject.otherLoginBack(union).toString()+"')");
                        }
                        break;
                    case 1://支付成功
                        Toast.makeText(MainActivity.this,"支付成功！",Toast.LENGTH_SHORT).show();
                        webView.loadUrl("javascript:payBack('"
                                + javaScriptObject.payBack(1).toString() + "')");
                        break;
                    case 2://支付失败
                        Toast.makeText(MainActivity.this,"支付失败！",Toast.LENGTH_SHORT).show();
                        webView.loadUrl("javascript:payBack('"
                                + javaScriptObject.payBack(2).toString() + "')");
                        break;
                    default:
                        break;
                }
            }
        };
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

    @Override
    public Handler getHandler() {
        return handler;
    }
}
