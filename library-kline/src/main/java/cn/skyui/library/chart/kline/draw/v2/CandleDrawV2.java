package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

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

    protected float mScaleX = 1;
    private float mMainScaleY = 1;

    private Rect mRect;
    private int mRectWidth;

    private float mCandleWidth = 0;
    private float mCandlePadding = 0;
    private float mCandleLineWidth = 0;

    private int mStartIndex = 0; // 可见区域数据List的开始索引位置
    private int mStopIndex = 0;  // 可见区域数据List的结束索引位置

    private float mMainMaxValue = Float.MAX_VALUE; // 可见区域数据的最大值，包括均线在内
    private float mMainMinValue = Float.MIN_VALUE; // 可见区域数据的最小值，包括均线在内

    private float mMainHighMaxValue = 0; // 最高价里最大的值
    private float mMainLowMinValue = 0;  // 最低价里最小的值

    private int mMainMaxIndex = 0; // 最高价的数据索引
    private int mMainMinIndex = 0; // 最低价的数据索引

    private Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma20Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private boolean mCandleSolid = true; // 实心蜡烛图

    public CandleDrawV2(Context context) {
        mContext = context;
        mGridPaint.setAntiAlias(true);
        mGridPaint.setColor(Color.GRAY);
        mGridPaint.setStyle(Paint.Style.STROKE);

        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red));
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green));

        mCandleWidth = (int) context.getResources().getDimension(R.dimen.chart_candle_width);
        mCandlePadding = (int) context.getResources().getDimension(R.dimen.chart_candle_padding);
        mCandleLineWidth = (int) context.getResources().getDimension(R.dimen.chart_candle_line_width);
    }

    public abstract KLine getItem(int position);

    public abstract int getCount();

    public void setRect(Rect rect) {
        mRect = rect;
        mRectWidth = mRect.width();
    }

    /**
     * 画表格
     *
     * @param canvas
     */
    public void drawGird(Canvas canvas) {
        float rowSpace = mRect.height() / GRID_ROWS;
        for (int i = 0; i <= GRID_ROWS; i++) {
            canvas.drawLine(0, rowSpace * i + mRect.top, mRect.width(), rowSpace * i + mRect.top, mGridPaint);
        }
    }

    /**
     * 计算当前显示区域，正在使用的数据块的start/end索引范围
     * 主要关注X轴的滑动，左侧为画布原点
     * 一屏显示多少根蜡烛图是固定的
     */
    public void calculateValue(int scrollX) {
        mMainMaxValue = Float.MIN_VALUE;
        mMainMinValue = Float.MAX_VALUE;

        // 屏幕内+屏幕外右侧画布区域的蜡烛图梳理
        float candleCount = (mRectWidth + scrollX) / (mCandleWidth + mCandlePadding);
        // 屏幕内的蜡烛图梳理
        float inRectCandleCount = mRectWidth / (mCandleWidth + mCandlePadding);
        // 屏幕外的蜡烛图梳理
        float scrollOutCount = scrollX / (mCandleWidth + mCandlePadding);
        mStartIndex = getCount() - (int) candleCount - 1;
        if (mStartIndex <= 0) {
            mStartIndex = 0;
        }
        mStopIndex = getCount() - (int) scrollOutCount - 1;
        if (mStopIndex <= inRectCandleCount) {
            mStopIndex = (int) (inRectCandleCount);
        }
        if(mStopIndex > getCount() - 1) {
            mStopIndex = getCount() - 1;
        }

        mMainMaxIndex = mStartIndex;
        mMainMinIndex = mStartIndex;
        mMainHighMaxValue = Float.MIN_VALUE;
        mMainLowMinValue = Float.MAX_VALUE;

        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine point = getItem(i);
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
        // 价格高度对应的坐标高度
        mMainScaleY = mRect.height() * 1f / (mMainMaxValue - mMainMinValue);
    }

    public void drawCandleChart(Canvas canvas, int scrollX) {
        canvas.save();
        canvas.scale(mScaleX, 1);
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine currentPoint = getItem(i);
            int scrollOutCount = getCount() - i;
            float currentPointX = mRectWidth - getX(scrollOutCount) + mCandlePadding / 2 + scrollX;
            KLine prevPoint = i == 0 ? currentPoint : getItem(i - 1);
            float prevX = i == 0 ? currentPointX : mRectWidth - getX(scrollOutCount + 1) + mCandlePadding / 2 + scrollX;
            drawCandleAndMaLine(prevPoint, currentPoint, prevX, currentPointX, canvas);
        }
        //还原 平移缩放
        canvas.restore();
    }

    private void drawCandleAndMaLine(@Nullable Candle prevPoint, @NonNull Candle currPoint, float prevX, float currX, @NonNull Canvas canvas) {
        drawCandleChart(canvas, currX, currPoint.high, currPoint.low, currPoint.open, currPoint.close);
        //画ma5
        if (prevPoint != null && prevPoint.ma5Price != 0) {
            drawLine(canvas, ma5Paint, prevX, prevPoint.ma5Price, currX, currPoint.ma5Price);
        }
        //画ma10
        if (prevPoint != null && prevPoint.ma10Price != 0) {
            drawLine(canvas, ma10Paint, prevX, prevPoint.ma10Price, currX, currPoint.ma10Price);
        }
        //画ma20
        if (prevPoint != null && prevPoint.ma20Price != 0) {
            drawLine(canvas, ma20Paint, prevX, prevPoint.ma20Price, currX, currPoint.ma20Price);
        }
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
    private void drawCandleChart(Canvas canvas, float x, float high, float low, float open, float close) {
        high = getY(high);
        low = getY(low);
        open = getY(open);
        close = getY(close);
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
     * 在主区域画线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    private void drawLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getY(startValue), stopX, getY(stopValue), paint);
    }

    private void drawText(@NonNull Canvas canvas, @NonNull IChartData chartData, float x, float y) {
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
     * 获取架构对应的Y坐标
     *
     * @param value 价格
     * @return Y坐标
     */
    private float getY(float value) {
        return (mMainMaxValue - value) * mMainScaleY + mRect.top;
    }

    /**
     * 根据索引索取X坐标
     *
     * @param position 索引值
     * @return X坐标
     */
    public float getX(int position) {
        return position * (mCandleWidth + mCandlePadding);
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

    public float getCandleWidth() {
        return mCandleWidth + mCandlePadding;
    }

    public float getCandlePadding() {
        return mCandlePadding;
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

    public int getStartIndex() {
        return mStartIndex;
    }

    public void setStartIndex(int mStartIndex) {
        this.mStartIndex = mStartIndex;
    }

    public int getStopIndex() {
        return mStopIndex;
    }

    public void setStopIndex(int mStopIndex) {
        this.mStopIndex = mStopIndex;
    }

}
