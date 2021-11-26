package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.Candle;
import cn.skyui.library.chart.kline.data.model.KLine;

/**
 * 主图的实现类
 */

public abstract class CandleDrawV2 {

    private static final int GRID_ROWS = 4;

    private Context mContext;

    private int mTopPadding;
    private int mBottomPadding;

    protected float mScaleX = 1;
    private float mMainScaleY = 1;

    private Rect mRect;
    private int mWidth;

    private float mMainMaxValue = Float.MAX_VALUE;
    private float mMainMinValue = Float.MIN_VALUE;
    private float mMainHighMaxValue = 0;
    private float mMainLowMinValue = 0;
    private int mMainMaxIndex = 0;
    private int mMainMinIndex = 0;

    private float mCandleWidth = 0;
    private float mCandleLineWidth = 0;

    private Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma20Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean mCandleSolid = true;

    public CandleDrawV2(Context context, Rect rect) {
        mContext = context;
        mRect = rect;
        mWidth = mRect.width();
        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red));
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green));
        mTopPadding = (int) context.getResources().getDimension(R.dimen.chart_top_padding);
        mBottomPadding = (int) context.getResources().getDimension(R.dimen.chart_bottom_padding);
    }

    /**
     * 画表格
     *
     * @param canvas
     */
    public void drawGird(Canvas canvas) {
        //-----------------------TopPadding横线------------------------
        canvas.drawLine(0, mRect.top, mRect.width(), mRect.top, mGridPaint);
        //-----------------------CandleGrid横线--------------------
        float rowSpace = (mRect.height() - mTopPadding - mBottomPadding) / GRID_ROWS;
        for (int i = 0; i <= GRID_ROWS; i++) {
            canvas.drawLine(0, rowSpace * i + mRect.top + mTopPadding, mRect.width(), rowSpace * i + mRect.top + mTopPadding, mGridPaint);
        }
        //-----------------------两侧竖线--------------------
        canvas.drawLine(0, 0, 0, mRect.height(), mGridPaint);
        canvas.drawLine(mRect.width(), 0, mRect.width(), mRect.height(), mGridPaint);
        //-----------------------BottomPadding横线------------------------
        canvas.drawLine(0, mRect.height(), mRect.width(), mRect.height(), mGridPaint);
    }

    private int mStartIndex = 0;
    private int mStopIndex = 0;

    /**
     * 计算当前的显示区域
     */
    public void calculateValue() {
        mMainMaxValue = Float.MIN_VALUE;
        mMainMinValue = Float.MAX_VALUE;
        mStartIndex = indexOfTranslateX(xToTranslateX(0));
        mStopIndex = indexOfTranslateX(xToTranslateX(mWidth));

        mMainMaxIndex = mStartIndex;
        mMainMinIndex = mStartIndex;
        mMainHighMaxValue = Float.MIN_VALUE;
        mMainLowMinValue = Float.MAX_VALUE;


        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine point = (KLine) getItem(i);
            mMainMaxValue = Math.max(mMainMaxValue, point.getMaxValue());
            mMainMinValue = Math.min(mMainMinValue, point.getMinValue());
            if (mMainHighMaxValue != Math.max(mMainHighMaxValue, point.high)) {
                mMainHighMaxValue = point.high;
                mMainMaxIndex = i;
            }
            if (mMainLowMinValue != Math.min(mMainLowMinValue, point.low)) {
                mMainLowMinValue = point.low;
                mMainMinIndex = i;
            }
        }
        if (mMainMaxValue != mMainMinValue) {
            float padding = (mMainMaxValue - mMainMinValue) * 0.05f;
            mMainMaxValue += padding;
            mMainMinValue -= padding;
        } else {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mMainMaxValue += Math.abs(mMainMaxValue * 0.05f);
            mMainMinValue -= Math.abs(mMainMinValue * 0.05f);
            if (mMainMaxValue == 0) {
                mMainMaxValue = 1;
            }
        }

        mMainScaleY = mRect.height() * 1f / (mMainMaxValue - mMainMinValue);
    }

    public abstract KLine getItem(int position);

    public abstract int getCount();

    public int indexOfTranslateX(float translateX) {
        return indexOfTranslateX(translateX, 0, getCount() - 1);
    }

    /**
     * 二分查找当前值的index
     *
     * @return
     */
    public int indexOfTranslateX(float translateX, int start, int end) {
        if (end == start) {
            return start;
        }
        if (end - start == 1) {
            float startValue = getX(start);
            float endValue = getX(end);
            return Math.abs(translateX - startValue) < Math.abs(translateX - endValue) ? start : end;
        }
        int mid = start + (end - start) / 2;
        float midValue = getX(mid);
        if (translateX < midValue) {
            return indexOfTranslateX(translateX, start, mid);
        } else if (translateX > midValue) {
            return indexOfTranslateX(translateX, mid, end);
        } else {
            return mid;
        }
    }

    /**
     * 根据索引索取x坐标
     *
     * @param position 索引值
     * @return
     */
    public float getX(int position) {
        return position * mCandleWidth;
    }

    private float mTranslateX = Float.MIN_VALUE;

    public void setDataLen(float mDataLen) {
        this.mDataLen = mDataLen;
    }

    // 数据长度
    private float mDataLen = 0;

    /**
     * scrollX 转换为 TranslateX
     *
     * @param scrollX
     */
    private void setTranslateXFromScrollX(int scrollX) {
        mTranslateX = scrollX + getMinTranslateX();
    }

    /**
     * 获取平移的最小值
     *
     * @return
     */
    private float getMinTranslateX() {
        return -mDataLen + mWidth / mScaleX - mCandleWidth / 2;
    }

    /**
     * view中的x转化为TranslateX
     *
     * @param x
     * @return
     */
    public float xToTranslateX(float x) {
        return -mTranslateX + x / mScaleX;
    }

    public void onDraw(@Nullable Candle lastPoint, @NonNull Candle curPoint, float lastX, float curX, @NonNull Canvas canvas) {
        drawCandle(canvas, curX, curPoint.high, curPoint.low, curPoint.open, curPoint.close);
        //画ma5
        if (lastPoint.ma5Price != 0) {
            drawMainLine(canvas, ma5Paint, lastX, lastPoint.ma5Price, curX, curPoint.ma5Price);
        }
        //画ma10
        if (lastPoint.ma10Price != 0) {
            drawMainLine(canvas, ma10Paint, lastX, lastPoint.ma10Price, curX, curPoint.ma10Price);
        }
        //画ma20
        if (lastPoint.ma20Price != 0) {
            drawMainLine(canvas, ma20Paint, lastX, lastPoint.ma20Price, curX, curPoint.ma20Price);
        }
    }

    /**
     * 在主区域画线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    public void drawMainLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getMainY(startValue), stopX, getMainY(stopValue), paint);
    }

    public float getMainY(float value) {
        return (mMainMaxValue - value) * mMainScaleY + mRect.top;
    }


    public void drawText(@NonNull Canvas canvas, @NonNull IChartData chartData, float x, float y) {
        Candle point = (Candle) chartData;
        String text = "MA5:" + KLine.getValueFormatter(ChartEnum.CANDLE.name()).format(point.ma5Price) + "  ";
        canvas.drawText(text, x, y, ma5Paint);
        x += ma5Paint.measureText(text);
        text = "MA10:" + KLine.getValueFormatter(ChartEnum.CANDLE.name()).format(point.ma10Price) + "  ";
        canvas.drawText(text, x, y, ma10Paint);
        x += ma10Paint.measureText(text);
        text = "MA20:" + KLine.getValueFormatter(ChartEnum.CANDLE.name()).format(point.ma20Price) + "  ";
        canvas.drawText(text, x, y, ma20Paint);
    }

    /**
     * 画Candle
     *
     * @param canvas
     * @param x      x轴坐标
     * @param high   最高价
     * @param low    最低价
     * @param open   开盘价
     * @param close  收盘价
     */
    private void drawCandle(Canvas canvas, float x, float high, float low, float open, float close) {
        high = getMainY(high);
        low = getMainY(low);
        open = getMainY(open);
        close = getMainY(close);
        float r = mCandleWidth / 2;
        float lineR = mCandleLineWidth / 2;
        if (open > close) {
            //实心
            if (mCandleSolid) {
                canvas.drawRect(x - r, close, x + r, open, mRedPaint);
                canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint);
            } else {
                mRedPaint.setStrokeWidth(mCandleLineWidth);
                canvas.drawLine(x, high, x, close, mRedPaint);
                canvas.drawLine(x, open, x, low, mRedPaint);
                canvas.drawLine(x - r + lineR, open, x - r + lineR, close, mRedPaint);
                canvas.drawLine(x + r - lineR, open, x + r - lineR, close, mRedPaint);
                mRedPaint.setStrokeWidth(mCandleLineWidth * getScaleX());
                canvas.drawLine(x - r, open, x + r, open, mRedPaint);
                canvas.drawLine(x - r, close, x + r, close, mRedPaint);
            }

        } else if (open < close) {
            canvas.drawRect(x - r, open, x + r, close, mGreenPaint);
            canvas.drawRect(x - lineR, high, x + lineR, low, mGreenPaint);
        } else {
            canvas.drawRect(x - r, open, x + r, close + 1, mRedPaint);
            canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint);
        }
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    public void setCandleWidth(float candleWidth) {
        mCandleWidth = candleWidth;
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    public void setCandleLineWidth(float candleLineWidth) {
        mCandleLineWidth = candleLineWidth;
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    public void setMa5Color(int color) {
        this.ma5Paint.setColor(color);
    }

    /**
     * 设置ma10颜色
     *
     * @param color
     */
    public void setMa10Color(int color) {
        this.ma10Paint.setColor(color);
    }

    /**
     * 设置ma20颜色
     *
     * @param color
     */
    public void setMa20Color(int color) {
        this.ma20Paint.setColor(color);
    }

    /**
     * 设置曲线宽度
     */
    public void setLineWidth(float width) {
        ma20Paint.setStrokeWidth(width);
        ma10Paint.setStrokeWidth(width);
        ma5Paint.setStrokeWidth(width);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        ma20Paint.setTextSize(textSize);
        ma10Paint.setTextSize(textSize);
        ma5Paint.setTextSize(textSize);
    }

    /**
     * 蜡烛是否实心
     */
    public void setCandleSolid(boolean candleSolid) {
        mCandleSolid = candleSolid;
    }

    public float getScaleX() {
        return mScaleX;
    }

    public void setScaleX(float mScaleX) {
        this.mScaleX = mScaleX;
    }

}
