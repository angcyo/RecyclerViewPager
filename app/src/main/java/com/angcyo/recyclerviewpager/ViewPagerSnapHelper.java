package com.angcyo.recyclerviewpager;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.view.View;

/**
 * Copyright (C) 2016,深圳市红鸟网络科技股份有限公司 All rights reserved.
 * 项目名称：
 * 类的描述：配合 {@link RecyclerViewPager} 使用
 * 创建人员：Robi
 * 创建时间：2017/02/16 09:52
 * 修改人员：Robi
 * 修改时间：2017/02/16 09:52
 * 修改备注：
 * Version: 1.0.0
 */
public class ViewPagerSnapHelper extends SnapHelper {

    /**
     * 每一页中, 含有多少个item
     */
    int mPageItemCount = 1;
    /**
     * 当前页面索引
     */
    int mCurrentPageIndex = 0;
    RecyclerView mRecyclerView;
    PageListener mPageListener;

    /**
     * 需要滚动到目标的页面索引
     */
    int mTargetIndex = RecyclerView.NO_POSITION;

    /**
     * fling操作时,需要锁住目标索引位置
     */
    boolean isFling = false;

    int scrollState;

    private RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            super.onScrollStateChanged(recyclerView, newState);
            scrollState = newState;

            if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                onScrollEnd();
            } else if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                isFling = false;
            } else if (newState == RecyclerView.SCROLL_STATE_SETTLING) {

            }
        }
    };

    public ViewPagerSnapHelper(int pageItemCount) {
        if (pageItemCount < 1) {
            throw new IllegalStateException("page item count need greater than 1");
        }
        this.mPageItemCount = pageItemCount;
    }

    protected void onScrollEnd() {
        int old = mCurrentPageIndex;
        int index = getPagerIndex(0, 0);
        //L.i("current->" + mCurrentPageIndex + " index->" + index + " target->" + mTargetIndex);

        if (index == mTargetIndex) {
            mCurrentPageIndex = mTargetIndex;
            //滚动结束后, 目标的索引位置和当前的索引位置相同, 表示已经完成了页面切换
            if (old != mCurrentPageIndex) {
                //L.e("page from->" + old + " to->" + mCurrentPageIndex);
            }
            if (mPageListener != null) {
                mPageListener.onPageSelector(mCurrentPageIndex);
            }
        }
    }

    @Override
    public void attachToRecyclerView(@Nullable RecyclerView recyclerView) throws IllegalStateException {
        if (recyclerView == null) {
            throw new NullPointerException("RecyclerView not be null");
        }
        mRecyclerView = recyclerView;
        super.attachToRecyclerView(recyclerView);
        mRecyclerView.addOnScrollListener(mScrollListener);
    }

    @Nullable
    @Override
    public int[] calculateDistanceToFinalSnap(@NonNull RecyclerView.LayoutManager layoutManager,
                                              @NonNull View targetView) {

        int[] out = new int[2];
        if (layoutManager.canScrollHorizontally()) {
            out[0] = mTargetIndex * mRecyclerView.getMeasuredWidth() - mRecyclerView.computeHorizontalScrollOffset();
        } else {
            out[0] = 0;
        }

        if (layoutManager.canScrollVertically()) {
            out[1] = mTargetIndex * mRecyclerView.getMeasuredHeight() - mRecyclerView.computeVerticalScrollOffset();
        } else {
            out[1] = 0;
        }
        return out;
    }

    @Nullable
    @Override
    public View findSnapView(RecyclerView.LayoutManager layoutManager) {
        int childCount = mRecyclerView.getLayoutManager().getChildCount();
        final int pagerIndex = getPagerIndex(0, 0);
        if (childCount == 0 || isFling) {
            return null;
        }
        mTargetIndex = pagerIndex;
        //随便返回一个补位空的view,就行.不需要通过这个View计算位置.
        return mRecyclerView.getLayoutManager().getChildAt(0);
    }

    @Override
    public boolean onFling(int velocityX, int velocityY) {

        RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (layoutManager == null) {
            return false;
        }
        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter == null) {
            return false;
        }
        int minFlingVelocity = mRecyclerView.getMinFlingVelocity();

        boolean handle = Math.abs(velocityY) > minFlingVelocity || Math.abs(velocityX) > minFlingVelocity;
        //L.w("onFling " + handle + " " + isFling);
        if (isFling) {
            return false;
        }

        if (handle) {
            if (mTargetIndex != RecyclerView.NO_POSITION) {
                mCurrentPageIndex = mTargetIndex;
            }

            if (velocityX > 0 || velocityY > 0) {
                mTargetIndex = fixPagerIndex(mCurrentPageIndex + 1);
            } else if (velocityX < 0 || velocityY < 0) {
                mTargetIndex = fixPagerIndex(mCurrentPageIndex - 1);
            } else {
                mTargetIndex = fixPagerIndex(mCurrentPageIndex);
            }

            int[] snapDistance = calculateDistanceToFinalSnap(layoutManager, null);
            if (snapDistance[0] != 0 || snapDistance[1] != 0) {
                isFling = true;
                mRecyclerView.smoothScrollBy(snapDistance[0], snapDistance[1]);
            } else {
                onScrollEnd();
            }
        }
        return handle;
    }

    /**
     * 只会在onFling的时候调用
     */
    @Override
    public int findTargetSnapPosition(RecyclerView.LayoutManager layoutManager, int velocityX,
                                      int velocityY) {
        final int itemCount = layoutManager.getItemCount();
        if (itemCount == 0) {
            return RecyclerView.NO_POSITION;
        }

        mTargetIndex = fixPagerIndex(getPagerIndex(velocityX, velocityY));
        return mTargetIndex * mPageItemCount;
    }

    /**
     * 获取当前应该显示第几页
     */
    private int getPagerIndex(int velocityX, int velocityY) {
        final int verticalScrollOffset = mRecyclerView.computeVerticalScrollOffset();
        final int horizontalScrollOffset = mRecyclerView.computeHorizontalScrollOffset();

        final int currentVerticalScrollOffset = mCurrentPageIndex * mRecyclerView.getMeasuredHeight();
        final int currentHorizontalScrollOffset = mCurrentPageIndex * mRecyclerView.getMeasuredWidth();

        int index = 0;
        if (mRecyclerView.getLayoutManager().canScrollVertically()) {
            //除掉整页距离之后的距离
            final float offset = verticalScrollOffset * 1.f % mRecyclerView.getMeasuredHeight();
            final float page = verticalScrollOffset * 1.f / mRecyclerView.getMeasuredHeight();//前面还有多少页
            index = (int) Math.floor(page);//前面还有多少页, 取整
            if (offset == 0) {
                return index;
            }

            if (currentVerticalScrollOffset <= verticalScrollOffset) {
                //向上滚动
                if (offset >= mRecyclerView.getMeasuredHeight() / 2) {
                    //超过一半的距离
                    index = mCurrentPageIndex + 1;
                } else {
                    if (velocityY > 0) {
                        index = mCurrentPageIndex + 1;
                    } else {
                        index = mCurrentPageIndex;
                    }
                }

            } else {
                //向下滚动
                if (offset >= mRecyclerView.getMeasuredHeight() / 2) {
                    //超过一半的距离
                    if (velocityY < 0) {
                        index = mCurrentPageIndex - 1;
                    } else {
                        index = mCurrentPageIndex;
                    }
                } else {
                    index = mCurrentPageIndex - 1;
                }
            }

        } else if (mRecyclerView.getLayoutManager().canScrollHorizontally()) {
            final float offset = horizontalScrollOffset * 1.f % mRecyclerView.getMeasuredWidth();
            final float page = horizontalScrollOffset * 1.f / mRecyclerView.getMeasuredWidth();
            index = (int) Math.floor(page);
            if (offset == 0) {
                return index;
            }

            if (currentHorizontalScrollOffset <= horizontalScrollOffset) {
                //向左滚动
                if (offset >= mRecyclerView.getMeasuredWidth() / 2) {
                    //超过一半的距离
                    index = mCurrentPageIndex + 1;
                } else {
                    if (velocityX > 0) {
                        index = mCurrentPageIndex + 1;
                    } else {
                        index = mCurrentPageIndex;
                    }
                }

            } else {
                //向右滚动
                if (offset >= mRecyclerView.getMeasuredWidth() / 2) {
                    //超过一半的距离
                    if (velocityX < 0) {
                        index = mCurrentPageIndex - 1;
                    } else {
                        index = mCurrentPageIndex;
                    }
                } else {
                    index = mCurrentPageIndex - 1;
                }
            }
        }
        return index;
    }

    private int fixPagerIndex(int index) {
        int maxIndex = mRecyclerView.getLayoutManager().getItemCount() / mPageItemCount - 1;
        int minIndex = 0;
        index = Math.max(minIndex, Math.min(index, maxIndex));
        if (index < mCurrentPageIndex) {
            index = mCurrentPageIndex - 1;
        } else if (index > mCurrentPageIndex) {
            index = mCurrentPageIndex + 1;
        }
        return index;
    }

    /**
     * 页面选择回调监听
     */
    public ViewPagerSnapHelper setPageListener(PageListener pageListener) {
        mPageListener = pageListener;
        return this;
    }

    public interface PageListener {
        void onPageSelector(int position);
    }
}
