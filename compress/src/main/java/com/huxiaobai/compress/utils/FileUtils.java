package com.huxiaobai.compress.utils;

import android.content.Context;
import android.media.MediaMetadataRetriever;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.File;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/11/26 11:24
 * 更新时间: 2021/11/26 11:24
 * 描述:
 */
public class FileUtils {
    public static final String FILE_HOST_NAME = "MediaX";

    public static boolean isStaticImage(String filePath) {
       /* MediaMetadataRetriever retriever = new MediaMetadataRetriever();
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
        return false;*/
        return true;
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

    public static File getPublicRootDirectory(@NonNull Context context) {
        File file;
        File[] files = ContextCompat.getExternalFilesDirs(context, FileUtils.FILE_HOST_NAME);
        if (files.length > 0) {
            file = files[0];
        } else {
            file = context.getExternalFilesDir(FileUtils.FILE_HOST_NAME);
        }
        return file;
    }

    public static String createImageName() {
        String name = System.currentTimeMillis() + Math.random() * 100 + "";
        if (name.length() > 8) {
            name = name.substring(name.length() - 8);
        }
        return name + (int) (Math.random() * 10000) + "" + (int) (Math.random() * 1000) + "" + (int) (Math.random() * 100) + ".jpeg";

    }
}
