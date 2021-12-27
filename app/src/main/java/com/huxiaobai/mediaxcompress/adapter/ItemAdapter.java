package com.huxiaobai.mediaxcompress.adapter;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.huxiaobai.mediaxcompress.R;
import com.huxiaobai.mediaxcompress.entity.Item;

import java.util.List;

/**
 * 作者: 胡庆岭
 * 创建时间: 2021/12/27 15:04
 * 更新时间: 2021/12/27 15:04
 * 描述:
 */
public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {
    private final Context mContext;
    private final List<Item> mData;

    public void setOnClickItemMediaListener(OnClickItemMediaListener onClickItemMediaListener) {
        this.onClickItemMediaListener = onClickItemMediaListener;
    }

    private OnClickItemMediaListener onClickItemMediaListener;

    public ItemAdapter(Context context, List<Item> data) {
        this.mContext = context;
        this.mData = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_add_media_view, parent, false));

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = mData.get(position);
        holder.mAivDelete.setVisibility(View.GONE);
        if (item.isAdd) {
            holder.mAivMedia.setImageResource(R.mipmap.icon_add);
        } else {
           // holder.mAivMedia.setImageURI(item.uri);
            Glide.with(mContext).load(item.uri).into(holder.mAivMedia);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            holder.mAivMedia.setForeground(ContextCompat.getDrawable(mContext,R.color.color_10000000));
        }
        initHolderEvent(holder, position);
    }

    private void initHolderEvent(ViewHolder holder, int position) {
        holder.mAivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mData.remove(position);
                notifyDataSetChanged();
                if (onClickItemMediaListener != null) {
                    onClickItemMediaListener.onClickDelete(v, position);
                }
            }
        });
        holder.mAivMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Item item = mData.get(position);
                if (item.isAdd && onClickItemMediaListener != null) {
                    onClickItemMediaListener.onClickAdd(v, position);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private final AppCompatImageView mAivMedia;
        private final AppCompatImageView mAivDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mAivMedia = itemView.findViewById(R.id.aiv_media);
            mAivDelete = itemView.findViewById(R.id.aiv_delete);
        }
    }

    public interface OnClickItemMediaListener {
        void onClickDelete(View view, int position);

        void onClickAdd(View view, int position);
    }
}
