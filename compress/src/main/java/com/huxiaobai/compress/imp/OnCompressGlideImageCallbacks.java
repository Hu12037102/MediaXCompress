package com.huxiaobai.compress.imp;

import java.io.File;
import java.util.List;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/12/16 12:28
 * 更新时间: 2021/12/16 12:28
 * 描述:
 */
public interface OnCompressGlideImageCallbacks extends OnBaseCompressGlideCallbacks {
    void onResults(List<File> files);
}
