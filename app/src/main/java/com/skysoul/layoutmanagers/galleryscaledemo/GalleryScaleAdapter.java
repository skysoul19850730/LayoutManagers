package com.skysoul.layoutmanagers.galleryscaledemo;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.skysoul.layoutmanagersdemos.BuildConfig;
import com.skysoul.layoutmanagersdemos.R;

import java.util.List;
import java.util.Random;


/**
 * Created by chensuilun on 2016/11/15.
 */
public class GalleryScaleAdapter extends RecyclerView.Adapter<GalleryScaleAdapter.ViewHolder> implements View.OnClickListener {
    public static final int VIEW_TYPE_IMAGE = 0;
    public static final int VIEW_TYPE_TEXT = 1;
    private static final String TAG = "DemoAdapter";
    private List<String> mItems;
    private OnItemClickListener mOnItemClickListener;


    public GalleryScaleAdapter(List<String> items) {
        this(items, VIEW_TYPE_IMAGE);
    }

    public GalleryScaleAdapter(List<String> items, int type) {
        this.mItems = items;
    }

    public void addData() {
        if (mItems != null) {
            for (int i = 0; i < 10; i++) {
                mItems.add("Extra:" + i);
            }
            notifyDataSetChanged();
        }
    }

    private static final Random RANDOM = new Random();

    public int dataChange() {
        int result = 0;
        if (mItems != null) {
            if (RANDOM.nextBoolean()) {
                for (int i = 0; i < 10; i++) {
                    mItems.add("Extra:" + i);
                }
                result = 1;
            } else {
                int size = mItems.size();
                int cut = size / 2;
                for (int i = size - 1; i > cut; i--) {
                    mItems.remove(i);
                }
                result = -1;
            }
            notifyDataSetChanged();
        }
        return result;
    }

    public GalleryScaleAdapter setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.mOnItemClickListener = onItemClickListener;
        return this;
    }

    @Override
    public int getItemViewType(int position)

    {
        if(position>12){
            return 0;
        }
        return 1;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (BuildConfig.DEBUG) {
            Log.e(TAG, "onCreateViewHolder: type:" + viewType);
        }
        View v;
        v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scale_item, parent, false);
        if(viewType==0){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.scale_item2, parent, false);
        }
        v.setOnClickListener(this);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "onBindViewHolder: position:" + position);
        }
        String item = mItems.get(position);
        if (getItemViewType(position) == VIEW_TYPE_IMAGE) {
            holder.text.setText(item);
        } else {
            holder.text.setText("HelloWorld：" + item);
        }
        holder.itemView.setTag(position);
        if ((position & 1) == 0) {
            holder.itemView.findViewById(R.id.item_tv_title).setBackgroundColor(Color.BLUE);
        } else {
            holder.itemView.findViewById(R.id.item_tv_title).setBackgroundColor(Color.RED);

        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }



    @Override
    public void onClick(final View v) {
        if (mOnItemClickListener != null) {
            mOnItemClickListener.onItemClick(v, (int) v.getTag());
        }
    }

    /**
     * @author chensuilun
     */
    protected static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView text;

        public ViewHolder(View itemView) {
            super(itemView);
            text = (TextView) itemView.findViewById(R.id.item_tv_title);
        }
    }

    /**
     * @author chensuilun
     */
    public interface OnItemClickListener {

        void onItemClick(View view, int position);

    }
}
