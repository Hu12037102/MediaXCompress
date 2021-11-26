package com.huxiaobai.compress.utils;

import android.media.MediaMetadataRetriever;

import java.io.File;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/11/26 11:24
 * 更新时间: 2021/11/26 11:24
 * 描述:
 */
public class FileUtils {
    public static boolean isStaticImage(String filePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            String mimeTye = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            mimeTye = DataUtils.checkNull(mimeTye).toString();
            return mimeTye.startsWith("image/") && !mimeTye.endsWith("/gif");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        return false;
    }

    public static boolean exitFile(String path) {
        if (DataUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return exitFile(file);
    }

    public static boolean exitFile(File file) {
        return file != null && file.exists() && file.isFile();
    }
}
