package com.angcyo.recyclerviewpager;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewPager extends RecyclerView {

    private static final String TAG = "RecyclerViewPager";
    /**
     * 一屏中, item的总数,
     * 如果是GridLayoutManager, 那么就是 行 * 列的总数量
     * 用来计算每个Item的宽度, 或者高度, 使其占满整个屏幕. 达到ViewPager的效果
     */
    int mItemCount = 8;
    List<OnViewPagerListener> mViewPagerListeners;
    private int mVerticalScrollOffsetStart;
    private int mHorizontalScrollOffsetStart;
    /**
     * 当前的页面索引
     */
    private int mCurrentPager = 0;
    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (newState == SCROLL_STATE_DRAGGING) {
                //开始滚动
                mVerticalScrollOffsetStart = recyclerView.computeVerticalScrollOffset();
                mHorizontalScrollOffsetStart = recyclerView.computeHorizontalScrollOffset();
            } else if (newState == SCROLL_STATE_IDLE) {
                //滚动结束之后
                final int verticalScrollOffset = recyclerView.computeVerticalScrollOffset();
                final int horizontalScrollOffset = recyclerView.computeHorizontalScrollOffset();
                final int rawWidth = getRawWidth();
                final int rawHeight = getRawHeight();
                int pagerIndex = mCurrentPager;

                int dx = 0, dy = 0;
                if (verticalScrollOffset == 0 && horizontalScrollOffset != 0) {
                    //横向滚动
                    final float page = horizontalScrollOffset * 1.f / rawWidth;//当前滚动到了第几页
                    final double floor = Math.floor(page);//前一页
                    final double ceil = Math.ceil(page);//后一页
                    final int offset;
                    final int offsetWidth;//滑动之后,  剩余屏幕的宽度

                    if (horizontalScrollOffset > mHorizontalScrollOffsetStart) {
                        pagerIndex = (int) floor;

                        //左滑动
                        offset = (int) (horizontalScrollOffset - floor * rawWidth);
                        offsetWidth = rawWidth - offset;
                        if (offset >= rawWidth / 3) {
                            dx = offsetWidth;
                        } else {
                            dx = -offset;
                        }

                    } else if (mHorizontalScrollOffsetStart > horizontalScrollOffset) {
                        pagerIndex = (int) ceil;

                        //右滑动
                        offset = (int) (ceil * rawWidth - horizontalScrollOffset);//横向滚动了多少距离
                        offsetWidth = rawWidth - offset;
                        if (offset >= rawWidth / 3) {
                            dx = -offsetWidth;
                        } else {
                            dx = offset;
                        }
                    }

                } else if (horizontalScrollOffset == 0 && verticalScrollOffset != 0) {
                    //竖向滚动
                    final float page = verticalScrollOffset * 1.f / rawHeight;//当前滚动到了第几页
                    final double floor = Math.floor(page);//前一页
                    final double ceil = Math.ceil(page);//后一页
                    final int offset;
                    final int offsetHeight;//滑动之后,  剩余屏幕的高度

                    if (verticalScrollOffset > mVerticalScrollOffsetStart) {
                        pagerIndex = (int) floor;

                        //上滑动
                        offset = (int) (verticalScrollOffset - floor * rawHeight);
                        offsetHeight = rawHeight - offset;
                        if (offset >= rawHeight / 3) {
                            dy = offsetHeight;
                        } else {
                            dy = -offset;
                        }

                    } else if (mVerticalScrollOffsetStart > verticalScrollOffset) {
                        pagerIndex = (int) ceil;

                        //下滑动
                        offset = (int) (ceil * rawHeight - verticalScrollOffset);//横向滚动了多少距离
                        offsetHeight = rawHeight - offset;
                        if (offset >= rawHeight / 3) {
                            dy = -offsetHeight;
                        } else {
                            dy = offset;
                        }
                    }
                } else {
                    pagerIndex = 0;
                }

                to(dx, dy);

                onViewPagerSelect(pagerIndex);
            }
        }

        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
        }
    };

    public RecyclerViewPager(Context context) {
        this(context, null);
    }

    public RecyclerViewPager(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecyclerViewPager(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setHasFixedSize(true);
        addOnScrollListener(mOnScrollListener);
        mViewPagerListeners = new ArrayList<>();
    }

    public int getRawWidth() {
        return getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
    }

    public int getRawHeight() {
        return getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
    }

    @Override
    public boolean fling(int velocityX, int velocityY) {
        return super.fling((int) (velocityX * 0.3f), (int) (velocityY * 0.3f));
    }

    @Override
    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (layout instanceof StaggeredGridLayoutManager) {
            Log.e(TAG, "setLayoutManager: 暂不支持StaggeredGridLayoutManager.");
        }
    }

    /**
     * 计算每个Item的宽度
     */
    public int getItemWidth() {
        final LayoutManager layoutManager = getLayoutManager();
        int itemWidth = 0;
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final int spanCount = gridLayoutManager.getSpanCount();
            if (gridLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                itemWidth = getRawWidth() / (mItemCount / spanCount);
            } else {
                itemWidth = getRawWidth() / spanCount;
            }

        } else if (layoutManager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            if (linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                itemWidth = getRawWidth() / mItemCount;
            } else {
                itemWidth = getRawWidth();
            }
        }

        return itemWidth;
    }

    /**
     * 每一页需要显示Item的数量
     */
    public int getItemCount() {
        return mItemCount;
    }

    /**
     * 如果是GridLayoutManager, 请尽量设置成spanCount的整数倍
     */
    public void setItemCount(int itemCount) {
        mItemCount = Math.max(1, itemCount);
    }

    /**
     * 获取页面数量
     */
    public int getPagerCount() {
        if (getAdapter() == null) {
            return 0;
        }
        return (int) Math.ceil(getAdapter().getItemCount() * 1f / mItemCount);//当给定的item个数不足以填充一屏时, 使用占位item
    }

    public int getItemHeight() {
        final LayoutManager layoutManager = getLayoutManager();
        int itemHeight = 0;
        if (layoutManager instanceof GridLayoutManager) {
            final GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
            final int spanCount = gridLayoutManager.getSpanCount();
            if (gridLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                itemHeight = getRawHeight() / spanCount;
            } else {
                itemHeight = getRawHeight() / (mItemCount / spanCount);
            }
        } else if (layoutManager instanceof LinearLayoutManager) {
            final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
            if (linearLayoutManager.getOrientation() == LinearLayoutManager.HORIZONTAL) {
                itemHeight = getRawHeight();
            } else {
                itemHeight = getRawHeight() / mItemCount;
            }
        }

        return itemHeight;
    }

    private void e(String msg) {
        Log.e("angcyo", "-->" + msg);
    }

    private void onViewPagerSelect(int index) {
        e("index:" + index);
        if (mCurrentPager != index) {
            mCurrentPager = index;
            for (OnViewPagerListener listener : mViewPagerListeners) {
                listener.onViewPager(index);
            }
        }
    }

    /**
     * 滚动到指定页
     */
    public void setCurrentPager(int pager) {
        if (mCurrentPager != pager) {
            final LayoutManager layoutManager = getLayoutManager();
            if (layoutManager instanceof LinearLayoutManager) {
                final LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;
                final int orientation = linearLayoutManager.getOrientation();
                if (orientation == LinearLayoutManager.HORIZONTAL) {
                    to(pager * getRawWidth() - mCurrentPager * getRawWidth(), 0);
                } else {
                    to(0, pager * getRawWidth() - mCurrentPager * getRawWidth());
                }
            }
        }
    }

    private void to(int dx, int dy) {
        smoothScrollBy(dx, dy, new LinearInterpolator());
    }

    public void addOnViewPagerListener(OnViewPagerListener listener) {
        mViewPagerListeners.add(listener);
    }

    public void removeOnViewPagerListener(OnViewPagerListener listener) {
        mViewPagerListeners.remove(listener);
    }

    public interface OnViewPagerListener {
        void onViewPager(int index);
    }
}