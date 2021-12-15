package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

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

    private float mCandleWidth;
    private float mCandleLineWidth = 0;
    private boolean mCandleSolid = true; // 实心蜡烛图

    private float mMaxPrice = 0; // 最高价里最大的值
    private float mMinPrice = 0;  // 最低价里最小的值
    private int mMaxPriceIndex = 0; // 最高价的数据索引
    private int mMinPriceIndex = 0; // 最低价的数据索引

    private Paint mMaxMinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
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

    @Override
    protected void calculateBegin() {
        mMaxPriceIndex = mStartIndex;
        mMinPriceIndex = mStartIndex;
        mMaxPrice = Float.MIN_VALUE;
        mMinPrice = Float.MAX_VALUE;
    }

    @Override
    protected void calculateItem(KLine point, int index) {
        if (mMaxPrice != Math.max(mMaxPrice, point.high)) {
            mMaxPrice = point.high;
            mMaxPriceIndex = index;
        }
        if (mMinPrice != Math.min(mMinPrice, point.low)) {
            mMinPrice = point.low;
            mMinPriceIndex = index;
        }
    }

    @Override
    protected void calculateEnd() {
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
        mScaleY = (mRect.height() - mTopPadding) * 1 / (mMaxValue - mMinValue);
    }

    /**
     * 画表格
     *
     * @param canvas
     */
    public void drawGird(Canvas canvas) {
        float rowSpace = (mRect.height() - mTopPadding) / GRID_ROWS;
        for (int i = 0; i <= GRID_ROWS; i++) {
            canvas.drawLine(0, rowSpace * i + mRect.top + mTopPadding, mRect.width(), rowSpace * i + mRect.top + mTopPadding, mGridPaint);
        }
    }

    @Override
    public void drawChartItem(@NonNull Canvas canvas, @Nullable KLine prevPoint, @NonNull KLine currPoint, float prevX, float currX) {
        drawCandleChart(canvas, currX, currPoint.high, currPoint.low, currPoint.open, currPoint.close);
        float r = mChartItemWidth / 2;
        currX+=r; prevX+=r;
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
            if (mCandleSolid) {
                //实心
                canvas.drawRect(x   -r, close, x + r, open, mRedPaint);
                canvas.drawRect(x - lineR, high, x + lineR, low, mRedPaint);
            } else {
                mRedPaint.setStrokeWidth(mCandleLineWidth);
                // 中线上下竖线
                canvas.drawLine(x, high, x, close, mRedPaint);
                canvas.drawLine(x, open, x, low, mRedPaint);
                // 矩形左右竖线
                canvas.drawLine(x - r + lineR, open, x - r + lineR, close, mRedPaint);
                canvas.drawLine(x + r - lineR, open, x + r - lineR, close, mRedPaint);
                mRedPaint.setStrokeWidth(mCandleLineWidth * getScaleX());
                // 矩形上下横线
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

    public void drawTitle(@NonNull Canvas canvas, @NonNull KLine point) {
        float x = 0, y = mRect.top + mTextBaseline;
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
     * 绘制坐标值
     * @param canvas
     */
    public void drawValue(@NonNull Canvas canvas) {
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        canvas.drawText(formatValue(mMaxValue), 0, baseLine + mRect.top + mTopPadding, mTextPaint);
        canvas.drawText(formatValue(mMinValue), 0, mRect.bottom - textHeight + baseLine, mTextPaint);
        float rowValue = (mMaxValue - mMinValue) / GRID_ROWS;
        float rowSpace = (mRect.height()-mTopPadding) / GRID_ROWS;
        for (int i = 1; i < GRID_ROWS; i++) {
            String text = formatValue(rowValue * (GRID_ROWS - i) + mMinValue);
            canvas.drawText(text, mRect.left, fixTextY(rowSpace * i + mRect.top + mTopPadding), mTextPaint);
        }
    }

    /**
     * 绘制最大值最小值
     * @param canvas
     * @param mTranslateX
     */
    public void drawMaxMin(Canvas canvas, float mTranslateX) {
        //绘制最大值和最小值（画布不在偏移了）
        float x = translateXtoX(getX(mMinPriceIndex), mTranslateX);
        float y = getY(mMinPrice);
        String LowString = "── " + mMinPrice;
        //计算显示位置
        //计算文本宽度
        int lowStringWidth = calculateMaxMin(LowString).width();
        int lowStringHeight = calculateMaxMin(LowString).height();
        if (x < mRectWidth / 2f) {
            //画右边
            canvas.drawText(LowString, x, y + lowStringHeight / 2f, mMaxMinPaint);
        } else {
            //画左边
            LowString = mMinPrice + " ──";
            canvas.drawText(LowString, x - lowStringWidth, y + lowStringHeight / 2f, mMaxMinPaint);
        }

        x = translateXtoX(getX(mMaxPriceIndex), mTranslateX);
        y = getY(mMaxPrice);

        String highString = "── " + mMaxPrice;
        int highStringWidth = calculateMaxMin(highString).width();
        int highStringHeight = calculateMaxMin(highString).height();
        if (x < mRectWidth / 2f) {
            //画右边
            canvas.drawText(highString, x, y + highStringHeight / 2f, mMaxMinPaint);
        } else {
            //画左边
            highString = mMaxPrice + " ──";
            canvas.drawText(highString, x - highStringWidth, y + highStringHeight / 2f, mMaxMinPaint);
        }
    }

    /**
     * translateX转化为view中的x
     *
     * @param translateX
     * @return
     */
    public float translateXtoX(float x, float translateX) {
        return (x + translateX) * mScaleX;
    }


    /**
     * 计算文本长度
     *
     * @return
     */
    private Rect calculateMaxMin(String text) {
        Rect rect = new Rect();
        mMaxMinPaint.getTextBounds(text, 0, text.length(), rect);
        return rect;
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
        super.setTextSize(textSize);
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
