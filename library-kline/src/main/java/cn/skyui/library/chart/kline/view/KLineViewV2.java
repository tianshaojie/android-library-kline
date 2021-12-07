package cn.skyui.library.chart.kline.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

import java.util.HashMap;
import java.util.Map;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.adapter.KLineChartAdapter;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.draw.v2.BaseChartDraw;
import cn.skyui.library.chart.kline.draw.v2.BollDrawV2;
import cn.skyui.library.chart.kline.draw.v2.CandleDrawV2;
import cn.skyui.library.chart.kline.draw.v2.KdjDrawV2;
import cn.skyui.library.chart.kline.draw.v2.MacdDrawV2;
import cn.skyui.library.chart.kline.draw.v2.RsiDrawV2;
import cn.skyui.library.chart.kline.draw.v2.VolumeDrawV2;

/**
 * k线图
 */
public class KLineViewV2 extends ScrollAndScaleView {

    private KLineChartAdapter mAdapter;
    private int mStartIndex = 0; // 可见区域数据List的开始索引位置
    private int mStopIndex = 0;  // 可见区域数据List的结束索引位置
    private int mSelectedIndex;

    private int mWidth = 0;
    private int mHeight = 0;

    private Rect mKLineRect;
    private Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mMaxMinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 长按浮窗
    private Paint mSelectorWindowTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectorWindowBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 长按十字线/文字
    private Paint mSelectedXLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectedYLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectedPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectorFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Rect mCandleRect;
    private CandleDrawV2 mCandleDraw;
    private VolumeDrawV2 mVolumeDraw;
    private MacdDrawV2 mMACDDraw;
    private KdjDrawV2 mKDJDraw;
    private RsiDrawV2 mRSIDraw;
    private BollDrawV2 mBOLLDraw;

    private BaseChartDraw mChildDraw;
    private Map<String, BaseChartDraw> mChildDraws = new HashMap<>();

    private boolean isShowVol = true;
    private float mVolRectHeight;
    private Rect mVolRect;

    private boolean isShowChild = true;
    private float mChildRectHeight;
    private Rect mChildRect;

    private ProgressBar mProgressBar;
    private boolean isRefreshing = false;
    private boolean isLoadMoreEnd = false;
    private boolean mLastScrollEnable;
    private boolean mLastScaleEnable;
    private KChartRefreshListener mRefreshListener;

    public interface KChartRefreshListener {
        void onLoadMore(KLineViewV2 chart);
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

    public KLineViewV2(Context context) {
        super(context);
        init();
        initView();
    }

    public KLineViewV2(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
        initAttrs(attrs);
        initView();
    }

