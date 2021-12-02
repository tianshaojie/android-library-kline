package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
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
import cn.skyui.library.chart.kline.view.BaseKLineChartView;
import cn.skyui.library.chart.kline.view.KLineViewV2;

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

    public CandleDrawV2(Context context, Rect rect) {
        mContext = context;
        mRect = rect;
        mGridPaint.setAntiAlias(true);
        mGridPaint.setColor(Color.GRAY);
        mGridPaint.setStyle(Paint.Style.STROKE);

        mRectWidth = mRect.width();
        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red));
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green));

        mCandleWidth = (int) context.getResources().getDimension(R.dimen.chart_candle_width);
        mCandlePadding = (int) context.getResources().getDimension(R.dimen.chart_candle_padding);
        mCandleLineWidth = (int) context.getResources().getDimension(R.dimen.chart_candle_line_width);
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
     * 计算当前的显示区域
     */
    public void calculateValue(int scrollX) {
        mMainMaxValue = Float.MIN_VALUE;
        mMainMinValue = Float.MAX_VALUE;

        // 一屏显示多少根蜡烛图是固定的
        float candleCount = (mRectWidth + scrollX) / (mCandleWidth + mCandlePadding); // 屏幕内+屏幕外右侧画布区域的蜡烛图梳理
        float inRectCandleCount = mRectWidth / (mCandleWidth + mCandlePadding); // 屏幕内的蜡烛图梳理
        float scrollOutCount = scrollX /  (mCandleWidth + mCandlePadding); // 屏幕外的蜡烛图梳理
        mStartIndex = getCount() - (int) candleCount;
        if(mStartIndex <= 0) {
            mStartIndex = 0;
        }
        mStopIndex = getCount() - (int) scrollOutCount - 1;
        if(mStopIndex <= inRectCandleCount) {
            mStopIndex = (int) (inRectCandleCount);
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

    public void drawCandle(Canvas canvas, int scrollX) {
        canvas.save();
        canvas.scale(mScaleX, 1);
        int index = 1;
        for (int i = mStopIndex; i >= mStartIndex; i--) {
            KLine currentPoint = getItem(i);
            float currentPointX = mRectWidth - getX(index) + mCandlePadding + scrollX;
            KLine prevPoint = i == 0 ? currentPoint : getItem(i - 1);
            float prevX = i == 0 ? currentPointX : mRectWidth - getX(index + 1) + mCandlePadding + scrollX;
            onDraw(prevPoint, currentPoint, prevX, currentPointX, canvas);
            index++;
        }
        //还原 平移缩放
        canvas.restore();
    }

    public void onDraw(@Nullable Candle prevPoint, @NonNull Candle currPoint, float prevX, float currX, @NonNull Canvas canvas) {
        drawCandle(canvas, currX, currPoint.high, currPoint.low, currPoint.open, currPoint.close);
        //画ma5
        if (prevPoint.ma5Price != 0) {
            drawMainLine(canvas, ma5Paint, prevX, prevPoint.ma5Price, currX, currPoint.ma5Price);
        }
        //画ma10
        if (prevPoint.ma10Price != 0) {
            drawMainLine(canvas, ma10Paint, prevX, prevPoint.ma10Price, currX, currPoint.ma10Price);
        }
        //画ma20
        if (prevPoint.ma20Price != 0) {
            drawMainLine(canvas, ma20Paint, prevX, prevPoint.ma20Price, currX, currPoint.ma20Price);
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

    public float getMainY(float value) {
        return (mMainMaxValue - value) * mMainScaleY + mRect.top;
    }

    public abstract KLine getItem(int position);

    public abstract int getCount();

    /**
     * 根据索引索取x坐标
     *
     * @param position 索引值
     * @return
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
