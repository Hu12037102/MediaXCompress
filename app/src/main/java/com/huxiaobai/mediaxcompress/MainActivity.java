package com.huxiaobai.mediaxcompress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;

import com.huxiaobai.compress.CompressGlide;
import com.huxiaobai.compress.imp.OnBaseCompressGlideCallback;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        CompressGlide.from("")
                .compressWidth(960)
                .compressHeight(540)
                .config(Bitmap.Config.ARGB_8888)
                .format(Bitmap.CompressFormat.PNG)
                .compressBitmapSize(120)
                .create()
                .addLifecycle(this.getLifecycle())
                .asyncCompressBitmap(new OnBaseCompressGlideCallback.OnCompressGlideBitmapCallback() {
                    @Override
                    public void onResult(@NonNull Bitmap bitmap) {

                    }
                });

    }
}