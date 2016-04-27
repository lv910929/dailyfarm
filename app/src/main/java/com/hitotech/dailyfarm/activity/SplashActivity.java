package com.hitotech.dailyfarm.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

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
        initImage();
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
            public void onAnimationEnd(Animation animation) {
                startActivity();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
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

    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }
}
