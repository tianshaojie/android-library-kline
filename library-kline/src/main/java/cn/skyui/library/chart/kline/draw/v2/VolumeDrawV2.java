package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.data.model.Volume;

/**
 * Volume实现类
 */

public abstract class VolumeDrawV2 {

    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float mCandleWidth = 0;
    private float mCandlePadding = 0;

    private Rect mRect;
    private int mRectWidth;

    public VolumeDrawV2(Context context) {
        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red));
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green));
        mCandleWidth = (int) context.getResources().getDimension(R.dimen.chart_candle_width);
        mCandlePadding = (int) context.getResources().getDimension(R.dimen.chart_candle_padding);
    }

    public void setRect(Rect rect) {
        mRect = rect;
        mRectWidth = mRect.width();
    }

    public abstract KLine getItem(int position);

    public abstract int getCount();

    private Float mVolMaxValue = Float.MIN_VALUE;
    private Float mVolMinValue = Float.MAX_VALUE;
    private float mVolScaleY = 1;

    public void calculateValue(int mStartIndex, int mStopIndex) {
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine point = (KLine) getItem(i);
            mVolMaxValue = Math.max(mVolMaxValue, point.vol.getMaxValue());
            mVolMinValue = Math.min(mVolMinValue, point.vol.getMinValue());
        }
        if (Math.abs(mVolMaxValue) < 0.01) {
            mVolMaxValue = 15.00f;
        }
        mVolScaleY = mRect.height() * 1f / (mVolMaxValue - mVolMinValue);
    }


    public void drawChart(Canvas canvas, int scrollX, int mStartIndex, int mStopIndex) {
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine currentPoint = getItem(i);
            int scrollOutCount = getCount() - i;
            float currentPointX = mRectWidth - getX(scrollOutCount) + mCandlePadding / 2 + scrollX;
            KLine lastPoint = i == 0 ? currentPoint : getItem(i - 1);
            float prevX = i == 0 ? currentPointX : mRectWidth - getX(scrollOutCount + 1) + mCandlePadding / 2 + scrollX;
            drawVolChart(lastPoint.vol, currentPoint.vol, prevX, currentPointX, canvas, i);
        }
    }

    public float getX(int position) {
        return position * (mCandleWidth + mCandlePadding);
    }

    public float getVolY(float value) {
        return (mVolMaxValue - value) * mVolScaleY + mRect.top;
    }

    private void drawVolChart(@Nullable Volume lastPoint, @NonNull Volume curPoint, float lastX, float curX,
                             @NonNull Canvas canvas, int position) {

        drawHistogram(canvas, curPoint, lastPoint, curX, position);
        if (lastPoint.ma5Volume != 0f) {
            drawVolLine(canvas, ma5Paint, lastX, lastPoint.ma5Volume, curX, curPoint.ma5Volume);;
        }
        if (lastPoint.ma10Volume != 0f) {
            drawVolLine(canvas, ma10Paint, lastX, lastPoint.ma10Volume, curX, curPoint.ma10Volume);
        }
    }

    private void drawHistogram(Canvas canvas, Volume curPoint, Volume lastPoint, float curX, int position) {
        float r = mCandleWidth / 2;
        float top = getVolY(curPoint.volume);
        int bottom = mRect.bottom;
        if (curPoint.closePrice >= curPoint.openPrice) {//涨
            canvas.drawRect(curX - r, top, curX + r, bottom, mRedPaint);
        } else {
            canvas.drawRect(curX - r, top, curX + r, bottom, mGreenPaint);
        }

    }

    public void drawText(@NonNull Canvas canvas, @NonNull IChartData chartData, float x, float y) {
        Volume point = (Volume) chartData;
        String text = "VOL:" + KLine.getValueFormatter(ChartEnum.VOL.name()).format(point.volume) + "  ";
        canvas.drawText(text, x, y, mTextPaint);
        x += mTextPaint.measureText(text);
        text = "MA5:" + KLine.getValueFormatter(ChartEnum.VOL.name()).format(point.ma5Volume) + "  ";
        canvas.drawText(text, x, y, ma5Paint);
        x += ma5Paint.measureText(text);
        text = "MA10:" + KLine.getValueFormatter(ChartEnum.VOL.name()).format(point.ma10Volume) + "  ";
        canvas.drawText(text, x, y, ma10Paint);
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawVolLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getVolY(startValue), stopX, getVolY(stopValue), paint);
    }

    /**
     * 设置 MA5 线的颜色
     *
     * @param color
     */
    public void setTextColor(int color) {
        this.mTextPaint.setColor(color);
    }


    /**
     * 设置 MA5 线的颜色
     *
     * @param color
     */
    public void setMa5Color(int color) {
        this.ma5Paint.setColor(color);
    }

    /**
     * 设置 MA10 线的颜色
     *
     * @param color
     */
    public void setMa10Color(int color) {
        this.ma10Paint.setColor(color);
    }

    public void setLineWidth(float width) {
        this.ma5Paint.setStrokeWidth(width);
        this.ma10Paint.setStrokeWidth(width);
    }

    /**
     * 设置文字大小
     *
     * @param textSize
     */
    public void setTextSize(float textSize) {
        this.ma5Paint.setTextSize(textSize);
        this.ma10Paint.setTextSize(textSize);
        this.mTextPaint.setTextSize(textSize);
    }

}
