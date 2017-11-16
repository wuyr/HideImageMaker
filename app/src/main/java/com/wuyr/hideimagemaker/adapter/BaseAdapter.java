package com.wuyr.hideimagemaker.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuyr on 17-10-24 下午1:05.
 */

abstract class BaseAdapter<O,VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> {

    Context mContext;
    List<O> mData;
    int mLayoutId;
    LayoutInflater mLayoutInflater;

    BaseAdapter(Context context, List<O> data, int layoutId) {
        mContext = context;
        mData = data == null ? new ArrayList<>() : data;
        mLayoutId = layoutId;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void addData(O o) {
        if (o != null) {
            mData.add(o);
            notifyItemInserted(mData.size());
        }
    }

    public boolean removeData(O o) {
        if (o != null) {
            int pos = mData.indexOf(o);
            if (pos != -1) {
                mData.remove(o);
                notifyItemRemoved(pos);
                return true;
            }
        }
        return false;
    }

    public void setData(List<O> data) {
        if (data != null) {
            mData = data;
            notifyDataSetChanged();
        }
    }
}