package com.hitotech.dailyfarm.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.Toast;

import com.hitotech.dailyfarm.R;
import com.umeng.analytics.MobclickAgent;
import com.umeng.message.IUmengRegisterCallback;
import com.umeng.message.MsgConstant;
import com.umeng.message.PushAgent;

public class SplashActivity extends AppCompatActivity {

    private ImageView startImage;

    private PushAgent mPushAgent;

    public Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);
        handler = new Handler();
        mPushAgent = PushAgent.getInstance(this);
        // sdk关闭通知声音
        mPushAgent.setNotificationPlaySound(MsgConstant.NOTIFICATION_PLAY_SDK_DISABLE);
        mPushAgent.onAppStart();
        //开启推送并设置注册的回调处理
        mPushAgent.enable();
        startImage = (ImageView) findViewById(R.id.iv_start);
        if (!checkNetWork()) {
            showWarnDialog();
        }else {
            initImage();
        }
    }

    private void initImage() {
        startImage.setImageResource(R.mipmap.bg_start);

        final ScaleAnimation scaleAnim = new ScaleAnimation(1.0f, 1.2f, 1.0f, 1.2f,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scaleAnim.setFillAfter(true);
        scaleAnim.setDuration(3000);
        scaleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (checkNetWork()) {
                    startActivity();
                } else {
                    Toast.makeText(getApplicationContext(), "无网络可用", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
        startImage.startAnimation(scaleAnim);
    }

    private void startActivity() {
        Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(intent);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        finish();
    }

    public IUmengRegisterCallback mRegisterCallback = new IUmengRegisterCallback() {

        @Override
        public void onRegistered(String registrationId) {

            handler.post(new Runnable() {

                @Override
                public void run() {
                    String deviceToken = mPushAgent.getRegistrationId();
                    Log.e("deviceToken-------",deviceToken);
                }
            });
        }
    };

    /**
     * 对网络连接状态进行判断
     * @return true, 可用; false， 不可用
     */
    @SuppressWarnings("deprecation")
    private boolean checkNetWork() {

        boolean result = false;
        ConnectivityManager mConnectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        // 检查网络连接，如果无网络可用，就不需要进行连网操作等
        NetworkInfo info = mConnectivity.getActiveNetworkInfo();
        if (info == null || !mConnectivity.getBackgroundDataSetting()) {
            result = false;
        } else {
            NetworkInfo[] infos = mConnectivity.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < infos.length; i++) {
                    if (infos[i].getState() == NetworkInfo.State.CONNECTED) {
                        result = true;
                    }
                }
            }
        }
        return result;
    }

    private void showWarnDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("没有可用的网络").setMessage("是否对网络进行设置?");
        builder.setPositiveButton("是",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        redirectToNETWORK();
                    }
                })
                .setNegativeButton("否",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int which) {
                                dialog.cancel();
                                finish();
                            }
                        }).show();
    }

    /**
     * 跳转到网络设置界面
     */
    private void redirectToNETWORK() {
        Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
        startActivityForResult(intent, 0);
        return;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        initImage();
    }

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
