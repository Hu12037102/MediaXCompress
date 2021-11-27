package com.huxiaobai.compress.utils;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import java.net.PortUnreachableException;
import java.util.List;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/11/26 11:28
 * 更新时间: 2021/11/26 11:28
 * 描述:
 */
public class DataUtils {
    public static CharSequence checkNull(@Nullable CharSequence text) {
        return TextUtils.isEmpty(text) ? "" : text;
    }

    public static boolean isEmpty(CharSequence text) {
        return TextUtils.isEmpty(text);
    }

    /*public static int getByteArrayLength(byte[] bytes) {
        return bytes == null ? 0 : bytes.length;
    }*/

    public static boolean isEmptyList(List<?> list) {
        return list == null || list.size() == 0;
    }
}
