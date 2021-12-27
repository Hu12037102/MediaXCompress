package com.huxiaobai.compress;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.huxiaobai.compress.imp.OnCompressGlideBitmapCallback;
import com.huxiaobai.compress.imp.OnCompressGlideImageCallback;
import com.huxiaobai.compress.imp.OnCompressGlideImageCallbacks;
import com.huxiaobai.compress.utils.DataUtils;
import com.huxiaobai.compress.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/12/15 10:04
 * 更新时间: 2021/12/15 10:04
 * 描述:
 */
public final class CompressGlide implements LifecycleEventObserver {
    private static final int COMPRESS_MAX_COUNT = 9;
    private Context mApplicationContext;
    private BaseCreate mCreate;
    private final List<Lifecycle> mLifecycles = new ArrayList<>();
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    private ExecutorService mThreadService;

    private CompressGlide() {

    }

    private static final CompressGlide mInstance = new CompressGlide();

    static CompressGlide getInstance() {
        return mInstance;
    }

    public static ImageCreate fromImage() {
        return new ImageCreate();
    }

    private CompressGlide inject(@NonNull Context context, BaseCreate create) {
        this.mApplicationContext = context;
        this.mCreate = create;
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

    private void initThreadService() {
        if (mThreadService == null || mThreadService.isShutdown()) {
            mThreadService = Executors.newFixedThreadPool(9);
        }
    }

    private void startThreadService(@NonNull Runnable runnable) {
        initThreadService();
        mThreadService.execute(runnable);
    }

    public void stopThreadService() {
        if (mThreadService != null && !mThreadService.isShutdown()) {
            mThreadService.shutdown();
        }
    }


    private Bitmap structureBitmap(@NonNull String path, @NonNull CompressGlide.BaseCreate create) {
        if (!FileUtils.exitFile(path)) {
            return null;
        }
        File file = new File(path);
        long size = (file.length() / 1024);
        Bitmap bitmap;
        if (size < create.compressBitmapSize) {
            bitmap = BitmapFactory.decodeFile(path);
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = (int) ((options.outWidth * 1.0f) / (create.compressWith * 1.0f) + (options.outHeight * 1.0f) / (create.compressHeight * 1.0f)) / 2;
            options.inJustDecodeBounds = false;
            options.inMutable = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                options.outConfig = create.config;
            }
            options.inPreferredConfig = create.config;
            options.inPreferQualityOverSpeed = false;
            bitmap = BitmapFactory.decodeFile(path, options);
            bitmap = rectifyInterface(path, bitmap);
        }
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

    public Bitmap compressBitmap(@NonNull String formImagePath) {
        Bitmap bitmap = null;
        try {
            bitmap = structureBitmap(formImagePath, mCreate);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    public synchronized void asyncCompressBitmap(@NonNull String fromImagePath, OnCompressGlideBitmapCallback callback) {
        if (callback != null) {
            callback.onStart();
        }
        startThreadService(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = compressBitmap(fromImagePath);
                mMainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (callback != null) {
                            if (bitmap != null) {
                                callback.onResult(bitmap);
                            } else {
                                callback.onError("compress fail,bitmap is null");
                            }
                        }
                    }
                });
            }
        });
    }

    private File bitmapToFile(Bitmap bitmap, BaseCreate create, String compressImagePath) {
        if (bitmap == null) {
            return null;
        }
        FileOutputStream fos = null;
        File imageFile;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int quality = 100;
        boolean result = bitmap.compress(mCreate.format, quality, bos);
        if (!result) {
            bitmap = bitmap.copy(mCreate.config, true);
            bitmap.compress(mCreate.format, quality, bos);
        }
        int firstLength = bos.toByteArray().length;
        while (bos.toByteArray().length / 1024 > create.compressBitmapSize) {
            Log.w("bitmapToFile--", quality + "--" + bos.toByteArray().length / 1024 + "--" + bitmap.getWidth() + "--" + bitmap.getHeight());
            bos.reset();
            quality -= 5;
            bitmap.compress(mCreate.format, quality, bos);
            if (firstLength == bos.toByteArray().length) {
                break;
            }
        }

        if (DataUtils.isEmpty(compressImagePath)) {
            imageFile = new File(FileUtils.getPublicRootDirectory(mApplicationContext), FileUtils.createImageName());
        } else {
            imageFile = new File(compressImagePath);
        }
        try {
            fos = new FileOutputStream(imageFile);
            fos.write(bos.toByteArray(), 0, bos.toByteArray().length);
            fos.flush();
            bitmap.recycle();
            return imageFile;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                bos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return imageFile;
    }

    public synchronized void compressImage(@NonNull String fromImagePath, OnCompressGlideImageCallback callback) {
        if (mCreate instanceof ImageCreate) {
            ImageCreate imageCreate = (ImageCreate) mCreate;
            startThreadService(new Runnable() {
                @Override
                public void run() {
                    Log.w("mThreadService--", Thread.currentThread() + "--");
                    try {
                        Bitmap bitmap = compressBitmap(fromImagePath);
                        File file = bitmapToFile(bitmap, imageCreate, imageCreate.compressImagePath);
                        mMainHandler.post(() -> {
                            if (callback != null) {
                                if (file != null) {
                                    callback.onResult(file);
                                } else {
                                    callback.onError("compress fail,file is null");
                                }

                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        } else {
            if (callback != null) {
                callback.onError("compress fail,BaseCreate instanceof ImageCreate");
            }
        }
    }

    public synchronized void compressImages(List<String> formPaths, OnCompressGlideImageCallbacks callbacks) {
        if (DataUtils.isEmptyList(formPaths)) {
            if (callbacks != null) {
                callbacks.onError("image path must exist");
            }
            return;
        }
        if (callbacks != null) {
            callbacks.onStart();
        }
        List<File> results = new ArrayList<>();
        startThreadService(new Runnable() {
            @Override
            public void run() {
                for (String path : formPaths) {
                    Bitmap bitmap = structureBitmap(path, mCreate);
                    File imageFile = new File(FileUtils.getPublicRootDirectory(mApplicationContext), FileUtils.createImageName());
                    imageFile = bitmapToFile(bitmap, mCreate, imageFile.getAbsolutePath());
                    if (FileUtils.exitFile(imageFile)) {
                        results.add(imageFile);
                    } else {
                        mMainHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                if (callbacks != null) {
                                    callbacks.onError("compress fail");
                                }
                            }
                        });
                        stopThreadService();
                        return;
                    }
                }
                if (callbacks != null) {
                    mMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            callbacks.onResults(results);
                        }
                    });

                }


            }
        });
    }


    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            try {
                stopThreadService();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    public static final class ImageCreate extends CompressGlide.BaseCreate {
        private String compressImagePath;

        private ImageCreate() {
        }

        public ImageCreate compressWith(int compressWith) {
            if (compressWith > 0) {
                this.compressWith = compressWith;
            }
            return this;
        }

        public ImageCreate compressHeight(int compressHeight) {
            if (compressHeight > 0) {
                this.compressHeight = compressHeight;
            }
            return this;
        }

        public ImageCreate config(Bitmap.Config config) {
            if (config != null) {
                this.config = config;
            }
            return this;
        }


        public ImageCreate compressImagePath(String path) {
            if (path != null) {
                this.compressImagePath = path;
            }
            return this;
        }

        public ImageCreate compressBitmapSize(int size) {
            if (size > 0) {
                this.compressBitmapSize = size;
            }
            return this;
        }

        @Override
        public CompressGlide create(Context context) {
            return CompressGlide.getInstance().inject(context, this);
        }
    }


    public static abstract class BaseCreate {
        protected BaseCreate() {

        }

        protected int compressWith = 720;
        protected int compressHeight = 1280;
        protected Bitmap.Config config = Bitmap.Config.RGB_565;
        private final Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        //kb
        protected int compressBitmapSize = 150;

        public abstract CompressGlide create(Context context);
    }


}
