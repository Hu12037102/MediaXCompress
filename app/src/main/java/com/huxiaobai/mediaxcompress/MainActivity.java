package com.huxiaobai.mediaxcompress;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;

import com.baixiaohu.permission.PermissionActivity;
import com.baixiaohu.permission.imp.OnPermissionsResult;
import com.huxiaobai.compress.CompressGlide;
import com.huxiaobai.compress.imp.OnCompressGlideImageCallback;
import com.huxiaobai.compress.utils.FileUtils;

import java.io.File;
import java.util.List;

public class MainActivity extends PermissionActivity {

    private AppCompatImageView mAivContentOriginal;
    private String mImagePath;
    private AppCompatImageView mAivContentCompress;

    @Override
    @SuppressLint("IntentReset")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mAivContentOriginal = findViewById(R.id.aiv_content_original);
        mAivContentCompress = findViewById(R.id.aiv_content_compress);

        findViewById(R.id.atv_compress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPermission();


            }
        });
        mAivContentOriginal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            //  ClipData clipData = data.getClipData();
            //  Log.w("onActivityResult--", clipData.getItemCount() + "--");
            Uri uri = data.getData();
            mAivContentOriginal.setImageURI(uri);
            mImagePath = FileUtils.getMediaPath(this, uri);
            Log.w("onActivityResult", uri + "--"+mImagePath);
        }
    }

    private void requestPermission() {
        requestPermission(new OnPermissionsResult() {
            @Override
            public void onAllow(List<String> allowPermissions) {
                Log.w("MainActivity--", FileUtils.exitFile(mImagePath)+"--");

                CompressGlide.fromImage()
                        .compressHeight(1280)
                        .compressWith(720)
                        .config(Bitmap.Config.RGB_565)
                        .compressBitmapSize(120)
                        .create(MainActivity.this)
                        .addLifecycle(getLifecycle())
                        .compressImage(mImagePath, new OnCompressGlideImageCallback() {
                            @Override
                            public void onResult(@NonNull File file) {
                                mAivContentCompress.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                Log.w("asyncCompressImage", "我成功了" + file.getAbsolutePath() + "--");
                            }

                            @Override
                            public void onStart() {
                                Log.w("asyncCompressImage", "我开始了");
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.w("asyncCompressImage", "我失败了" + errorMessage);

                            }

                            @Override
                            public void onCancel() {
                                Log.w("asyncCompressImage", "我取消了");
                            }
                        });


            }

            @Override
            public void onNoAllow(List<String> noAllowPermissions) {

            }

            @Override
            public void onForbid(List<String> noForbidPermissions) {

            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }
}