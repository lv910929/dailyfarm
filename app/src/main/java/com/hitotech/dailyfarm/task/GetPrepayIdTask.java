package com.hitotech.dailyfarm.task;

import android.content.Context;
import android.os.Message;
import android.util.Log;
import android.util.Xml;

import com.hitotech.dailyfarm.data.Constant;
import com.hitotech.dailyfarm.utils.DialogUtil;
import com.hitotech.dailyfarm.utils.IHandler;
import com.hitotech.dailyfarm.wxapi.SocialWechatHandler;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Lv on 2016/4/17.
 */
public class GetPrepayIdTask extends BaseTask{

    private IHandler handler;

    private Map<String,String> prepareMap;

    private Map<String,String> resultunifiedorder;

    public GetPrepayIdTask(Context context, IHandler handler, Map<String, String> prepareMap) {
        super(context);
        this.handler = handler;
        this.prepareMap = prepareMap;
    }

    @Override
    protected void onPreExecute(){
        DialogUtil.showWaitDialog(context, "生成订单中...");
    }

    @Override
    protected Integer doInBackground(String... params) {
        final int[] result = {0};
        String productStrings = SocialWechatHandler.genProductArgs(prepareMap);
        OkHttpUtils
                .post()
                .url(Constant.WX_PAY_URL)
                .addParams("",productStrings)
                .build()
                .execute(new StringCallback() {

                    @Override
                    public void onError(Call call, Exception e) {
                        Log.e("error:", e.getMessage());
                        result[0] = -1;
                    }

                    @Override
                    public void onResponse(String response) {
                        Log.e("orion", "----"+response);
                        resultunifiedorder = decodeXml(response);
                    }
                });
        return result[0];
    }

    private Map<String,String> decodeXml(String response) {
        try {
            Map<String, String> xmlMap = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(response));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                String nodeName=parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        if("xml".equals(nodeName)==false){
                            xmlMap.put(nodeName,parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }
            return xmlMap;
        } catch (Exception e) {
            Log.e("orion","----"+e.toString());
        }
        return null;
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    protected void onPostExecute(Integer result){

        super.onPostExecute(result);
        DialogUtil.hideWaitDialog();
        if (result==0) {
            Message message = handler.getHandler().obtainMessage(1);
            message.obj = resultunifiedorder;
            message.sendToTarget();
        }
    }
}
