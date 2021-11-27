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

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

import com.huxiaobai.compress.imp.OnCompressGlideBitmapCallback;
import com.huxiaobai.compress.imp.OnCompressGlideImageCallback;
import com.huxiaobai.compress.utils.DataUtils;
import com.huxiaobai.compress.utils.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    private Thread mImageCompressThread;
    private BaseCompressCreate mCreate;
    private Context mApplicationContext;

    private static final CompressGlide mInstance = new CompressGlide();
    private final List<Lifecycle> mLifecycles = new ArrayList<>();

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

    public static ImageCompressCreate fromImage(@NonNull String path) {
        return ImageCompressCreate.formPath(path);
    }

    public static ImageCompressCreate fromImage(@NonNull File file) {
        return ImageCompressCreate.fromFile(file);
    }


    private Bitmap structureBitmap(@NonNull String path, @NonNull BaseBuilder builder) {
        File file = new File(path);
        long size = (file.length() / 1024);
        Bitmap bitmap;
        if (size > builder.compressBitmapSize) {
            bitmap = BitmapFactory.decodeFile(path);
        } else {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            options.inSampleSize = (int) ((options.outWidth * 1.0f) / (builder.compressWith * 1.0f) + (options.outHeight * 1.0f) / (builder.compressHeight * 1.0f)) / 2;
            options.inJustDecodeBounds = false;
            options.inMutable = true;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                options.outConfig = builder.config;
            }
            options.inPreferredConfig = builder.config;
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

    public Bitmap compressBitmap() {
        Bitmap bitmap = null;
        try {
            int from = mCreate.from;
            BaseBuilder builder = mCreate.mBuilder;
            if (from == BaseCompressCreate.FROM_PATH) {
                String path = mCreate.mImagePath;
                if (FileUtils.exitFile(path) && FileUtils.isStaticImage(path)) {
                    bitmap = structureBitmap(path, builder);
                }
            } else if (from == BaseCompressCreate.FROM_FILE) {
                File file = mCreate.mImageFile;
                if (FileUtils.exitFile(file) && FileUtils.isStaticImage(file.getAbsolutePath())) {
                    bitmap = structureBitmap(file.getAbsolutePath(), builder);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }


    public void asyncCompressBitmap(OnCompressGlideBitmapCallback callback) {
        mBitmapThread = new Thread() {
            @Override
            public void run() {
                try {
                    if (!mBitmapThread.isInterrupted()) {
                        Bitmap bitmap = compressBitmap();
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
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        };
        if (callback != null) {
            callback.onStart();
        }
        mBitmapThread.start();
    }

    private File bitmapToFile(@NonNull Bitmap bitmap) {
        FileOutputStream fos = null;
        File imageFile = null;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int quality = 100;
        Builder builder = (Builder) mCreate.mBuilder;
        boolean result = bitmap.compress(builder.format, quality, bos);
        Log.w("bitmapToFile--",builder+"");
        if (result) {
           /* while (bos.toByteArray().length/ 1024 > builder.compressBitmapSize) {
                bos.reset();
                quality -= 5;
                bitmap.compress(builder.format, quality, bos);
            }*/
            String path = builder.mCompressPath;
            if (DataUtils.isEmpty(path)){
                imageFile = new File(FileUtils.getPublicRootDirectory(mApplicationContext),FileUtils.createImageName());
            }else {
                imageFile = new File(path);
            }

            try {
                fos = new FileOutputStream(imageFile);
                fos.write(bos.toByteArray(), 0, bos.toByteArray().length);
                fos.flush();
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

        }
        return imageFile;

    }

    public void asyncCompressImage(OnCompressGlideImageCallback callback) {
        mImageCompressThread = new Thread() {
            @Override
            public void run() {
                if (!mImageCompressThread.isInterrupted()) {
                    Bitmap bitmap = compressBitmap();
                    if (bitmap != null) {
                        File imageFile = bitmapToFile(bitmap);
                        Log.w("asyncCompressImage---", imageFile + "---");
                        if (callback != null) {
                            mMainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    if (imageFile == null) {
                                        callback.onError("compress filed");
                                    } else {
                                        callback.onResult(imageFile);
                                    }
                                }
                            });
                        }

                    } else {
                        if (callback != null) {
                            mMainHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    callback.onError("bitmap is null");
                                }
                            });
                        }
                    }
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
        mImageCompressThread.start();

    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            if (mBitmapThread != null) {
                mBitmapThread.interrupt();
            }
        }
    }

    public CompressGlide inject(@NonNull BaseCompressCreate create,@NonNull Context context) {
        this.mCreate = create;
        this.mApplicationContext = context;
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


    public static final class ImageCompressCreate extends BaseCompressCreate {
        public static ImageCompressCreate formPath(@NonNull String path) {
            return new ImageCompressCreate(path);
        }

        public static ImageCompressCreate fromFile(@NonNull File file) {
            return new ImageCompressCreate(file);
        }


        @Override
        protected Builder createBuilder() {
            return new Builder();
        }

        private ImageCompressCreate(String imagePath) {
            super(imagePath);
        }

        private ImageCompressCreate(File file) {
            super(file);
        }

        public ImageCompressCreate createCompressPath(@Nullable String path) {
            if (!DataUtils.isEmpty(path)) {
                ((Builder) mBuilder).mCompressPath = path;
            }
            return this;
        }
    }

    public static final class ImagesCompressCreate extends BaseCompressCreate {

        @Override
        protected Builders createBuilder() {
            return new Builders();
        }

        private ImagesCompressCreate(String imagePath) {
            super(imagePath);
        }

        private ImagesCompressCreate(File file) {
            super(file);
        }


        public ImagesCompressCreate createCompressPath(@Nullable List<String> paths) {
            if (!DataUtils.isEmptyList(paths)) {
                ((Builders) mBuilder).mCompressPaths.clear();
                ((Builders) mBuilder).mCompressPaths.addAll(paths);
            }
            return this;
        }

    }


    public static abstract class BaseCompressCreate {
        public String mImagePath;
        public File mImageFile;
        public static final int FROM_UNKNOWN = 0;
        public static final int FROM_PATH = 1;
        public static final int FROM_FILE = 2;

        protected abstract BaseBuilder createBuilder();

        protected final BaseBuilder mBuilder;


        @IntDef({BaseCompressCreate.FROM_UNKNOWN, BaseCompressCreate.FROM_PATH, BaseCompressCreate.FROM_FILE})
        @Retention(RetentionPolicy.SOURCE)
        public @interface Form {
        }

        public final @Form
        int from;


        public BaseCompressCreate(String imagePath) {
            this.mImagePath = imagePath;
            from = BaseCompressCreate.FROM_PATH;
            mBuilder = createBuilder();
        }

        public BaseCompressCreate(File file) {
            this.mImageFile = file;
            from = BaseCompressCreate.FROM_FILE;
            mBuilder = createBuilder();
        }

        public BaseCompressCreate compressWidth(int width) {
            if (mBuilder != null) {
                mBuilder.compressWith = (width);
            }

            return this;
        }

        public BaseCompressCreate compressHeight(int height) {
            if (mBuilder != null) {
                mBuilder.compressHeight = (height);
            }
            return this;
        }

        public BaseCompressCreate config(Bitmap.Config config) {
            if (mBuilder != null) {
                mBuilder.config = (config);
            }
            return this;
        }

        public BaseCompressCreate format(Bitmap.CompressFormat format) {
            if (mBuilder != null) {
                mBuilder.format = (format);
            }
            return this;
        }

        public BaseCompressCreate compressBitmapSize(int sizeKb) {
            if (mBuilder != null) {
                if (sizeKb > 0) {
                    mBuilder.compressBitmapSize = (sizeKb);
                }
            }
            return this;
        }

        public CompressGlide create(Context context) {

            return getInstance().inject(this,context);
        }


    }


    private static final class Builders extends BaseBuilder {
        public final List<String> mCompressPaths = new ArrayList<>();
    }

    private static class Builder extends BaseBuilder {
        private String mCompressPath ;
    }


    private static class BaseBuilder {
        public int compressWith = 720;
        public int compressHeight = 1280;
        public Bitmap.Config config = Bitmap.Config.RGB_565;
        public Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        //kb
        public int compressBitmapSize = 150;

        @Override
        public String toString() {
            return "BaseBuilder{" +
                    "compressWith=" + compressWith +
                    ", compressHeight=" + compressHeight +
                    ", config=" + config +
                    ", format=" + format +
                    ", compressBitmapSize=" + compressBitmapSize +
                    '}';
        }
        /*public void setCompressWith(int compressWith) {
            this.compressWith = compressWith;
        }

        public void setCompressHeight(int compressHeight) {
            this.compressHeight = compressHeight;
        }

        public void setConfig(Bitmap.Config config) {
            this.config = config;
        }

        public void setFormat(Bitmap.CompressFormat format) {
            this.format = format;
        }

        public void setCompressBitmapSize(int compressBitmapSize) {
            this.compressBitmapSize = compressBitmapSize;
        }*/
    }


}
