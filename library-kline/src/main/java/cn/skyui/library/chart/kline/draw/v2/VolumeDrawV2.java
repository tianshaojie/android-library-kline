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
import cn.skyui.library.chart.kline.base.IChartDraw;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.data.model.Volume;
import cn.skyui.library.chart.kline.utils.ViewUtil;
import cn.skyui.library.chart.kline.view.BaseKLineChartView;

/**
 * Volume实现类
 */

public class VolumeDrawV2 implements IChartDraw<Volume> {

    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRedPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mGreenPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma5Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint ma10Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int pillarWidth = 0;

    private Rect mRect;
    private int mRectWidth;

    public VolumeDrawV2(Context context) {
        mRedPaint.setColor(ContextCompat.getColor(context, R.color.chart_red));
        mGreenPaint.setColor(ContextCompat.getColor(context, R.color.chart_green));
        pillarWidth = ViewUtil.dp2Px(context, 5);
    }

    public void setRect(Rect rect) {
        mRect = rect;
        mRectWidth = mRect.width();
    }

    @Override
    public void drawTranslated(@Nullable Volume lastPoint, @NonNull Volume curPoint, float lastX, float curX,
                               @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position) {

        drawHistogram(canvas, curPoint, lastPoint, curX, view, position);
        if (lastPoint.ma5Volume != 0f) {
            view.drawVolLine(canvas, ma5Paint, lastX, lastPoint.ma5Volume, curX, curPoint.ma5Volume);;
        }
        if (lastPoint.ma10Volume != 0f) {
            view.drawVolLine(canvas, ma10Paint, lastX, lastPoint.ma10Volume, curX, curPoint.ma10Volume);
        }
    }

    private void drawHistogram(Canvas canvas, Volume curPoint, Volume lastPoint, float curX,
                               BaseKLineChartView view, int position) {
        float r = pillarWidth / 2;
        float top = view.getVolY(curPoint.volume);
        int bottom = view.getVolRect().bottom;
        if (curPoint.closePrice >= curPoint.openPrice) {//涨
            canvas.drawRect(curX - r, top, curX + r, bottom, mRedPaint);
        } else {
            canvas.drawRect(curX - r, top, curX + r, bottom, mGreenPaint);
        }

    }

    @Override
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

    private Float mVolMaxValue = Float.MAX_VALUE;
    private Float mVolMinValue = Float.MIN_VALUE;
    private float mVolScaleY = 1;

    private void calculateValue() {

    }


    public float getVolY(float value) {
        return (mVolMaxValue - value) * mVolScaleY + mRect.top;
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
