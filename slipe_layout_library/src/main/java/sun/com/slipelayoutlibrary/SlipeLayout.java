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
public class SlipeLayout extends RelativeLayout {
    private final int OUTSLIPESPEED = 1000;
    private final int INSLIPESPEED = -1000;
    private final int OUTSLIPELENGTH = 500;
    private final int INSLIPELENGTH = -500;

    private SlipeViewDragHelper mDragger;

    private View mDragView;

    private Point mAutoBackOriginPos = new Point();

    public SlipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (getChildCount() > 1) {
            throw new RuntimeException("only have one child view");
        }
        init();

    }


    private void init() {
        mDragger = SlipeViewDragHelper.create(this, 1.0f, new SlipeCallback());
        mDragger.setEdgeTrackingEnabled(SlipeViewDragHelper.EDGE_LEFT);
    }


    private class SlipeCallback extends SlipeViewDragHelper.Callback {
        private int slipeLeft;
        private float downLeft;
        private float clampLeft;
        private float childLeft;
        private float moveLeft;

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
            //mEdgeTrackerView禁止直接移动
            return child == mDragView;
        }

        @Override
        public void onViewDown(MotionEvent event) {
            childLeft = getChildAt(0).getLeft();
            downLeft = event.getX();
        }

        @Override
        public void onViewMove(MotionEvent event) {
            moveLeft = event.getX();
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
            slipeLeft = left;
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
            clampLeft = left;
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
            //mAutoBackView手指释放时可以自动回去
            if (releasedChild == mDragView) {
                float length = moveLeft - downLeft;
                Log.d("SHF", "onViewReleased--slipeLeft-->" + slipeLeft + "--downLeft-->" + downLeft + "--clampLeft-->" + clampLeft + "--moveLeft-->" + moveLeft);
                Log.d("SHF", "onViewReleased--左滑还是右滑-->" + length + "--速度xvel-->" + xvel + "--当前位置childLeft-->" + childLeft + "--length-->" + length);
                if (length >= 0) {//右滑
                    if (xvel >= 0 && xvel <= OUTSLIPESPEED//右滑 速度慢 没过最大滑动距离
                            && childLeft == mAutoBackOriginPos.x
                            && length <= OUTSLIPELENGTH) {
                        mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
                        invalidate();
                        return;
                    }

                    if (xvel >= 0 && xvel <= OUTSLIPESPEED
                            && childLeft == mAutoBackOriginPos.x
                            && length >= OUTSLIPELENGTH) {//右滑 速度慢 超过最大滑动距离
                        mDragger.settleCapturedViewAt(getWidth(), mAutoBackOriginPos.y);
                        invalidate();
                        return;
                    }

                    if (xvel > OUTSLIPESPEED) {//右滑 速度快
                        mDragger.settleCapturedViewAt(getWidth(), mAutoBackOriginPos.y);
                        invalidate();
                        return;
                    }
                } else {//左滑
                    if (xvel >= INSLIPESPEED && xvel <= 0
                            && childLeft == getWidth()
                            && length >= INSLIPELENGTH) {//左滑 速度慢 没过最大滑动距离
                        mDragger.settleCapturedViewAt(getWidth(), mAutoBackOriginPos.y);
                        invalidate();
                        return;
                    }

                    if (xvel >= INSLIPESPEED && xvel <= 0
                            && childLeft == getWidth()
                            && length <= OUTSLIPELENGTH) {//右滑 速度慢 超过最大滑动距离
                        mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
                        invalidate();
                        return;
                    }

                    if (xvel < OUTSLIPESPEED) {//左滑 速度快
                        mDragger.settleCapturedViewAt(mAutoBackOriginPos.x, mAutoBackOriginPos.y);
                        invalidate();
                        return;
                    }
                }
            }
        }

        //在边界拖动时回调
        @Override
        public void onEdgeDragStarted(int edgeFlags, int pointerId) {
            Log.d("SHF", "onEdgeDragStarted");
        }

        @Override
        public int getViewHorizontalDragRange(View child) {
            return super.getViewHorizontalDragRange(child);
        }

        @Override
        public int getViewVerticalDragRange(View child) {
            return super.getViewVerticalDragRange(child);
        }
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

    @Override
    public void computeScroll() {
        if (mDragger.continueSettling(true)) {
            invalidate();
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        //记录初始坐标
        mAutoBackOriginPos.x = mDragView.getLeft();
        mAutoBackOriginPos.y = mDragView.getTop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mDragView = getChildAt(0);
    }

}
