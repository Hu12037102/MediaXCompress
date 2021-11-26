package com.huxiaobai.compress;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.huxiaobai.compress.imp.OnBaseCompressGlideCallback;
import com.huxiaobai.compress.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/11/25 17:49
 * 更新时间: 2021/11/25 17:49
 * 描述:
 */
public final class CompressGlide implements LifecycleEventObserver {

    private Thread mBitmapThread;
    private Handler mMainHandler;

    public static CompressGlide.CompressBitmapCreate from(@NonNull String path) {
        return CompressCreate.formPath(path);
    }

    public static CompressGlide.CompressBitmapCreate from(@NonNull File file) {
        return CompressCreate.fromFile(file);
    }

    static CompressGlide getInstance() {
        return mInstance;
    }

    private CompressGlide() {
        initMainHandler();
    }

    private void initMainHandler() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
            mMainHandler = Handler.createAsync(Looper.getMainLooper());
        } else {
            mMainHandler = new Handler(Looper.getMainLooper());
        }
    }

    private CompressBitmapCreate mCreate;

    private static final CompressGlide mInstance = new CompressGlide();
    private final List<Lifecycle> mLifecycles = new ArrayList<>();

    private Bitmap structureBitmap(@NonNull String path, @NonNull Builder builder) {
        File file = new File(path);
        long size = (file.length() / 1024);
        BitmapFactory.Options options = new BitmapFactory.Options();
        if (size > builder.compressBitmapSize) {
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = (int) ((options.outWidth * 1.0f) / (builder.mCompressWith * 1.0f) + (options.outHeight * 1.0f) / (builder.mCompressHeight * 1.0f)) / 2;
            options.inJustDecodeBounds = false;
        }
        options.inMutable = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            options.outConfig = builder.mConfig;
        }
        options.inPreferredConfig = builder.mConfig;
        options.inPreferQualityOverSpeed = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);
        bitmap = rectifyInterface(path, bitmap);
        return bitmap;
    }

    private Bitmap rectifyInterface(@NonNull String path, @NonNull Bitmap bitmap) {
        Bitmap result = null;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    break;
                default:
                    matrix.postRotate(0);
                    break;
            }
            result = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Bitmap compressBitmap() {
        int from = mCreate.from;
        String path = mCreate.mImagePath;
        Builder builder = mCreate.mBuilder;
        Bitmap bitmap = null;
        if (from == CompressBitmapCreate.FROM_PATH) {
            if (FileUtils.exitFile(path)) {
                bitmap = structureBitmap(path, builder);
            }
        } else if (from == CompressBitmapCreate.FROM_FILE) {
            File file = mCreate.mImageFile;
            if (FileUtils.exitFile(file)) {
                bitmap = structureBitmap(file.getAbsolutePath(), builder);
            }
        }
        return bitmap;
    }


    public void asyncCompressBitmap(OnBaseCompressGlideCallback.OnCompressGlideBitmapCallback callback) {
        mBitmapThread = new Thread() {
            @Override
            public void run() {
                Bitmap bitmap = compressBitmap();
                if (!mBitmapThread.isInterrupted()) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                if (bitmap != null) {
                                    callback.onResult(bitmap);
                                } else {
                                    callback.onError("bitmap is null");
                                }
                            }
                        }
                    });

                } else {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (callback != null) {
                                callback.onCancel();
                            }
                        }
                    });
                }
            }
        };
        if (callback != null) {
            callback.onStart();
        }
        mBitmapThread.start();
    }

    public void compressImage() {
        BitmapFactory.Options options = new BitmapFactory.Options();

    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            if (mBitmapThread != null) {
                mBitmapThread.interrupt();
            }
        }
    }

    public <T extends CompressBitmapCreate> CompressGlide inject(@NonNull T create) {
        this.mCreate = create;
        mLifecycles.clear();
        return this;
    }

    public CompressGlide addLifecycle(Lifecycle lifecycle) {
        if (lifecycle != null) {
            lifecycle.addObserver(this);
            mLifecycles.add(lifecycle);
        }
        return this;
    }

    public void removeLifecycle(Lifecycle lifecycle) {
        if (lifecycle != null) {
            lifecycle.removeObserver(this);
            mLifecycles.remove(lifecycle);
        }
    }

    public void clearLifecycles() {
        for (Lifecycle lifecycle : mLifecycles) {
            lifecycle.removeObserver(this);
        }
        mLifecycles.clear();
    }

    public void reset() {
        clearLifecycles();

    }


    public static class CompressCreate extends CompressBitmapCreate {


        private CompressCreate(String imagePath) {
            super(imagePath);
        }

        private CompressCreate(File file) {
            super(file);
        }

        public CompressCreate compressPath(String path) {
            mBuilder.mCompressPath = path;
            return this;
        }


    }

    public static class CompressBitmapCreate {

        public static final int FROM_UNKNOWN = 0;
        public static final int FROM_PATH = 1;
        public static final int FROM_FILE = 2;

        @IntDef({CompressBitmapCreate.FROM_UNKNOWN, CompressBitmapCreate.FROM_PATH, CompressBitmapCreate.FROM_FILE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Form {
        }

        private String mImagePath;
        private File mImageFile;
        private final @Form
        int from;

        protected final CompressGlide.Builder mBuilder = new CompressGlide.Builder();

        private CompressBitmapCreate(String imagePath) {
            this.mImagePath = imagePath;
            this.from = CompressBitmapCreate.FROM_PATH;
        }

        private CompressBitmapCreate(File file) {
            this.mImageFile = file;
            this.from = CompressBitmapCreate.FROM_FILE;
        }

        public static CompressBitmapCreate formPath(@NonNull String path) {
            return new CompressBitmapCreate(path);
        }

        public static CompressBitmapCreate fromFile(@NonNull File file) {
            return new CompressBitmapCreate(file);
        }


        public CompressBitmapCreate compressWidth(int width) {
            mBuilder.mCompressWith = width;
            return this;
        }

        public CompressBitmapCreate compressHeight(int height) {
            mBuilder.mCompressHeight = (height);
            return this;
        }

        public CompressBitmapCreate config(Bitmap.Config config) {
            mBuilder.mConfig = (config);
            return this;
        }

        public CompressBitmapCreate format(Bitmap.CompressFormat format) {
            mBuilder.mFormat = (format);
            return this;
        }

        public CompressBitmapCreate compressBitmapSize(int sizeKb) {
            if (sizeKb > 0) {
                mBuilder.compressBitmapSize = sizeKb;
            }
            return this;
        }

        public CompressGlide create() {
            return getInstance().inject(this);

        }
    }


    private static class Builder {
        private int mCompressWith = 1280;
        private int mCompressHeight = 720;
        private Bitmap.Config mConfig = Bitmap.Config.RGB_565;
        private Bitmap.CompressFormat mFormat = Bitmap.CompressFormat.JPEG;
        //kb
        private int compressBitmapSize = 150;
        private String mCompressPath;
    }


}
