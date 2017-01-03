package com.angcyo.recyclerviewpager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

public abstract class RecyclerViewPagerAdapter<T> extends RBaseAdapter<T> {

    private RecyclerViewPager mRecyclerViewPager;
    private int rawSize;//真实的item个数

    public RecyclerViewPagerAdapter(RecyclerViewPager recyclerViewPager, List<T> datas) {
        super(recyclerViewPager.getContext(), datas);
        mRecyclerViewPager = recyclerViewPager;
    }

    @Override
    public int getItemCount() {
        rawSize = mAllDatas == null ? 0 : mAllDatas.size();
        final int itemCount = mRecyclerViewPager.getItemCount();
        final double ceil = Math.ceil(rawSize * 1f / itemCount);//当给定的item个数不足以填充一屏时, 使用占位item
        return (int) (ceil * itemCount);
    }

    @Override
    public int getItemViewType(int position) {
        if (position < rawSize) {
            return 200;
        }
        return -200;// placeholder 占位item类型
    }

    @Override
    public RBaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View item;
        if (viewType == 200) {
            int itemLayoutId = getItemLayoutId(viewType);
            if (itemLayoutId == 0) {
                item = createContentView(parent, viewType);
            } else {
                item = LayoutInflater.from(mContext).inflate(itemLayoutId, parent, false);
            }
        } else {
            item = new View(mContext);
        }
        return new RBaseViewHolder(item, viewType);
    }

    @Override
    protected void onBindView(RBaseViewHolder holder, int position, T bean) {
        holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(mRecyclerViewPager.getItemWidth(),
                mRecyclerViewPager.getItemHeight()));
        if (holder.getItemViewType() == 200) {
            onBindRawView(holder, position, bean);
        }
    }

    protected abstract void onBindRawView(RBaseViewHolder holder, int position, T bean);
}