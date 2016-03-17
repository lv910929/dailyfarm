package com.hitotech.dailyfarm.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.GeolocationPermissions;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.hitotech.dailyfarm.R;

public class MainActivity extends AppCompatActivity {

    public static final String URL_STRING = "file:///android_asset/day1.html";

    private WebView webView;

    SVProgressHUD progressHUD;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView= (WebView) findViewById(R.id.webView);
        setWebView();
        webView.loadUrl(URL_STRING);
    }

    private void setWebView(){
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
        // 最重要的方法，一定要设置，这就是出不来的主要原因
        webSettings.setDomStorageEnabled(true);
        // 设置Web视图
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onJsAlert(WebView view, String url, String message,
                                     JsResult result) {
                return super.onJsAlert(view, url, message, result);
            }

            @Override
            public void onGeolocationPermissionsShowPrompt(String origin,
                                                           GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
                super.onGeolocationPermissionsShowPrompt(origin, callback);
            }
        });
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

    // Web视图
    private class MyWebViewClient extends WebViewClient {

        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url != null && url.startsWith("mailto:")
                    || url.startsWith("geo:") || url.startsWith("tel:")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            webView.loadUrl("file:///android_asset/day1.html");
        }

        public void onLoadResource(WebView view, String url) {
            if (progressHUD == null){
                progressHUD = new SVProgressHUD(MainActivity.this);
                progressHUD.showWithStatus("加载中...");
            }
        }

        public void onPageFinished(WebView view, String url) {

            if(!webView.getSettings().getLoadsImagesAutomatically()) {
                webView.getSettings().setLoadsImagesAutomatically(true);
            }
            if (progressHUD.isShowing()){
                progressHUD.dismiss();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.destroy();
        }
        super.onDestroy();
    }
}
