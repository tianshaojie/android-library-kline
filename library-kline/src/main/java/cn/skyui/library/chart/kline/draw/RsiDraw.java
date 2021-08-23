package cn.skyui.library.chart.kline.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import cn.skyui.library.chart.kline.view.BaseKLineChartView;
import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.base.IChartDraw;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.data.model.Rsi;

/**
 * RSI实现类
 */

public class RsiDraw implements IChartDraw<Rsi> {

    private Paint mRSI1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRSI2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRSI3Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RsiDraw(BaseKLineChartView view) {

    }

    @Override
    public void drawTranslated(@Nullable Rsi lastPoint, @NonNull Rsi curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position) {
        view.drawChildLine(canvas, mRSI1Paint, lastX, lastPoint.rsi1, curX, curPoint.rsi1);
        view.drawChildLine(canvas, mRSI2Paint, lastX, lastPoint.rsi2, curX, curPoint.rsi2);
        view.drawChildLine(canvas, mRSI3Paint, lastX, lastPoint.rsi3, curX, curPoint.rsi3);
    }

    @Override
    public void drawText(@NonNull Canvas canvas, @NonNull IChartData chartData, float x, float y) {
        Rsi point = (Rsi) chartData;
        String text = "";
        text = "RSI1:" + KLine.getValueFormatter(ChartEnum.RSI.name()).format(point.rsi1) + "  ";
        canvas.drawText(text, x, y, mRSI1Paint);
        x += mRSI1Paint.measureText(text);
        text = "RSI2:" + KLine.getValueFormatter(ChartEnum.RSI.name()).format(point.rsi2) + "  ";
        canvas.drawText(text, x, y, mRSI2Paint);
        x += mRSI2Paint.measureText(text);
        text = "RSI3:" + KLine.getValueFormatter(ChartEnum.RSI.name()).format(point.rsi3) + "  ";
        canvas.drawText(text, x, y, mRSI3Paint);
    }

    public void setRSI1Color(int color) {
        mRSI1Paint.setColor(color);
    }

    public void setRSI2Color(int color) {
        mRSI2Paint.setColor(color);
    }

    public void setRSI3Color(int color) {
        mRSI3Paint.setColor(color);
    }

    /**
     * 设置曲线宽度
     */
    public void setLineWidth(float width)
    {
        mRSI1Paint.setStrokeWidth(width);
        mRSI2Paint.setStrokeWidth(width);
        mRSI3Paint.setStrokeWidth(width);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize)
    {
        mRSI2Paint.setTextSize(textSize);
        mRSI3Paint.setTextSize(textSize);
        mRSI1Paint.setTextSize(textSize);
    }
}
