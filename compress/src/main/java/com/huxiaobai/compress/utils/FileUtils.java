package com.huxiaobai.compress.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.util.UUID;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/11/26 11:24
 * 更新时间: 2021/11/26 11:24
 * 描述:
 */
public class FileUtils {
    public static final String FILE_HOST_NAME = "MediaX";

    public static boolean isStaticImage(String filePath) {
       /* boolean isStaticImage = false;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            String mimeTye = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            mimeTye = DataUtils.checkNull(mimeTye).toString();
            isStaticImage = mimeTye.startsWith("image/") && !mimeTye.endsWith("/gif");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            retriever.release();
        }
        Log.w("isStaticImage----", isStaticImage + "---");
        return isStaticImage;*/
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
        int random = (int) (Math.random() * 1000);
        String name = System.currentTimeMillis() + random + "";
        if (name.length() > 8) {
            name = name.substring(name.length() - 8);
        }
        String uuid = UUID.randomUUID().toString().trim().replaceAll("-", "");
        if (uuid.length() > 6) {
            uuid = uuid.substring(uuid.length() - 6);
        }
        return "img_" + uuid + name + ".jpeg";

    }

    public static String getMediaPath(@NonNull Context context, @NonNull Uri uri) {
        String uriPath = "";
        try {
            ContentResolver resolver = context.getApplicationContext().getContentResolver();
            String[] filePathColumn = {MediaStore.MediaColumns.DATA};
            Cursor cursor = resolver.query(uri, filePathColumn, null, null, null);
            if (cursor != null) {
                cursor.moveToFirst();
                uriPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA));
                cursor.close();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return uriPath;
    }
}
