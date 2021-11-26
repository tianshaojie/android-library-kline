package cn.skyui.library.chart.kline.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.v4.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.ScaleGestureDetector;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.adapter.KLineChartAdapter;
import cn.skyui.library.chart.kline.base.IAdapter;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.draw.CandleDraw;
import cn.skyui.library.chart.kline.draw.v2.CandleDrawV2;

/**
 * k线图
 */
public class KLineView extends ScrollAndScaleView{

    KLineChartAdapter mAdapter;

    private int mWidth = 0;
    private int mHeight = 0;

    // 数据长度
    private float mDataLen = 0;
    // 蜡烛图+间距的宽度
    private float mPointWidth = 6;

    private ValueAnimator mAnimator;
    private long mAnimationDuration = 500;


    private Rect mCandleRect;
    private CandleDrawV2 mCandleDraw;

    private boolean isShowFirstChildRect;
    private float mFirstChildRectHeight;
    private Rect mFirstChildRect;

    private boolean isShowSecondChildRect;
    private float mSecondChildRectHeight;
    private Rect mSecondChildRect;

    public KLineView(Context context) {
        super(context);
        init();
    }

    public KLineView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }

    public KLineView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.KChartView);
        if (array != null) {
            try {
                setShowFirstChildRect(array.getBoolean(R.styleable.KLineView_kv_is_show_first_child_rect, true));
                setShowSecondChildRect(array.getBoolean(R.styleable.KLineView_kv_is_show_second_child_rect, true));
                setFirstChildRectHeight(array.getDimension(R.styleable.KLineView_kv_first_child_rect_height, getDimension(R.dimen.kline_first_child_rect_height)));
                setSecondChildRectHeight(array.getDimension(R.styleable.KLineView_kv_second_child_rect_height, getDimension(R.dimen.kline_second_child_rect_height)));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                array.recycle();
            }
        }
    }

    private void init() {
        setWillNotDraw(false);
        mDetector = new GestureDetectorCompat(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);

        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setDuration(mAnimationDuration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });
    }

    private void initView() {
        mCandleDraw = new CandleDrawV2(getContext(), mCandleRect) {
            @Override
            public KLine getItem(int position) {
                return mAdapter.getItem(position);
            }

            @Override
            public int getCount() {
                return mAdapter.getCount();
            }
        };
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.mHeight = h;
        float oneThirdHeight = mHeight / 3.0f;
        if(mFirstChildRectHeight > oneThirdHeight) {
            mFirstChildRectHeight = oneThirdHeight;
        }
        if(mSecondChildRectHeight > oneThirdHeight) {
            mSecondChildRectHeight = oneThirdHeight;
        }
        initRect();
        initView();
    }

    private void initRect() {
        if(!isShowFirstChildRect && !isShowSecondChildRect) {
            mCandleRect = new Rect(0, 0, mWidth, mHeight);
        } else if(isShowFirstChildRect && isShowSecondChildRect) {
            mCandleRect = new Rect(0, 0, mWidth, (int) (mHeight - mFirstChildRectHeight - mSecondChildRectHeight));
            mFirstChildRect = new Rect(0, mCandleRect.bottom, mWidth, (int) (mCandleRect.bottom + mFirstChildRectHeight));
            mSecondChildRect = new Rect(0, mFirstChildRect.bottom, mWidth, (int) (mFirstChildRect.bottom + mSecondChildRectHeight));
        } else {
            mCandleRect = new Rect(0, 0, mWidth, (int) (mHeight - mFirstChildRectHeight));
            mFirstChildRect = new Rect(0, mCandleRect.bottom, mWidth, (int) (mCandleRect.bottom + mFirstChildRectHeight));
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mCandleRect.height() == 0) {
            return;
        }
//        mCandleDraw.calculateValue();
        canvas.save();
        canvas.scale(1, 1);
        mCandleDraw.drawGird(canvas);
        canvas.restore();
    }

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mItemCount = getAdapter().getCount();
            notifyChanged();
        }

        @Override
        public void onInvalidated() {
            mItemCount = getAdapter().getCount();
            notifyChanged();
        }
    };

    public KLineChartAdapter getAdapter() {
        return mAdapter;
    }


    /**
     * 设置数据适配器
     */
    public void setAdapter(KLineChartAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
            mItemCount = mAdapter.getCount();
        } else {
            mItemCount = 0;
        }
        notifyChanged();
    }

    private int mItemCount;
    /**
     * 重新计算并刷新线条
     */
    public void notifyChanged() {
        if (mItemCount != 0) {
            mDataLen = (mItemCount - 1) * mPointWidth;
            mCandleDraw.setDataLen(mDataLen);
            checkAndFixScrollX();
            setTranslateXFromScrollX(mScrollX);
        } else {
            setScrollX(0);
        }
        invalidate();
    }

    private float mTranslateX = Float.MIN_VALUE;

    /**
     * scrollX 转换为 TranslateX
     *
     * @param scrollX
     */
    private void setTranslateXFromScrollX(int scrollX) {
        mTranslateX = scrollX + getMinTranslateX();
    }

    @Override
    public void onLeftSide() {
        showLoading();
    }

    private void showLoading() {
    }

    @Override
    public void onRightSide() {

    }

    @Override
    public int getMinScrollX() {
        return 0;
    }

    @Override
    public int getMaxScrollX() {
        return Math.round(getMaxTranslateX() - getMinTranslateX());
    }

    /**
     * 获取平移的最小值
     *
     * @return
     */
    private float getMinTranslateX() {
        return -mDataLen + mWidth / mScaleX - mPointWidth / 2;
    }

    /**
     * 获取平移的最大值
     *
     * @return
     */
    private float getMaxTranslateX() {
        if (!isFullScreen()) {
            return getMinTranslateX();
        }
        return mPointWidth / 2;
    }

    /**
     * 数据是否充满屏幕
     *
     * @return
     */
    public boolean isFullScreen() {
        return mDataLen >= mWidth / mScaleX;
    }

    private float getDimension(@DimenRes int resId) {
        return getResources().getDimension(resId);
    }

    public void setShowFirstChildRect(boolean showFirstChildRect) {
        isShowFirstChildRect = showFirstChildRect;
    }

    public void setShowSecondChildRect(boolean showSecondChildRect) {
        isShowSecondChildRect = showSecondChildRect;
    }


    public void setFirstChildRectHeight(float mFirstChildRectHeight) {
        this.mFirstChildRectHeight = mFirstChildRectHeight;
    }

    public void setSecondChildRectHeight(float mSecondChildRectHeight) {
        this.mSecondChildRectHeight = mSecondChildRectHeight;
    }
}
