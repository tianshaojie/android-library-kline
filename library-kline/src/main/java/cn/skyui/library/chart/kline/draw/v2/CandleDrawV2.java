package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.List;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.Candle;
import cn.skyui.library.chart.kline.data.model.KLine;

/**
 * 主图的实现类
 */

public class CandleDrawV2 extends BaseChartDraw {

    private static final int GRID_ROWS = 4;

    protected float mCandleWidth;
    private float mCandleLineWidth = 0;
    private boolean mCandleSolid = true; // 实心蜡烛图

    private float mMaxPrice = 0; // 最高价里最大的值
    private float mMinPrice = 0;  // 最低价里最小的值
    private int mMaxPriceIndex = 0; // 最高价的数据索引
    private int mMinPriceIndex = 0; // 最低价的数据索引

    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma20Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public CandleDrawV2(Context context) {
        super(context, ChartEnum.CANDLE);
        mContext = context;
        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red));
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green));
        mCandleWidth = (int) context.getResources().getDimension(R.dimen.chart_candle_width);
        mCandleLineWidth = (int) context.getResources().getDimension(R.dimen.chart_candle_line_width);
    }

    /**
     * 计算当前显示区域，正在使用的数据块的start/end索引范围
     * 主要关注X轴的滑动，左侧为画布原点
     * 一屏显示多少根蜡烛图是固定的
     */
    @Override
    public void calculateValue(List<KLine> dataList, int mStartIndex, int mStopIndex) {
        super.calculateValue(dataList, mStartIndex, mStopIndex);
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine point = dataList.get(i);
            if (mMaxPrice != Math.max(mMaxPrice, point.high)) {
                mMaxPrice = point.high;
                mMaxPriceIndex = i;
            }
            if (mMinPrice != Math.min(mMinPrice, point.low)) {
                mMinPrice = point.low;
                mMinPriceIndex = i;
            }
        }
        if (mMaxValue != mMinValue) {
            float padding = (mMaxValue - mMinValue) * 0.05f;
            mMaxValue += padding;
            mMinValue -= padding;
        } else {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mMaxValue += Math.abs(mMaxValue * 0.05f);
            mMinValue -= Math.abs(mMinValue * 0.05f);
            if (mMaxValue == 0) {
                mMaxValue = 1.0f;
            }
        }
        mScaleY = (mRect.height() - mTopPadding) * 1f / (mMaxValue - mMinValue);
    }

    @Override
    public void onDraw(Canvas canvas, int scrollX) {
        drawGird(canvas);
        super.onDraw(canvas, scrollX);
    }

    /**
     * 画表格
     *
     * @param canvas
     */
    private void drawGird(Canvas canvas) {
        float paintWidth = mGridPaint.getStrokeWidth()/2;
        canvas.drawLine(0, paintWidth, mRect.width(), paintWidth, mGridPaint);
        float rowSpace = (mRect.height() - mTopPadding) / GRID_ROWS;
        for (int i = 0; i < GRID_ROWS; i++) {
            canvas.drawLine(0, rowSpace * i + mRect.top + mTopPadding, mRect.width(), rowSpace * i + mRect.top + mTopPadding, mGridPaint);
        }
    }

    @Override
    public void drawChart(@NonNull Canvas canvas, @Nullable KLine prevPoint, @NonNull KLine currPoint, float prevX, float currX) {
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
        // 加上间距的一半(左右间距各占一半)是X坐标起始的位置
        x = x + (mChartItemWidth - mCandleWidth) / 2;
        if (open > close) {
            if (mCandleSolid) {
                //实心
                canvas.drawRect(x, close, x + mCandleWidth, open, mRedPaint);
                canvas.drawRect(x + r - lineR, high, x + r + lineR, low, mRedPaint);
            } else {
                mRedPaint.setStrokeWidth(mCandleLineWidth);
                // 中线上下竖线
                canvas.drawLine(x + r, high, x + r, close, mRedPaint);
                canvas.drawLine(x + r, open, x + r, low, mRedPaint);
                // 矩形左右竖线
                canvas.drawLine(x, open, x, close, mRedPaint);
                canvas.drawLine(x + mCandleWidth, open, x + mCandleWidth, close, mRedPaint);
                mRedPaint.setStrokeWidth(mCandleLineWidth * getScaleX());
                // 矩形上下横线
                canvas.drawLine(x, open, x + mCandleWidth, open, mRedPaint);
                canvas.drawLine(x, close, x + mCandleWidth, close, mRedPaint);
            }
        } else if (open < close) {
            canvas.drawRect(x, open, x + mCandleWidth, close, mGreenPaint);
            canvas.drawRect(x + r - lineR, high, x + r + lineR, low, mGreenPaint);
        } else {
            canvas.drawRect(x, open, x + mCandleWidth, close + 1, mRedPaint);
            canvas.drawRect(x + r - lineR, high, x + r + lineR, low, mRedPaint);
        }
    }

    public void drawText(@NonNull Canvas canvas, @NonNull KLine chartData, float x, float y) {
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

    public float getCandleWidth() {
        return mCandleWidth;
    }

    public void setCandleWidth(float chartWidth) {
        this.mCandleWidth = chartWidth;
    }
}
