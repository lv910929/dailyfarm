package com.hitotech.dailyfarm.utils;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

import com.google.gson.JsonObject;

/**
 * Created by Lv on 2016/4/3.
 */
public class JavaScriptObject {

    Context mContxt;

    public JavaScriptObject(Context mContxt) {
        this.mContxt = mContxt;
    }

    public JsonObject client_callback() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name","张三");
        jsonObject.addProperty("password","123456");
        return jsonObject;
    }

    /*
     * JS调用android的方法
     * */
    @JavascriptInterface
    public void conntent(String result) {
        Toast.makeText(mContxt, result, Toast.LENGTH_SHORT).show();
    }
}
