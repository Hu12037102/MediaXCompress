package com.huxiaobai.compress.imp;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/11/27 14:33
 * 更新时间: 2021/11/27 14:33
 * 描述:
 */
public interface OnCompressGlideImageCallback extends OnBaseCompressGlideCallback{
    void onResult(@NonNull File file);
}
