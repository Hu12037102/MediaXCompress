package com.huxiaobai.compress.imp;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/11/26 11:34
 * 更新时间: 2021/11/26 11:34
 * 描述:
 */
public interface OnBaseCompressGlideCallback {
    default void onStart() {
    }

    default void onError(String errorMessage) {
    }

    default void onCancel(){}

    interface OnCompressGlideBitmapCallback extends OnBaseCompressGlideCallback {
        void onResult(@NonNull Bitmap bitmap);
    }

    interface OnCompressGlideImageCallback extends OnBaseCompressGlideCallback {
        void onResult(@NonNull File file);
    }
}
