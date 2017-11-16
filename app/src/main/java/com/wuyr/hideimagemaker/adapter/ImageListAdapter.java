package com.wuyr.hideimagemaker.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.wuyr.hideimagemaker.R;

import java.io.File;
import java.util.List;

/**
 * Created by wuyr on 17-11-15 下午2:34.
 */

public class ImageListAdapter extends BaseAdapter<File, ImageListAdapter.ViewHolder> {

    private float mItemWidth;
    private OnItemOnClickListener mOnItemOnClickListener;

    public ImageListAdapter(Context context, List<File> data, int layoutId) {
        super(context, data, layoutId);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (mItemWidth == 0)
            mItemWidth = getScreenWidth(mContext) / 3;

        holder.imageView.setController(Fresco.newDraweeControllerBuilder().setOldController(
                holder.imageView.getController()).setImageRequest(ImageRequestBuilder.newBuilderWithSource(Uri.fromFile(mData.get(position)))
                        .setResizeOptions(new ResizeOptions((int) mItemWidth / 2, (int) mItemWidth / 2)).build()).build());
        if (mOnItemOnClickListener != null)
            holder.imageView.setOnClickListener(v -> mOnItemOnClickListener.onClick(mData.get(holder.getAdapterPosition()).getPath()));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(mLayoutInflater.inflate(mLayoutId, parent, false));
    }

    private static int getScreenWidth(Context context) {
        return ((Activity) context).getWindow().getDecorView().getWidth();
    }

    public void setOnItemOnClickListener(OnItemOnClickListener listener) {
        mOnItemOnClickListener = listener;
    }

    public interface OnItemOnClickListener {
        void onClick(String path);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        DraweeView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            imageView = (DraweeView) itemView.findViewById(R.id.image_view);
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            int size = getScreenWidth(itemView.getContext()) / 3;
            layoutParams.width = size;
            layoutParams.height = size;
        }
    }
}
