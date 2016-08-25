package sun.com.slipelayoutlibrary;

import android.content.Context;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

/**
 * Created by walkingMen on 16/8/23.
 * 滑动布局
 */
public class SlideLayout extends RelativeLayout {
    private OnSlideStatusListener slideStatusListener;

    private final int OUTSLIPESPEED = 1000;
    private final int INSLIPESPEED = -1000;

    private SlideViewDragHelper mDragger;

    private View mDragView;

    private Point mAutoBackOriginPos = new Point();
    private boolean haveSavePoint = false;
    private boolean isOut = false;
    private boolean isMove = false;
    private boolean isScroll = false;

    public SlideLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public void setSlideStatusListener(OnSlideStatusListener slideStatusListener) {
        this.slideStatusListener = slideStatusListener;
    }

    private void init() {
        mDragger = SlideViewDragHelper.create(this, 1.0f, new SlipeCallback());
        mDragger.setEdgeTrackingEnabled(SlideViewDragHelper.EDGE_LEFT);
    }


    private class SlipeCallback extends SlideViewDragHelper.Callback {
        private int screenMiddle;//屏幕中间
        private float downLeft;//点击 child0 的left
        private float beforeChildLeft;//点击 child0 的left
        private float childLeft;//移动时  child0 的left
        private float recentChildLeft;//最近执行移动的childLeft
        private float moveLeft;

        private float dragXSpeed;


        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //mEdgeTrackerView禁止直接移动
            return child == mDragView;
        }

        @Override
        public void onViewDown(MotionEvent event) {
            childLeft = getChildAt(0).getLeft();
            if (!isScroll() && !isMove) {//如果正在滚动 不记录按下left
                beforeChildLeft = getChildAt(0).getLeft();
                downLeft = event.getX();
            }
        }

        @Override
        public void onViewMove(MotionEvent event) {
            if (!isScroll()) {//如果正在滚动 不记录移动left
                moveLeft = event.getX();
            }
        }

        @Override
        public void onViewDragMove(MotionEvent event) {
            childLeft = getChildAt(0).getLeft();
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            isMove = true;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            return left;
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
            return super.clampViewPositionVertical(child, top, dy);
        }

        @Override
        public void onViewCaptured(View capturedChild, int activePointerId) {
            super.onViewCaptured(capturedChild, activePointerId);
        }

        //手指释放的时候回调
        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
            dragXSpeed = xvel;
            screenMiddle = getWidth() / 2;

            //mAutoBackView手指释放时可以自动回去
            if (releasedChild == mDragView) {
                float length = moveLeft - downLeft;//手指移动的距离
                recentChildLeft = childLeft;
                Log.d("SHF", "onViewReleased--left or right-->" + (length >= 0 ? "right" : "left") + "--速度xvel-->" + xvel + "--当前位置childLeft-->" + childLeft + "--length-->" + length);

                if (length >= 0) {//右滑

                    if (childLeft > getWidth()) {
                        slideOut();
                        invalidate();
                        return;
                    }

                    if (xvel <= 0 && childLeft < screenMiddle) {
                        slideIn();
                        invalidate();
                        return;
                    }

                    if (xvel <= 0 && childLeft > screenMiddle) {//右滑 但是滑动时快速点击 将滑动速度变成了负数
                        slideOut();
                        invalidate();
                        return;
                    }

                    if (xvel >= 0 && xvel <= OUTSLIPESPEED//右滑 速度慢 没过最大滑动距离
                            && (childLeft <= screenMiddle && childLeft >= 0)) {
                        slideIn();
                        invalidate();
                        return;
                    }

                    if (xvel >= 0 && xvel <= OUTSLIPESPEED
                            && (childLeft > screenMiddle)) {//右滑 速度慢 超过最大滑动距离
                        slideOut();
                        invalidate();
                        return;
                    }

                    if (xvel > OUTSLIPESPEED) {//右滑 速度快
                        slideOut();
                        invalidate();
                        return;
                    }

                } else {//左滑
                    if (childLeft < 0) {//滑到左屏幕外头
                        slideIn();
                        invalidate();
                        return;
                    }

                    if (xvel >= 0 && childLeft < screenMiddle) {
                        slideIn();
                        invalidate();
                        return;
                    }

                    if (xvel >= 0 && childLeft > screenMiddle) {//左滑 但是滑动时快速点击 将滑动速度变成了整的
                        slideOut();
                        invalidate();
                        return;
                    }

                    if (xvel >= INSLIPESPEED && xvel <= 0
                            && (childLeft > screenMiddle)) {//左滑 速度慢 没过最大滑动距离
                        slideOut();
                        invalidate();
                        return;
                    }

                    if (xvel >= INSLIPESPEED && xvel <= 0
                            && (childLeft <= screenMiddle && childLeft >= 0)) {//左滑 速度慢 超过最大滑动距离
                        slideIn();
                        invalidate();
                        return;
                    }

                    if (xvel < INSLIPESPEED) {//左滑 速度快
                        slideIn();
                        invalidate();
                        return;
                    }
                }
            }
        }


        /**
         * 滚动完成监听
         *
         * @param isComplete
         */
        @Override
        public void onStartScrollListener(boolean isComplete) {
            isScroll = true;
            if (isComplete && slideStatusListener != null) {
                float length = moveLeft - downLeft;//手指移动的距离
                isScroll = false;
                isMove = false;
                if (recentChildLeft < 0) {
                    return;
                }
                if (recentChildLeft > getWidth()) {
                    return;
                }
                Log.d("SHF", "onstartScrollListener--left or right-->"
                        + (length >= 0 ? "right" : "left") + "--dragXSpeed-->"
                        + dragXSpeed + "--当前位置recentChildLeft-->" + recentChildLeft
                        + "--length-->" + length + "--beforeChildLeft-->" + beforeChildLeft);

                if (beforeChildLeft <= mAutoBackOriginPos.x) {
                    if (recentChildLeft > screenMiddle) {
                        slideStatusListener.slideOutComplete();
                        return;
                    }
                    if (dragXSpeed > OUTSLIPESPEED) {//右滑 速度快
                        slideStatusListener.slideOutComplete();
                        return;
                    }
                }
                if (beforeChildLeft >= getWidth()) {
                    if (recentChildLeft < screenMiddle) {
                        slideStatusListener.slideInComplete();
                        return;
                    }

                    if (dragXSpeed < INSLIPESPEED) {
                        slideStatusListener.slideInComplete();
                        return;
                    }
                }

            }
            return;
        }

    }

    private void slideOut() {
        mDragger.settleCapturedViewAt(getWidth(), mAutoBackOriginPos.y);
        isOut = true;
    }

    private void slideIn() {
        mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
        isOut = false;
    }


    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return mDragger.shouldInterceptTouchEvent(event);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mDragger.processTouchEvent(event);
        return true;
    }

    /**
     * http://my.oschina.net/ososchina/blog/600281
     */
    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int count = getChildCount();
        if (count == 0) {
            throw new RuntimeException("you must have one child view!!!");
        }
        if (!isOut && !isMove) {
            super.onLayout(changed, l, t, r, b);
        } else {
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() != GONE) {
                    child.layout(child.getLeft(), child.getTop(), child.getRight(), child.getBottom());
                }
            }
        }

        if (!haveSavePoint) {
            //记录初始坐标
            mAutoBackOriginPos.x = mDragView.getLeft();
            mAutoBackOriginPos.y = mDragView.getTop();
            haveSavePoint = true;
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragView = getChildAt(0);
    }

    public interface OnSlideStatusListener {
        void slideOutComplete();

        void slideInComplete();
    }

}
