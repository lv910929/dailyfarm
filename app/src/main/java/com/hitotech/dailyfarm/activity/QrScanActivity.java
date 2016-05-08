package com.hitotech.dailyfarm.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.Result;
import com.hitotech.dailyfarm.R;
import com.hitotech.dailyfarm.qrcode.camera.CameraManager;
import com.hitotech.dailyfarm.qrcode.decode.DecodeThread;
import com.hitotech.dailyfarm.qrcode.utils.BeepManager;
import com.hitotech.dailyfarm.qrcode.utils.InactivityTimer;
import com.hitotech.dailyfarm.qrcode.utils.QrScanHandler;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;
import com.umeng.analytics.MobclickAgent;

import java.io.IOException;

/**
 * Created by Lv on 2016/4/10.
 */
public class QrScanActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    private static final String TAG = QrScanActivity.class.getSimpleName();

    private CameraManager cameraManager;
    private QrScanHandler handler;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;

    private Toolbar toolbarComm;
    private TextView textTitle;
    private RelativeLayout captureContainer;
    private SurfaceView capturePreview;
    private ImageView captureErrorMask;
    private FrameLayout captureCropView;
    private ImageView captureScanMask;

    private int mQrcodeCropWidth = 0;
    private int mQrcodeCropHeight = 0;
    private int mBarcodeCropWidth = 0;
    private int mBarcodeCropHeight = 0;
    private boolean isHasSurface = false;
    private ObjectAnimator mScanMaskObjectAnimator = null;

    private Rect cropRect;

    public Handler getHandler() {
        return handler;
    }

    public CameraManager getCameraManager() {
        return cameraManager;
    }

    public Rect getCropRect() {
        return cropRect;
    }

    public void setCropRect(Rect cropRect) {
        this.cropRect = cropRect;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrscan);
        initUI();
        inactivityTimer = new InactivityTimer(this);
        beepManager = new BeepManager(this);
        initCropViewAnimator();
    }

    private void initUI(){
        toolbarComm = (Toolbar) findViewById(R.id.toolbar_comm);
        textTitle = (TextView) findViewById(R.id.text_title);
        captureContainer = (RelativeLayout) findViewById(R.id.capture_container);
        capturePreview = (SurfaceView) findViewById(R.id.capture_preview);
        captureErrorMask = (ImageView) findViewById(R.id.capture_error_mask);
        captureCropView = (FrameLayout) findViewById(R.id.capture_crop_view);
        captureScanMask = (ImageView) findViewById(R.id.capture_scan_mask);

        initToolBar("二维码扫描");
    }

    //设置通用的toolbar
    private void initToolBar(String title){
        setTitle("");
        setSupportActionBar(toolbarComm);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbarComm.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        textTitle.setText(title);
    }
    /**
     * 初始化截取的矩形区域
     */
    private void initCrop() {
        int cameraWidth = cameraManager.getCameraResolution().y;
        int cameraHeight = cameraManager.getCameraResolution().x;
        int[] location = new int[2];
        captureCropView.getLocationInWindow(location);
        int cropLeft = location[0];
        int cropTop = location[1];
        int cropWidth = captureCropView.getWidth();
        int cropHeight = captureCropView.getHeight();
        int containerWidth = captureContainer.getWidth();
        int containerHeight = captureContainer.getHeight();
        int x = cropLeft * cameraWidth / containerWidth;
        int y = cropTop * cameraHeight / containerHeight;
        int width = cropWidth * cameraWidth / containerWidth;
        int height = cropHeight * cameraHeight / containerHeight;
        setCropRect(new Rect(x, y, width + x, height + y));
    }

    private void initCropViewAnimator() {
        mQrcodeCropWidth = getResources().getDimensionPixelSize(R.dimen.qrcode_crop_width);
        mQrcodeCropHeight = getResources().getDimensionPixelSize(R.dimen.qrcode_crop_height);
        mBarcodeCropWidth = getResources().getDimensionPixelSize(R.dimen.barcode_crop_width);
        mBarcodeCropHeight = getResources().getDimensionPixelSize(R.dimen.barcode_crop_height);
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            cameraManager.openDriver(surfaceHolder);
            if (handler == null) {
                handler = new QrScanHandler(this, cameraManager, DecodeThread.ALL_MODE);
            }
            onCameraPreviewSuccess();
        } catch (IOException ioe) {
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            displayFrameworkBugMessageAndExit();
        }
    }

    private void onCameraPreviewSuccess() {
        initCrop();
        captureErrorMask.setVisibility(View.GONE);

        ViewHelper.setPivotX(captureScanMask, 0.0f);
        ViewHelper.setPivotY(captureScanMask, 0.0f);

        mScanMaskObjectAnimator = ObjectAnimator.ofFloat(captureScanMask, "scaleY", 0.0f, 1.0f);
        mScanMaskObjectAnimator.setDuration(2000);
        mScanMaskObjectAnimator.setInterpolator(new DecelerateInterpolator());
        mScanMaskObjectAnimator.setRepeatCount(-1);
        mScanMaskObjectAnimator.setRepeatMode(ObjectAnimator.RESTART);
        mScanMaskObjectAnimator.start();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (holder == null) {
            Log.e(TAG, "*** WARNING *** surfaceCreated() gave us a null surface!");
        }
        if (!isHasSurface) {
            isHasSurface = true;
            initCamera(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera(holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        isHasSurface = false;
    }

    public void handleDecode(Result rawResult, Bundle bundle) {
        inactivityTimer.onActivity();
        beepManager.playBeepSoundAndVibrate();
        sendResult(rawResult.getText());
    }

    private void sendResult(String result){
        Intent intent=new Intent();
        intent.putExtra("resultContent",result);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_name));
        builder.setMessage("相机打开出错，请稍后重试");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                finish();
            }
        });
        builder.show();
    }

    @Override
    protected void onResume() {

        super.onResume();
        MobclickAgent.onResume(this);
        cameraManager = new CameraManager(getApplication());
        handler = null;
        if (isHasSurface) {
            initCamera(capturePreview.getHolder());
        } else {
            capturePreview.getHolder().addCallback(this);
        }
        inactivityTimer.onResume();
    }

    @Override
    protected void onPause() {
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        beepManager.close();
        inactivityTimer.onPause();
        cameraManager.closeDriver();
        if (!isHasSurface) {
            capturePreview.getHolder().removeCallback(this);
        }
        if (null != mScanMaskObjectAnimator && mScanMaskObjectAnimator.isStarted()) {
            mScanMaskObjectAnimator.cancel();
        }
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        super.onDestroy();
    }
}
