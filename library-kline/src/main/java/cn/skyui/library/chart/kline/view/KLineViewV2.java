package cn.skyui.library.chart.kline.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.DimenRes;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ProgressBar;

import java.util.HashMap;
import java.util.Map;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.adapter.KLineChartAdapter;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.draw.v2.BaseChartDraw;
import cn.skyui.library.chart.kline.draw.v2.CandleDrawV2;
import cn.skyui.library.chart.kline.draw.v2.MacdDrawV2;
import cn.skyui.library.chart.kline.draw.v2.VolumeDrawV2;

/**
 * k线图
 */
public class KLineViewV2 extends ScrollAndScaleView {

    KLineChartAdapter mAdapter;

    private int mWidth = 0;
    private int mHeight = 0;

    private ValueAnimator mAnimator;
    private long mAnimationDuration = 500;

    private Rect mKLineRect;
    private Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Rect mCandleRect;
    private CandleDrawV2 mCandleDraw;
    private VolumeDrawV2 mVolumeDraw;
    private MacdDrawV2 mMACDDraw;
//    private BollDraw mBOLLDraw;
//    private RsiDraw mRSIDraw;
//    private KdjDraw mKDJDraw;

    private boolean isShowFirstChildRect;
    private float mFirstChildRectHeight;
    private Rect mFirstChildRect;

    private boolean isShowSecondChildRect;
    private float mSecondChildRectHeight;
    private Rect mSecondChildRect;

    ProgressBar mProgressBar;
    private boolean isRefreshing = false;
    private boolean isLoadMoreEnd = false;
    private boolean mLastScrollEnable;
    private boolean mLastScaleEnable;
    private KChartRefreshListener mRefreshListener;

    public interface KChartRefreshListener {
        void onLoadMore(KLineViewV2 chart);
    }

    /**
     * 设置刷新监听
     */
    public void setRefreshListener(KChartRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    public KLineViewV2(Context context) {
        super(context);
        init();
    }

    public KLineViewV2(Context context, AttributeSet attrs) {
        super(context, attrs);
        initAttrs(attrs);
        init();
    }

    public KLineViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(attrs);
        init();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.KChartView);
        if (array != null) {
            try {
                setShowFirstChildRect(array.getBoolean(R.styleable.KLineView_kv_is_show_first_child_rect, true));
                setFirstChildRectHeight(array.getDimension(R.styleable.KLineView_kv_first_child_rect_height, getDimension(R.dimen.kline_first_child_rect_height)));
                setShowSecondChildRect(array.getBoolean(R.styleable.KLineView_kv_is_show_second_child_rect, true));
                setSecondChildRectHeight(array.getDimension(R.styleable.KLineView_kv_second_child_rect_height, getDimension(R.dimen.kline_second_child_rect_height)));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                array.recycle();
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.mHeight = h;
        float oneThirdHeight = mHeight / 3.0f;
        if (mFirstChildRectHeight > oneThirdHeight) {
            mFirstChildRectHeight = oneThirdHeight;
        }
        if (mSecondChildRectHeight > oneThirdHeight) {
            mSecondChildRectHeight = oneThirdHeight;
        }
        initRect();
        initView();
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

        int strokeWidth = 2;
        mGridPaint.setStrokeWidth(strokeWidth);
        mGridPaint.setAntiAlias(true);
        mGridPaint.setColor(Color.RED);
        mGridPaint.setStyle(Paint.Style.STROKE);

        mCandleDraw = new CandleDrawV2(getContext()) {
            @Override
            public KLine getItem(int position) {
                return mAdapter.getItem(position);
            }

            @Override
            public int getCount() {
                return mAdapter.getCount();
            }
        };
        mVolumeDraw = new VolumeDrawV2(getContext()) {
            @Override
            public KLine getItem(int position) {
                return mAdapter.getItem(position);
            }

            @Override
            public int getCount() {
                return mAdapter.getCount();
            }
        };
//        mMACDDraw = new MacdDrawV2(getContext()) {
//            @Override
//            public KLine getItem(int position) {
//                return null;
//            }
//
//            @Override
//            public int getCount() {
//                return 0;
//            }
//        };
//        mKDJDraw = new KdjDraw(getContext());
//        mRSIDraw = new RsiDraw(getContext());
//        mBOLLDraw = new BollDraw(getContext());

        addChildDraw(ChartEnum.MACD.name(), mMACDDraw);
//        addChildDraw(ChartEnum.KDJ.name(), mKDJDraw);
//        addChildDraw(ChartEnum.RSI.name(), mRSIDraw);
//        addChildDraw(ChartEnum.BOOL.name(),mBOLLDraw);
        setChildDraw(ChartEnum.MACD.name());
    }

    private void initView() {
        mProgressBar = new ProgressBar(getContext());
        LayoutParams layoutParams = new LayoutParams(dp2px(50), dp2px(50));
        layoutParams.addRule(CENTER_IN_PARENT);
        addView(mProgressBar, layoutParams);
        mProgressBar.setVisibility(GONE);
    }

    private void initRect() {
        mKLineRect = new Rect(1, 1, mWidth-1, mHeight-1);
        if (!isShowFirstChildRect && !isShowSecondChildRect) {
            mCandleRect = new Rect(0, 0, mWidth, mHeight);
        } else if (isShowFirstChildRect && isShowSecondChildRect) {
            mCandleRect = new Rect(0, 0, mWidth, (int) (mHeight - mFirstChildRectHeight - mSecondChildRectHeight));
            mFirstChildRect = new Rect(0, mCandleRect.bottom, mWidth, (int) (mCandleRect.bottom + mFirstChildRectHeight));
            mSecondChildRect = new Rect(0, mFirstChildRect.bottom, mWidth, (int) (mFirstChildRect.bottom + mSecondChildRectHeight));
        } else if(isShowFirstChildRect) {
            mCandleRect = new Rect(0, 0, mWidth, (int) (mHeight - mFirstChildRectHeight));
            mFirstChildRect = new Rect(0, mCandleRect.bottom, mWidth, (int) (mCandleRect.bottom + mFirstChildRectHeight));
        } else {
            mCandleRect = new Rect(0, 0, mWidth, (int) (mHeight - mFirstChildRectHeight));
            mFirstChildRect = new Rect(0, mCandleRect.bottom, mWidth, (int) (mCandleRect.bottom + mSecondChildRectHeight));
        }

        mCandleDraw.setRect(mCandleRect);
        mVolumeDraw.setRect(mFirstChildRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mWidth == 0 || mCandleRect.height() == 0 || mItemCount == 0) {
            return;
        }
        canvas.save();
        canvas.scale(1, 1);
        canvas.drawRect(mKLineRect, mGridPaint);
        mCandleDraw.drawGird(canvas);
        mCandleDraw.calculateValue(mScrollX);
        mCandleDraw.drawCandleChart(canvas, mScrollX);
        mVolumeDraw.calculateValue(mCandleDraw.getStartIndex(), mCandleDraw.getStopIndex());
        mVolumeDraw.drawChart(canvas, mScrollX, mCandleDraw.getStartIndex(), mCandleDraw.getStopIndex());
        canvas.restore();
    }

    private Boolean isWR = false;
    private Boolean isShowChild = false;
    private Boolean isShowVol = true;

    private BaseChartDraw mChildDraw;
    private String mChildDrawType;
    private Map<String, BaseChartDraw> mChildDraws = new HashMap<>();
    private float mChildMaxValue = Float.MIN_VALUE;
    private float mChildMinValue = Float.MAX_VALUE;

    public void calculateSubChartValue(int mStartIndex, int mStopIndex) {
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine point = (KLine) mAdapter.getItem(i);
            mChildMaxValue = Math.max(mChildMaxValue, point.getChildData(mChildDrawType).getMaxValue());
            mChildMinValue = Math.min(mChildMinValue, point.getChildData(mChildDrawType).getMinValue());
        }

        if (Math.abs(mChildMaxValue) < 0.01 && Math.abs(mChildMinValue) < 0.01) {
            mChildMaxValue = 1f;
        } else if (mChildMaxValue == mChildMinValue) {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mChildMaxValue += Math.abs(mChildMaxValue * 0.05f);
            mChildMinValue -= Math.abs(mChildMinValue * 0.05f);
            if (mChildMaxValue == 0) {
                mChildMaxValue = 1f;
            }
        }

        if (isWR) {
            mChildMaxValue = 0f;
            if (Math.abs(mChildMinValue) < 0.01)
                mChildMinValue = -10.00f;
        }
    }

