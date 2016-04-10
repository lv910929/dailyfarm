package com.hitotech.dailyfarm.utils;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.hitotech.dailyfarm.adapter.SocializeAdapter;
import com.hitotech.dailyfarm.wxapi.SocialWechatHandler;

import me.drakeet.materialdialog.MaterialDialog;

public class DialogUtil {

    private static ProgressDialog progressDialog;

    private static SVProgressHUD progressHUD;

    public static void showInfoDialog(Context context, String title, String message) {
        new AlertDialog.Builder(context)
                .setMessage(message)
                .setTitle(title)
                .setCancelable(false)
                .setPositiveButton("确定", null)
                .show();
    }

    public static void showWaitDialog(Context context, String message) {
        if (!((Activity) context).isFinishing()) {
            progressDialog = new ProgressDialog(context);
            // progressDialog.setTitle(title);
            progressDialog.setMessage(message);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }
    }

    public static void hideWaitDialog() {
        if (null != progressDialog && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    public static void releaseWaitDialog() {
        progressDialog = null;
    }

    public static void showSuccessDialog(Context context, String message) {
        if (!((Activity) context).isFinishing()) {
            progressHUD = new SVProgressHUD(context);
            progressHUD.showSuccessWithStatus(message);
        }
    }

    public static void showErrorDialog(Context context, String message){
        if (!((Activity) context).isFinishing()) {
            progressHUD = new SVProgressHUD(context);
            progressHUD.showErrorWithStatus(message);
        }
    }

    public static void showShareDialog(final Context context, final String title, final String description,final String url) {
        SocializeAdapter socializeAdapter = new SocializeAdapter(context);
        ListView listView = new ListView(context);
        listView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        float scale = context.getResources().getDisplayMetrics().density;
        int dpAsPixels = (int) (8 * scale + 0.5f);
        listView.setPadding(0, 0, 0, 0);
        listView.setDividerHeight(0);
        listView.setAdapter(socializeAdapter);

        final MaterialDialog alert = new MaterialDialog(context)
                .setTitle("分享到")
                .setCanceledOnTouchOutside(true)
                .setContentView(listView);
        /*alert.setNegativeButton("取消", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               alert.dismiss();
            }
        });*/
        alert.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    SocialWechatHandler.sendWebPageToWX(context,title,description,url,true);
                } else {
                    SocialWechatHandler.sendWebPageToWX(context, title, description, url, false);
                }
                alert.dismiss();
            }
        });
    }
}
