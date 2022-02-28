package com.huxiaobai.mediaxcompress;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.baixiaohu.permission.PermissionActivity;
import com.baixiaohu.permission.imp.OnPermissionsResult;
import com.huxiaobai.compress.CompressGlide;
import com.huxiaobai.compress.imp.OnCompressGlideImageCallback;
import com.huxiaobai.compress.imp.OnCompressGlideImageCallbacks;
import com.huxiaobai.compress.utils.FileUtils;
import com.huxiaobai.mediaxcompress.adapter.ItemAdapter;
import com.huxiaobai.mediaxcompress.entity.Item;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends PermissionActivity {


    private List<Item> mOriginalItems;
    private ItemAdapter mOriginalAdapter;
    private List<Item> mCompressItems;
    private ItemAdapter mCompressAdapter;
    private ProgressDialog mLoadingDialog;

    @Override
    @SuppressLint("IntentReset")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RecyclerView mRvOriginal = findViewById(R.id.rv_original);
        mRvOriginal.setLayoutManager(new GridLayoutManager(this, 4));
        RecyclerView mRvCompress = findViewById(R.id.rv_compress);
        mRvCompress.setLayoutManager(new GridLayoutManager(this, 4));


        mOriginalItems = new ArrayList<>();
        Item addItem = new Item();
        addItem.isAdd = true;
        mOriginalItems.add(addItem);
        mOriginalAdapter = new ItemAdapter(this, mOriginalItems);
        mRvOriginal.setAdapter(mOriginalAdapter);


        mCompressItems = new ArrayList<>();
        mCompressAdapter = new ItemAdapter(this, mCompressItems);
        mRvCompress.setAdapter(mCompressAdapter);

        findViewById(R.id.atv_compress).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCompressItems.clear();
                mCompressAdapter.notifyDataSetChanged();
                requestPermission();
            }
        });
        mOriginalAdapter.setOnClickItemMediaListener(new ItemAdapter.OnClickItemMediaListener() {
            @Override
            public void onClickDelete(View view, int position) {

            }

            @Override
            public void onClickAdd(View view, int position) {
                try {
                    Iterator<Item> iterator = mOriginalItems.iterator();
                    while (iterator.hasNext()) {
                        Item item = iterator.next();
                        if (!item.isAdd) {
                            iterator.remove();
                        }
                    }
                    mOriginalAdapter.notifyDataSetChanged();

                    mCompressItems.clear();
                    mCompressAdapter.notifyDataSetChanged();

                    Intent intent = new Intent();
                   // intent.setType("image/*");
                    intent.setType("video/*");
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.putExtra(Intent.EXTRA_ALARM_COUNT, "9");
                    intent.setAction(Intent.ACTION_PICK);
                    intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), 100);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            //  ClipData clipData = data.getClipData();
            //  Log.w("onActivityResult--", clipData.getItemCount() + "--");
         /*   Uri uri = data.getData();
            mAivContentOriginal.setImageURI(uri);
            mImagePath = FileUtils.getMediaPath(this, uri);
            Log.w("onActivityResult", uri + "--" + mImagePath);*/
            try {
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        Uri itemUri = item.getUri();
                        Item mediaItem = new Item();
                        mediaItem.uri = itemUri;
                        mOriginalItems.add(0, mediaItem);

                        Log.w("onActivityResult--", itemUri + "---");
                    }
                    mOriginalAdapter.notifyDataSetChanged();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    private void requestPermission() {

        requestPermission(new OnPermissionsResult() {
            @Override
            public void onAllow(List<String> allowPermissions) {
                List<String> data = new ArrayList<>();
                for (int i = 0; i < mOriginalItems.size(); i++) {
                    Item item = mOriginalItems.get(i);
                    if (!item.isAdd) {
                        String path = FileUtils.getMediaPath(MainActivity.this, item.uri);
                        data.add(path);
                    }
                }
                if (data.size() == 0) {
                    Toast.makeText(getApplicationContext(), R.string.please_selector_image, Toast.LENGTH_SHORT).show();
                    return;
                }
                CompressGlide.fromImage()
                        .create(MainActivity.this)
                        .compressImages(data, new OnCompressGlideImageCallbacks() {
                    @Override
                    public void onResults(List<File> files) {
                        for (File file : files) {
                            Uri uri = Uri.fromFile(file);
                            Item item = new Item();
                            item.uri = uri;
                            mCompressItems.add(item);
                        }
                        mCompressAdapter.notifyDataSetChanged();
                        dismissLoadingDialog();
                        Log.w("asyncCompressImage", "我成功了" + files.size() + "--");
                    }

                    @Override
                    public void onStart() {
                        Log.w("asyncCompressImage", "我开始了");
                        showLoadingDialog();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Log.w("asyncCompressImage", "我失败了" + errorMessage);
                        dismissLoadingDialog();
                    }


                });
              /*  CompressGlide.fromImage()
                        .compressHeight(1280)
                        .compressWith(720)
                        .config(Bitmap.Config.RGB_565)
                        .compressBitmapSize(150)
                        .create(MainActivity.this)
                        .addLifecycle(getLifecycle())
                        .compressImages(data, new OnCompressGlideImageCallbacks() {
                            @Override
                            public void onResults(List<File> files) {
                                for (File file : files) {
                                    Uri uri = Uri.fromFile(file);
                                    Item item = new Item();
                                    item.uri = uri;
                                    mCompressItems.add(item);
                                }
                                mCompressAdapter.notifyDataSetChanged();
                                dismissLoadingDialog();
                                Log.w("asyncCompressImage", "我成功了" + files.size() + "--");
                            }

                            @Override
                            public void onStart() {
                                Log.w("asyncCompressImage", "我开始了");
                                showLoadingDialog();
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.w("asyncCompressImage", "我失败了" + errorMessage);
                                dismissLoadingDialog();
                            }

                            @Override
                            public void onCancel() {
                                Log.w("asyncCompressImage", "我取消了");
                                dismissLoadingDialog();
                            }
                        });*/

                /*CompressGlide.fromImage()
                        .compressHeight(1280)
                        .compressWith(720)
                        .config(Bitmap.Config.RGB_565)
                        .compressBitmapSize(120)
                        .create(MainActivity.this)
                        .addLifecycle(getLifecycle())
                        .compressImage(mImagePath, new OnCompressGlideImageCallback() {
                            @Override
                            public void onResult(@NonNull File file) {
                                mAivContentCompress.setImageBitmap(BitmapFactory.decodeFile(file.getAbsolutePath()));
                                Log.w("asyncCompressImage", "我成功了" + file.getAbsolutePath() + "--");
                            }

                            @Override
                            public void onStart() {
                                Log.w("asyncCompressImage", "我开始了");
                            }

                            @Override
                            public void onError(String errorMessage) {
                                Log.w("asyncCompressImage", "我失败了" + errorMessage);

                            }

                            @Override
                            public void onCancel() {
                                Log.w("asyncCompressImage", "我取消了");
                            }
                        });*/


            }

            @Override
            public void onNoAllow(List<String> noAllowPermissions) {

            }

            @Override
            public void onForbid(List<String> noForbidPermissions) {

            }
        }, Manifest.permission.READ_EXTERNAL_STORAGE);
    }

    private void showLoadingDialog() {
        if (mLoadingDialog == null) {
            mLoadingDialog = new ProgressDialog(this);
            mLoadingDialog.setMessage("正在加载...");
        }
        if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.show();
        }
    }
    private void dismissLoadingDialog(){
        if (mLoadingDialog!=null && mLoadingDialog.isShowing()){
            mLoadingDialog.dismiss();
        }
    }
}