    public void drawSubChart(Canvas canvas, int scrollX, int mStartIndex, int mStopIndex) {
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine currentPoint = mAdapter.getItem(i);
            int scrollOutCount = mAdapter.getCount() - i;
            float currentPointX = mSecondChildRect.width() - getX(scrollOutCount) + mCandleDraw.getCandlePadding() / 2 + scrollX;
            KLine lastPoint = i == 0 ? currentPoint : mAdapter.getItem(i - 1);
            float prevX = i == 0 ? currentPointX : mSecondChildRect.width() - getX(scrollOutCount + 1) + mCandleDraw.getCandlePadding() / 2 + scrollX;
            mChildDraw.drawChart(lastPoint.vol, currentPoint.vol, prevX, currentPointX, canvas, i);
        }
    }

    /**
     * 设置子图的绘制方法
     *
     * @param mSelectedChartName
     */
    public void setChildDraw(String mSelectedChartName) {
        this.mChildDrawType = mSelectedChartName;
        this.mChildDraw = mChildDraws.get(mSelectedChartName);
        if (!isShowChild) {
            isShowChild = true;
            initRect();
        }
        invalidate();
    }

    /**
     * 给子区域添加画图方法
     *
     * @param name      显示的文字标签
     * @param childDraw IChartDraw
     */
    public void addChildDraw(String name, BaseChartDraw childDraw) {
        mChildDraws.put(name, childDraw);
    }


    public float getX(int position) {
        return position * mCandleDraw.getCandleWidth();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
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
            checkAndFixScrollX();
        } else {
            setScrollX(0);
        }
        invalidate();
    }

    @Override
    public void onLeftSide() {
        showLoading();
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    public void showLoading() {
        if (!isLoadMoreEnd && !isRefreshing) {
            isRefreshing = true;
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            if (mRefreshListener != null) {
                mRefreshListener.onLoadMore(this);
            }
            mLastScaleEnable = isScaleEnable();
            mLastScrollEnable = isScrollEnable();
            super.setScrollEnable(false);
            super.setScaleEnable(false);
        }
    }

    private void hideLoading() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
        super.setScrollEnable(mLastScrollEnable);
        super.setScaleEnable(mLastScaleEnable);
    }

    /**
     * 刷新完成
     */
    public void refreshComplete() {
        isRefreshing = false;
        hideLoading();
    }

    /**
     * 刷新完成，没有数据
     */
    public void refreshEnd() {
        isLoadMoreEnd = true;
        isRefreshing = false;
        hideLoading();
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
        float mDataLen = (mItemCount - 1) * mCandleDraw.getCandleWidth();
        return (int) (mDataLen - mWidth / mScaleX + mCandleDraw.getCandleWidth() / 2);
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

    public int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