    public KLineViewV2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
        initAttrs(attrs);
        initView();
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.KChartView);
        if (array == null) {
            return;
        }
        try {
            // kline
            setShowVol(array.getBoolean(R.styleable.KLineView_kline_is_show_vol_rect, true));
            setVolRectHeight(array.getDimension(R.styleable.KLineView_kline_vol_rect_height, getDimension(R.dimen.kline_vol_rect_height)));
            setShowChild(array.getBoolean(R.styleable.KLineView_kline_is_show_child_rect, true));
            setChildRectHeight(array.getDimension(R.styleable.KLineView_kline_child_rect_height, getDimension(R.dimen.kline_child_rect_height)));
            // 背景色
            setBackgroundColor(array.getColor(R.styleable.KLineView_kline_background_color, getColor(R.color.chart_background)));
            // 坐标格线条颜色宽度
            setGridLineWidth(array.getDimension(R.styleable.KLineView_kline_grid_line_width, getDimension(R.dimen.chart_grid_line_width)));
            setGridLineColor(array.getColor(R.styleable.KLineView_kline_grid_line_color, getColor(R.color.chart_grid_line_color)));
            // 各类均线宽度
            setLineWidth(array.getDimension(R.styleable.KLineView_kline_line_width, getDimension(R.dimen.chart_line_width)));
            // 默认字体大小颜色
            setTextSize(array.getDimension(R.styleable.KLineView_kline_text_size, getDimension(R.dimen.chart_text_size)));
            setTextColor(array.getColor(R.styleable.KLineView_kline_text_color, getColor(R.color.chart_text)));
            // 设置最大值/最小值文字大小颜色
            setMaxMinTextSize(array.getDimension(R.styleable.KLineView_kline_max_min_text_size, getDimension(R.dimen.chart_max_min_text_size)));
            setMaxMinTextColor(array.getColor(R.styleable.KLineView_kline_max_min_text_color, getColor(R.color.chart_max_min_text)));
            // 选中十字线颜色宽度
            setSelectedXLineColor(array.getColor(R.styleable.KLineView_kline_selected_x_line_color, getColor(R.color.chart_selected_x_line_color)));
            setSelectedXLineWidth(array.getDimension(R.styleable.KLineView_kline_selected_x_line_width, getDimension(R.dimen.chart_selected_x_line_width)));
            setSelectedYLineColor(array.getColor(R.styleable.KLineView_kline_selected_y_line_color, getColor(R.color.chart_selected_y_line_color)));
            setSelectedYLineWidth(array.getDimension(R.styleable.KLineView_kline_selected_y_line_width, getDimension(R.dimen.chart_selected_y_line_width)));
            // 选中十字线文字框的背景色
            setSelectedPointTextBackgroundColor(array.getColor(R.styleable.KLineView_kline_selected_point_text_bg_color, getColor(R.color.chart_selected_point_text_bg_color)));
            // 选中浮窗背景色字体色
            setSelectorWindowBackgroundColor(array.getColor(R.styleable.KLineView_kline_selector_window_bg_color, getColor(R.color.chart_selector_window_bg_color)));
            setSelectorWindowTextSize(array.getDimension(R.styleable.KLineView_kline_selected_window_text_size, getDimension(R.dimen.chart_selected_window_text_size)));

            //candle
            setMa5Color(array.getColor(R.styleable.KLineView_kline_dif_color, getColor(R.color.chart_ma5)));
            setMa10Color(array.getColor(R.styleable.KLineView_kline_dea_color, getColor(R.color.chart_ma10)));
            setMa20Color(array.getColor(R.styleable.KLineView_kline_macd_color, getColor(R.color.chart_ma20)));
            // 蜡烛图宽度，不包括左右padding
            setCandleWidth(array.getDimension(R.styleable.KLineView_kline_candle_width, getDimension(R.dimen.chart_candle_width)));
            // 蜡烛图上下线的宽度
            setCandleLineWidth(array.getDimension(R.styleable.KLineView_kline_candle_line_width, getDimension(R.dimen.chart_candle_line_width)));
            setCandleSolid(array.getBoolean(R.styleable.KLineView_kline_candle_solid, true));

            //macd
            setMACDWidth(array.getDimension(R.styleable.KLineView_kline_macd_width, getDimension(R.dimen.chart_candle_width)));
            setDIFColor(array.getColor(R.styleable.KLineView_kline_dif_color, getColor(R.color.chart_ma5)));
            setDEAColor(array.getColor(R.styleable.KLineView_kline_dea_color, getColor(R.color.chart_ma10)));
            setMACDColor(array.getColor(R.styleable.KLineView_kline_macd_color, getColor(R.color.chart_ma20)));
            //kdj
            setKColor(array.getColor(R.styleable.KLineView_kline_dif_color, getColor(R.color.chart_ma5)));
            setDColor(array.getColor(R.styleable.KLineView_kline_dea_color, getColor(R.color.chart_ma10)));
            setJColor(array.getColor(R.styleable.KLineView_kline_macd_color, getColor(R.color.chart_ma20)));
            //rsi
            setRSI1Color(array.getColor(R.styleable.KLineView_kline_dif_color, getColor(R.color.chart_ma5)));
            setRSI2Color(array.getColor(R.styleable.KLineView_kline_dea_color, getColor(R.color.chart_ma10)));
            setRSI3Color(array.getColor(R.styleable.KLineView_kline_macd_color, getColor(R.color.chart_ma20)));
            //boll
            setUpColor(array.getColor(R.styleable.KLineView_kline_dif_color, getColor(R.color.chart_ma5)));
            setMbColor(array.getColor(R.styleable.KLineView_kline_dea_color, getColor(R.color.chart_ma10)));
            setDnColor(array.getColor(R.styleable.KLineView_kline_macd_color, getColor(R.color.chart_ma20)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            array.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        this.mHeight = h;
        float oneThirdHeight = mHeight / 3.0f;
        if (mVolRectHeight > oneThirdHeight) {
            mVolRectHeight = oneThirdHeight;
        }
        if (mChildRectHeight > oneThirdHeight) {
            mChildRectHeight = oneThirdHeight;
        }
        initRect();
    }


    private void init() {
        setWillNotDraw(false);
        mDetector = new GestureDetectorCompat(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);

        mGridPaint.setAntiAlias(true);
        mGridPaint.setColor(Color.RED);
        mGridPaint.setStyle(Paint.Style.STROKE);
        mGridPaint.setStrokeWidth(getResources().getDimension(R.dimen.chart_line_width));

        mCandleDraw = new CandleDrawV2(getContext());
        mVolumeDraw = new VolumeDrawV2(getContext());
        mMACDDraw = new MacdDrawV2(getContext());
        mKDJDraw = new KdjDrawV2(getContext());
        mRSIDraw = new RsiDrawV2(getContext());
        mBOLLDraw = new BollDrawV2(getContext());

        addChildDraw(ChartEnum.MACD.name(), mMACDDraw);
        addChildDraw(ChartEnum.KDJ.name(), mKDJDraw);
        addChildDraw(ChartEnum.RSI.name(), mRSIDraw);
        addChildDraw(ChartEnum.BOOL.name(),mBOLLDraw);
//        setChildDraw(ChartEnum.MACD.name());
    }

    private void initView() {
        mProgressBar = new ProgressBar(getContext());
        LayoutParams layoutParams = new LayoutParams(dp2px(50), dp2px(50));
        layoutParams.addRule(CENTER_IN_PARENT);
        addView(mProgressBar, layoutParams);
        mProgressBar.setVisibility(GONE);
    }

    private void initRect() {
        int paintWidth = (int) mGridPaint.getStrokeWidth()/2;
        mKLineRect = new Rect(paintWidth, paintWidth, mWidth-paintWidth, mHeight-paintWidth);
        if (isShowVol && isShowChild) {
            mCandleRect = new Rect(0, 0, mWidth, (int) (mHeight - mVolRectHeight - mChildRectHeight));
            mVolRect = new Rect(0, mCandleRect.bottom, mWidth, (int) (mCandleRect.bottom + mVolRectHeight));
            mChildRect = new Rect(0, mVolRect.bottom, mWidth, (int) (mVolRect.bottom + mChildRectHeight));
        } else if(isShowVol) {
            mCandleRect = new Rect(0, 0, mWidth, (int) (mHeight - mVolRectHeight));
            mVolRect = new Rect(0, mCandleRect.bottom, mWidth, (int) (mCandleRect.bottom + mVolRectHeight));
        } else if(isShowChild) {
            mCandleRect = new Rect(0, 0, mWidth, (int) (mHeight - mVolRectHeight));
            mChildRect = new Rect(0, mCandleRect.bottom, mWidth, (int) (mCandleRect.bottom + mChildRectHeight));
        } if (!isShowVol && !isShowChild) {
            mCandleRect = new Rect(0, 0, mWidth, mHeight);
        }

        mCandleDraw.setRect(mCandleRect);
        mVolumeDraw.setRect(mVolRect);
        mMACDDraw.setRect(mChildRect);
        mKDJDraw.setRect(mChildRect);
        mRSIDraw.setRect(mChildRect);
        mBOLLDraw.setRect(mChildRect);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
    }

    @Override
    protected void onScaleChanged(float scale, float oldScale) {
        checkAndFixScrollX();
        // setTranslateXFromScrollX(mScrollX);
        super.onScaleChanged(scale, oldScale);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(mBackgroundPaint.getColor());
        if (mWidth == 0 || mCandleRect.height() == 0 || mItemCount == 0) {
            return;
        }
        canvas.save();
        canvas.scale(1, 1);
        canvas.drawRect(mKLineRect, mGridPaint);
        // 计算最大最小值决定坐标
        calculateDataIndex(mScrollX);
        mCandleDraw.calculateValue(mAdapter.getItems(), mStartIndex, mStopIndex);
        mVolumeDraw.calculateValue(mAdapter.getItems(), mStartIndex, mStopIndex);
        mMACDDraw.calculateValue(mAdapter.getItems(), mStartIndex, mStopIndex);
        mCandleDraw.drawGird(canvas);
        drawChart(canvas);
        drawValue(canvas, isLongPress ? mSelectedIndex : mStopIndex);
        canvas.restore();
    }

    /**
     * 根据索引索取X坐标
     *
     * @param position 索引值
     * @return X坐标
     */
    protected float getX(int position) {
        return position * mCandleDraw.getChartItemWidth();
    }

    public void calculateDataIndex(int scrollX) {
        float singleChartWidth = mCandleDraw.getChartItemWidth();
        // 屏幕内+屏幕外右侧画布区域的蜡烛图数量
        float candleCount = (mWidth + scrollX) / singleChartWidth;
        // 屏幕内的蜡烛图数量
        float inRectCandleCount = mWidth / singleChartWidth;
        // 屏幕外右侧的蜡烛图数量
        float scrollOutCount = scrollX / singleChartWidth;
        mStartIndex = mAdapter.getCount() - (int) candleCount - 1;
        if (mStartIndex <= 0) {
            mStartIndex = 0;
        }
        mStopIndex = mAdapter.getCount() - (int) scrollOutCount - 1;
        if (mStopIndex <= inRectCandleCount) {
            mStopIndex = (int) (inRectCandleCount);
        }
        if (mStopIndex > mAdapter.getCount() - 1) {
            mStopIndex = mAdapter.getCount() - 1;
        }
    }

    private void drawChart(Canvas canvas) {
        canvas.save();
        canvas.scale(mScaleX, 1);
        //canvas.translate((mScrollX + getMinTranslateX()) * mScaleX, 0);
        // 画屏幕内的数据图表
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine currentPoint = mAdapter.getItem(i);
            int rightSidePointCount = mAdapter.getCount() - i;
            float currentPointX = mWidth + mScrollX - getX(rightSidePointCount);
            Log.i("KLineView", "mWidth=" +mWidth + ", mScrollX=" + mScrollX + ",currentPointX=" + currentPointX + ", chartItemWidth=" + mCandleDraw.getChartItemWidth());
            KLine prevPoint = i == 0 ? currentPoint : mAdapter.getItem(i - 1);
            float prevX = i == 0 ? currentPointX : mWidth + mScrollX - getX(rightSidePointCount + 1);

            mCandleDraw.drawChartItem(canvas, prevPoint, currentPoint, prevX, currentPointX);
            mVolumeDraw.drawChartItem(canvas, prevPoint, currentPoint, prevX, currentPointX);
            mMACDDraw.drawChartItem(canvas, prevPoint, currentPoint, prevX, currentPointX);
//            mKDJDraw.drawChartItem(canvas, prevPoint, currentPoint, prevX, currentPointX);
//            mRSIDraw.drawChartItem(canvas, prevPoint, currentPoint, prevX, currentPointX);
//            mBOLLDraw.drawChartItem(canvas, prevPoint, currentPoint, prevX, currentPointX);
        }
        //还原 平移缩放
        canvas.restore();
    }

    /**
     * 画值
     *
     * @param canvas
     * @param position 显示某个点的值
     */
    private void drawValue(Canvas canvas, int position) {
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        if (position >= 0 && position < mItemCount) {
            KLine point = (KLine) mAdapter.getItem(position);
            if (mCandleDraw != null) {
                float y = mCandleRect.top + mCandleDraw.getTopPadding() - textHeight/2;
                float x = 0;
                mCandleDraw.drawText(canvas, point, x, y);
            }
            if (mVolumeDraw != null) {
                float y = mVolRect.top + mVolumeDraw.getTopPadding() - textHeight/2;
                mVolumeDraw.drawText(canvas, point, 0, y);
            }
            if (mMACDDraw != null) {
                float y = mChildRect.top + mMACDDraw.getTopPadding() - textHeight/2;
                mMACDDraw.drawText(canvas, point, 0, y);
            }
        }
    }

    /**
     * 设置子图的绘制方法
     *
     * @param mSelectedChartName
     */
    public void setChildDraw(String mSelectedChartName) {
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
        Log.i("KLineView", "mWidth=" + mWidth + ", getChartItemWidth=" + mCandleDraw.getChartItemWidth());

        float mDataLen = (mItemCount - 1) * mCandleDraw.getChartItemWidth();
        return (int) (mDataLen - mWidth / mScaleX + mCandleDraw.getChartItemWidth() / 2);
    }
    /**
     * 设置刷新监听
     */
    public void setRefreshListener(KChartRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    private float getDimension(@DimenRes int resId) {
        return getResources().getDimension(resId);
    }

    private int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(getContext(), resId);
    }


    public void setShowVol(boolean showVol) {
        isShowVol = showVol;
    }

    public void setShowChild(boolean showChild) {
        isShowChild = showChild;
    }


    public void setVolRectHeight(float volRectHeight) {
        this.mVolRectHeight = volRectHeight;
    }

    public void setChildRectHeight(float childRectHeight) {
        this.mChildRectHeight = childRectHeight;
    }

    public int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public void setTextSize(float textSize) {
        mCandleDraw.setTextSize(textSize);
        mBOLLDraw.setTextSize(textSize);
        mRSIDraw.setTextSize(textSize);
        mMACDDraw.setTextSize(textSize);
        mKDJDraw.setTextSize(textSize);
        mVolumeDraw.setTextSize(textSize);
    }

    public void setLineWidth(float lineWidth) {
        mCandleDraw.setLineWidth(lineWidth);
        mBOLLDraw.setLineWidth(lineWidth);
        mRSIDraw.setLineWidth(lineWidth);
        mMACDDraw.setLineWidth(lineWidth);
        mKDJDraw.setLineWidth(lineWidth);
        mVolumeDraw.setLineWidth(lineWidth);
    }

    public void setTextColor(int color) {
        mVolumeDraw.setTextColor(color);
        mMACDDraw.setTextColor(color);
    }

    /**
     * 设置表格线宽度
     */
    public void setGridLineWidth(float width) {
        mGridPaint.setStrokeWidth(width);
    }

    /**
     * 设置表格线颜色
     */
    public void setGridLineColor(int color) {
        mGridPaint.setColor(color);
        mCandleDraw.setGridLineColor(color);
        mVolumeDraw.setGridLineColor(color);
        mMACDDraw.setGridLineColor(color);
        mKDJDraw.setGridLineColor(color);
        mBOLLDraw.setGridLineColor(color);
        mRSIDraw.setGridLineColor(color);
    }

    /**
     * 设置最大值/最小值文字颜色
     */
    public void setMaxMinTextColor(int color) {
        mMaxMinPaint.setColor(color);
    }

    /**
     * 设置最大值/最小值文字大小
     */
    public void setMaxMinTextSize(float textSize) {
        mMaxMinPaint.setTextSize(textSize);
    }

    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * 设置背景颜色
     */
    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
    }

