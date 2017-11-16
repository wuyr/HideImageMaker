package com.wuyr.hideimagemaker.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.internal.SnackbarContentLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.ContentLoadingProgressBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.wuyr.hideimagemaker.R;
import com.wuyr.hideimagemaker.utils.BitmapPixelUtil;
import com.wuyr.hideimagemaker.utils.LogUtil;
import com.wuyr.hideimagemaker.utils.MySnackBar;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by wuyr on 17-11-13 下午10:08.
 */

public class MainActivity extends AppCompatActivity {

    private final int INPUT_IMAGE_1 = 1, INPUT_IMAGE_2 = 2;
    private int mScreenWidth;
    private int mStatusFlag;
    private int mLastClickedId;
    private boolean isInputImage1Ready, isInputImage2Ready, isHanding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_main_view);
        init();
    }

    private void init() {
        LogUtil.setDebugLevel(LogUtil.ERROR);
        View.OnClickListener onClickListener = v -> {
            mLastClickedId = v.getId();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                verifyStoragePermissions();
            else
                handleOnClickEvent();
        };
        findViewById(R.id.input_image_view1).setOnClickListener(onClickListener);
        findViewById(R.id.input_image_view2).setOnClickListener(onClickListener);
        findViewById(R.id.output_image_view).setOnClickListener(onClickListener);
        findViewById(R.id.menu_settings).setOnClickListener(onClickListener);
        findViewById(R.id.start_btn).setOnClickListener(onClickListener);
        findViewById(R.id.output_image_view).setOnLongClickListener(v -> {
            if (((ImageView) v).getDrawable() != null) {
                Bitmap bitmap = ((BitmapDrawable) ((ImageView) v).getDrawable()).getBitmap();
                try {
                    String savePath = Environment.getExternalStorageDirectory().getPath() + "/HideImageMaker/";
                    String fileName = System.currentTimeMillis() + ".png";
                    File savePathFile = new File(savePath);
                    if (!savePathFile.exists())
                        //noinspection ResultOfMethodCallIgnored
                        savePathFile.mkdirs();
                    File bitmapFile = new File(savePathFile, fileName);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, new FileOutputStream(bitmapFile));
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(bitmapFile)));
                    MySnackBar.show(findViewById(R.id.root_view), getString(R.string.saved_to) + savePath, Snackbar.LENGTH_LONG);
                } catch (Exception e) {
                    e.printStackTrace();
                    MySnackBar.show(findViewById(R.id.root_view), getString(R.string.save_failed), Snackbar.LENGTH_LONG);
                }
            }
            return true;
        });
        findViewById(R.id.start_btn).post(() -> {
            View rootView = findViewById(R.id.input_image_root_view);
            mScreenWidth = rootView.getWidth();
            ViewGroup.LayoutParams layoutParams = rootView.getLayoutParams();
            layoutParams.height = mScreenWidth / 2;
            rootView.requestLayout();
        });
    }

    private void showAboutDialog() {
        new AlertDialog.Builder(this).setView(R.layout.dialog_about_view)
                .setPositiveButton(R.string.alipay, (dialog, which) -> {
                    dialog.dismiss();
                    showAliPayDialog();
                })
                .setNeutralButton(R.string.no, null).setNegativeButton(R.string.wechat, (dialog, which) -> {
            dialog.dismiss();
            showWeChatDialog();
        }).setCancelable(false).show();
    }

    private void showAliPayDialog() {
        new AlertDialog.Builder(this).setView(R.layout.dialog_alipay_view).show();
        Toast.makeText(this, R.string.thanks, Toast.LENGTH_LONG).show();
    }

    private void showWeChatDialog() {
        new AlertDialog.Builder(this).setView(R.layout.dialog_wechat_view).show();
        Toast.makeText(this, R.string.thanks, Toast.LENGTH_LONG).show();
    }

    private void handleOnClickEvent() {
        switch (mLastClickedId) {
            case R.id.input_image_view1:
                handleInputImageEvent(INPUT_IMAGE_1);
                break;
            case R.id.input_image_view2:
                handleInputImageEvent(INPUT_IMAGE_2);
                break;
            case R.id.output_image_view:
                if (!isHanding) {
                    ImageView imageView = (ImageView) findViewById(R.id.output_image_view);
                    if (imageView.getDrawable() != null) {
                        imageView.setBackgroundColor(mStatusFlag % 2 == 0 ? Color.BLACK : Color.WHITE);
                        mStatusFlag++;
                    }
                }
                break;
            case R.id.start_btn:
                handleStartButtonEvent();
                break;
            case R.id.menu_settings:
                showAboutDialog();
                break;
            default:
                break;
        }
    }

    private void handleInputImageEvent(int requestCode) {
        if (!isHanding)
            startActivityForResult(new Intent(MainActivity.this, ImageSelectorActivity.class), requestCode);
    }

    private void handleStartButtonEvent() {
        if (isInputImage1Ready && isInputImage2Ready) {
            isInputImage1Ready = isInputImage2Ready = false;
            new Thread() {
                @Override
                public void run() {
                    isHanding = true;
                    Bitmap bitmap1 = ((BitmapDrawable) ((ImageView) findViewById(R.id.input_image_view1)).getDrawable()).getBitmap();
                    Bitmap bitmap2 = ((BitmapDrawable) ((ImageView) findViewById(R.id.input_image_view2)).getDrawable()).getBitmap();
                    if (bitmap1.getByteCount() > bitmap2.getByteCount()) {
                        bitmap1 = BitmapPixelUtil.scaleBitmap(bitmap1, bitmap2.getWidth(), bitmap2.getHeight());
                    } else if (bitmap1.getByteCount() < bitmap2.getByteCount()) {
                        bitmap2 = BitmapPixelUtil.scaleBitmap(bitmap2, bitmap1.getWidth(), bitmap1.getHeight());
                    }
                    Bitmap resultBitmap = BitmapPixelUtil.makeHideImage(bitmap1, bitmap2, progress -> runOnUiThread(() ->
                            ((ContentLoadingProgressBar) findViewById(R.id.progress_bar)).setProgress((int) (progress * 100))));
                    runOnUiThread(() -> {
                        ((ImageView) findViewById(R.id.output_image_view)).setImageBitmap(resultBitmap);
                        isInputImage1Ready = isInputImage2Ready = true;
                        isHanding = false;
                        Snackbar snackbar = MySnackBar.show(findViewById(R.id.root_view),
                                getString(R.string.finish), Snackbar.LENGTH_INDEFINITE);
                        try {
                            Snackbar.SnackbarLayout root = (Snackbar.SnackbarLayout) snackbar.getView();
                            SnackbarContentLayout contentLayout = (SnackbarContentLayout) root.getChildAt(0);
                            Class<SnackbarContentLayout> clazz = SnackbarContentLayout.class;
                            Method method = clazz.getDeclaredMethod("getMessageView");
                            method.setAccessible(true);
                            TextView tv = (TextView) method.invoke(contentLayout);
                            tv.setMaxLines(10);
                        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                        snackbar.setAction(R.string.i_know, v -> snackbar.dismiss()).setActionTextColor(getResources().getColor(R.color.md_green_A400));
                    });
                }
            }.start();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && data != null) {
            ImageView imageView = null;
            if (requestCode == INPUT_IMAGE_1) {
                imageView = (ImageView) findViewById(R.id.input_image_view1);
                findViewById(R.id.input_image_view1_text).setVisibility(View.INVISIBLE);
                isInputImage1Ready = true;
            } else if (requestCode == INPUT_IMAGE_2) {
                imageView = (ImageView) findViewById(R.id.input_image_view2);
                findViewById(R.id.input_image_view2_text).setVisibility(View.INVISIBLE);
                isInputImage2Ready = true;
            }
            if (imageView != null) {
                Bitmap bitmap = BitmapFactory.decodeFile(data.getStringExtra("path")).copy(Bitmap.Config.ARGB_8888, true);
                imageView.setImageBitmap(bitmap);
                int width = mScreenWidth / 2;
                if (bitmap.getWidth() > width) {
                    bitmap = BitmapPixelUtil.scaleBitmap(bitmap, width, (int) ((float) width / bitmap.getWidth() * bitmap.getHeight()));
                    imageView.setImageBitmap(bitmap);
                }
            }
        } else super.onActivityResult(requestCode, resultCode, data);
    }

    private long lastPressedTime;

    @Override
    public void onBackPressed() {
        if ((System.currentTimeMillis() - lastPressedTime) < 2000)
            finish();
        lastPressedTime = System.currentTimeMillis();
        MySnackBar.show(findViewById(R.id.root_view),
                getString(R.string.press_back_exit), Snackbar.LENGTH_SHORT);
    }

    @Override
    public void finish() {
        super.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED)
            MySnackBar.show(findViewById(R.id.root_view), getString(R.string.no_permission), Snackbar.LENGTH_LONG);
        else
            handleOnClickEvent();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};

    private void verifyStoragePermissions() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE);
        else
            handleOnClickEvent();
    }
}
