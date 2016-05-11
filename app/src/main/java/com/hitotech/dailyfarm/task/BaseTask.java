package com.hitotech.dailyfarm.task;


import android.content.Context;
import android.os.AsyncTask;

import com.hitotech.dailyfarm.utils.DialogUtil;


public abstract class BaseTask extends AsyncTask<String, Integer, Integer> {

    protected static byte[] bufferData = new byte[1024];

    protected Context context;

    public BaseTask(Context context) {
        this.context = context;
    }

    @Override
    protected void onPostExecute(Integer result) {
        DialogUtil.hideWaitDialog();
        switch (result) {
            case -1:
                DialogUtil.showErrorDialog(context, "生成订单失败，请稍后再试");
                //Toast.makeText(context, "服务器通信超时，请稍后再试", Toast.LENGTH_SHORT).show();
                break;
            case -2:
                DialogUtil.showErrorDialog(context, "服务器连接失败，请检查网络状态");
                //Toast.makeText(context, "服务器连接失败，请检查网络状态", Toast.LENGTH_SHORT).show();
                break;
        }
    }
}
