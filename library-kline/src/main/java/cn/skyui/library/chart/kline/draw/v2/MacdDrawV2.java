package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.base.IValueFormatter;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.data.model.Macd;

/**
 * macd实现类
 */

public class MacdDrawV2 extends BaseChartDraw {

    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDIFPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDEAPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mMACDPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    /**
     * macd 中柱子的宽度
     */
    private IValueFormatter formatter;

    public MacdDrawV2(Context context) {
        super(context, ChartEnum.MACD);
        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red));
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green));
        formatter = KLine.getValueFormatter(ChartEnum.MACD.name());
    }

    @Override
    public void drawChart(Canvas canvas, int scrollX, int mStartIndex, int mStopIndex) {
        if(mDateList == null || mDateList.size() == 0) {
            return;
        }
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine currentPoint = mDateList.get(i);
            int scrollOutCount = mDateList.size() - i;
            float currentPointX = mRectWidth - getX(scrollOutCount) + mChartPadding / 2 + scrollX;
            KLine lastPoint = i == 0 ? currentPoint : mDateList.get(i - 1);
            float prevX = i == 0 ? currentPointX : mRectWidth - getX(scrollOutCount + 1) + mChartPadding / 2 + scrollX;
            drawChart(canvas, lastPoint.macd, currentPoint.macd, prevX, currentPointX);
        }
    }

    private void drawChart(@NonNull Canvas canvas, @Nullable Macd lastPoint, @NonNull Macd curPoint, float lastX, float curX) {
        drawMACD(canvas, curX, curPoint.macd);
        drawLine(canvas, mDIFPaint, lastX, lastPoint.dea, curX, curPoint.dea);
        drawLine(canvas, mDEAPaint, lastX, lastPoint.dif, curX, curPoint.dif);
    }

    /**
     * 画macd
     *
     * @param canvas
     * @param x
     * @param macd
     */
    private void drawMACD(Canvas canvas, float x, float macd) {
        float macdy = getY(macd);
        float r = mChartWidth / 2;
        float zeroy = getY(0);
        if (macd > 0) {
            canvas.drawRect(x - r, macdy, x + r, zeroy, mRedPaint);
        } else {
            canvas.drawRect(x - r, zeroy, x + r, macdy, mGreenPaint);
        }
    }

    private void drawText(@NonNull Canvas canvas, @NonNull IChartData chartData, float x, float y) {
        Macd point = (Macd) chartData;
        String text = "MACD(12,26,9)  ";
        canvas.drawText(text, x, y, mTextPaint);
        x += mTextPaint.measureText(text);
        text = "MACD:" + formatter.format(point.macd) + "  ";
        canvas.drawText(text, x, y, mMACDPaint);
        x += mMACDPaint.measureText(text);
        text = "DIF:" + formatter.format(point.dif) + "  ";
        canvas.drawText(text, x, y, mDEAPaint);
        x += mDIFPaint.measureText(text);
        text = "DEA:" + formatter.format(point.dea);
        canvas.drawText(text, x, y, mDIFPaint);
    }

    /**
     * 设置DIF颜色
     */
    public void setDIFColor(int color) {
        this.mDIFPaint.setColor(color);
    }

    /**
     * 设置DEA颜色
     */
    public void setDEAColor(int color) {
        this.mDEAPaint.setColor(color);
    }

    /**
     * 设置MACD颜色
     */
    public void setMACDColor(int color) {
        this.mMACDPaint.setColor(color);
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    public void setMACDWidth(float MACDWidth) {
        mChartWidth = MACDWidth;
    }

    /**
     * 设置曲线宽度
     */
    public void setLineWidth(float width) {
        mDEAPaint.setStrokeWidth(width);
        mDIFPaint.setStrokeWidth(width);
        mMACDPaint.setStrokeWidth(width);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        mDEAPaint.setTextSize(textSize);
        mDIFPaint.setTextSize(textSize);
        mMACDPaint.setTextSize(textSize);
        mTextPaint.setTextSize(textSize);
    }

    public void setTextColor(int color) {
        mTextPaint.setColor(color);
    }
}