    /**
     * 设置选中point 值显示背景
     */
    public void setSelectedPointTextBackgroundColor(int color) {
        mSelectedPointPaint.setColor(color);
    }

    /**
     * 设置选择器横线宽度
     */
    public void setSelectedXLineWidth(float width) {
        mSelectedXLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置选择器横线颜色
     */
    public void setSelectedXLineColor(int color) {
        mSelectedXLinePaint.setColor(color);
    }

    /**
     * 设置选择器竖线宽度
     */
    public void setSelectedYLineWidth(float width) {
        mSelectedYLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置选择器竖线颜色
     */
    public void setSelectedYLineColor(int color) {
        mSelectedYLinePaint.setColor(color);
    }

    /**
     * 设置选择器文字大小
     * @param textSize
     */
    public void setSelectorWindowTextSize(float textSize){
        mSelectorWindowTextPaint.setTextSize(textSize);
    }

    /**
     * 设置选择器背景
     * @param color
     */
    public void setSelectorWindowBackgroundColor(int color) {
        mSelectorWindowBackgroundPaint.setColor(color);
    }

    /**
     * 设置蜡烛图宽度
     *
     * @param candleWidth
     */
    public void setCandleWidth(float candleWidth) {
        mCandleDraw.setCandleWidth(candleWidth);
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    public void setCandleLineWidth(float candleLineWidth) {
        mCandleDraw.setCandleLineWidth(candleLineWidth);
    }

    /**
     * 蜡烛是否空心
     */
    public void setCandleSolid(boolean candleSolid) {
        mCandleDraw.setCandleSolid(candleSolid);
    }

    public void setRSI1Color(int color) {
        mRSIDraw.setRSI1Color(color);
    }

    public void setRSI2Color(int color) {
        mRSIDraw.setRSI2Color(color);
    }

    public void setRSI3Color(int color) {
        mRSIDraw.setRSI3Color(color);
    }

    /**
     * 设置DIF颜色
     */
    public void setDIFColor(int color) {
        mMACDDraw.setDIFColor(color);
    }

    /**
     * 设置DEA颜色
     */
    public void setDEAColor(int color) {
        mMACDDraw.setDEAColor(color);
    }

    /**
     * 设置MACD颜色
     */
    public void setMACDColor(int color) {
        mMACDDraw.setMACDColor(color);
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    public void setMACDWidth(float MACDWidth) {
        mMACDDraw.setMACDWidth(MACDWidth);
    }

    /**
     * 设置up颜色
     */
    public void setUpColor(int color) {
        mBOLLDraw.setUpColor(color);
    }

    /**
     * 设置mb颜色
     *
     * @param color
     */
    public void setMbColor(int color) {
        mBOLLDraw.setMbColor(color);
    }

    /**
     * 设置dn颜色
     */
    public void setDnColor(int color) {
        mBOLLDraw.setDnColor(color);
    }

    /**
     * 设置K颜色
     */
    public void setKColor(int color) {
        mKDJDraw.setKColor(color);
    }

    /**
     * 设置D颜色
     */
    public void setDColor(int color) {
        mKDJDraw.setDColor(color);
    }

    /**
     * 设置J颜色
     */
    public void setJColor(int color) {
        mKDJDraw.setJColor(color);
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    public void setMa5Color(int color) {
        mCandleDraw.setMa5Color(color);
        mVolumeDraw.setMa5Color(color);
    }

    /**
     * 设置ma10颜色
     *
     * @param color
     */
    public void setMa10Color(int color) {
        mCandleDraw.setMa10Color(color);
        mVolumeDraw.setMa10Color(color);
    }

    /**
     * 设置ma20颜色
     *
     * @param color
     */
    public void setMa20Color(int color) {
        mCandleDraw.setMa20Color(color);
    }

}
