package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.data.model.Volume;

/**
 * Volume实现类
 */

public class VolumeDrawV2 extends BaseChartDraw {

    private float mCandleWidth;
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public VolumeDrawV2(Context context) {
        super(context, ChartEnum.VOL);
        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red));
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green));
        mCandleWidth = (int) context.getResources().getDimension(R.dimen.chart_candle_width);
    }

    @Override
    public void drawChartItem(@NonNull Canvas canvas, @Nullable KLine prevPoint, @NonNull KLine currPoint, float prevX, float currX) {
        drawVol(canvas, currPoint.vol, currX);
        float r = getChartItemWidth() / 2;
        currX+=r; prevX+=r;
        if (prevPoint.vol.ma5Volume != 0f) {
            drawLine(canvas, ma5Paint, prevX, prevPoint.vol.ma5Volume, currX, currPoint.vol.ma5Volume);;
        }
        if (prevPoint.vol.ma10Volume != 0f) {
            drawLine(canvas, ma10Paint, prevX, prevPoint.vol.ma10Volume, currX, currPoint.vol.ma10Volume);
        }
    }

    private void drawVol(Canvas canvas, Volume curPoint, float currX) {
        float top = getY(curPoint.volume);
        int bottom = mRect.bottom;
        // 加上间距的一半(左右间距各占一半)是X坐标起始的位置
        currX = currX + (getChartItemWidth() - getCandleWidth()) / 2;
        if (curPoint.closePrice >= curPoint.openPrice) {//涨
            canvas.drawRect(currX, top, currX + getCandleWidth(), bottom, mRedPaint);
        } else {
            canvas.drawRect(currX, top, currX + getCandleWidth(), bottom, mGreenPaint);
        }

    }

    public void drawText(@NonNull Canvas canvas, @NonNull KLine chartData, float x, float y) {
        Volume point = chartData.vol;
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

    public float getCandleWidth() {
        return mCandleWidth * mScaleX;
    }

    public void setCandleWidth(float chartWidth) {
        this.mCandleWidth = chartWidth;
    }
}
