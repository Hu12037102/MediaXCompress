package com.huxiaobai.mediaxcompress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.huxiaobai.compress.CompressGlide;
import com.huxiaobai.compress.imp.OnBaseCompressGlideCallback;
import com.huxiaobai.compress.imp.OnCompressGlideBitmapCallback;
import com.huxiaobai.compress.imp.OnCompressGlideImageCallback;
import com.huxiaobai.compress.utils.FileUtils;

import java.io.File;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String path = FileUtils.getPublicRootDirectory(this).getAbsolutePath() + "/IMG_20211127_172424.jpg";
      //  String path = FileUtils.getPublicRootDirectory(this).getAbsolutePath() + "/huge.jpg";
        File file = new File(path);
        Log.w("MainActivity--", file.getAbsolutePath() + "---" + FileUtils.exitFile(file));
        CompressGlide.fromImage(file)
                .compressWidth(960)
                .compressHeight(540)
                .config(Bitmap.Config.ARGB_8888)
                .format(Bitmap.CompressFormat.PNG)
                .compressBitmapSize(120)
                .create(MainActivity.this)
                .addLifecycle(this.getLifecycle())
                .asyncCompressImage(new OnCompressGlideImageCallback() {
                    @Override
                    public void onResult(@NonNull File file) {
                        Log.w("asyncCompressImage", "我成功了" + file.getAbsolutePath() + "--");
                    }

                    @Override
                    public void onStart() {
                        Log.w("asyncCompressImage", "我开始了");
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.w("asyncCompressImage", "我失败了");

                    }

                    @Override
                    public void onCancel() {
                        Log.w("asyncCompressImage", "我取消了");
                    }
                });

    }
}