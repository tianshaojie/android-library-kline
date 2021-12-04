package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.base.IChartDraw;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.data.model.Rsi;
import cn.skyui.library.chart.kline.view.BaseKLineChartView;

/**
 * RSI实现类
 */

public class RsiDrawV2 extends BaseChartDraw {

    private Paint mRSI1Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRSI2Paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mRSI3Paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RsiDrawV2(Context context) {
        super(context, ChartEnum.RSI);
    }

    @Override
    public void drawSingleChart(@NonNull Canvas canvas, @Nullable KLine prevPoint, @NonNull KLine currPoint, float prevX, float curX) {
        drawLine(canvas, mRSI1Paint, prevX, prevPoint.rsi.rsi1, curX, currPoint.rsi.rsi1);
        drawLine(canvas, mRSI2Paint, prevX, prevPoint.rsi.rsi2, curX, currPoint.rsi.rsi2);
        drawLine(canvas, mRSI3Paint, prevX, prevPoint.rsi.rsi3, curX, currPoint.rsi.rsi3);
    }
    

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
