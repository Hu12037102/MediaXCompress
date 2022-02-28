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
public interface OnBaseCompressGlideCallbacks {
    default void onStart() {
    }

    default void onError(String errorMessage) {
    }

    //default void onCancel(){}




}
