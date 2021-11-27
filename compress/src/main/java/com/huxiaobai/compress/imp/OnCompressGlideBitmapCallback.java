package com.huxiaobai.compress.imp;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/11/27 14:32
 * 更新时间: 2021/11/27 14:32
 * 描述:
 */
public interface OnCompressGlideBitmapCallback extends OnBaseCompressGlideCallback{
    void onResult(@NonNull Bitmap bitmap);
}
