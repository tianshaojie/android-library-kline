package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.data.model.Kdj;

/**
 * KDJ实现类
 */

public class KdjDrawV2 extends BaseChartDraw {

    private Paint mKPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mJPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public KdjDrawV2(Context context) {
        super(context, ChartEnum.KDJ);
    }

    @Override
    public void drawChartItem(@NonNull Canvas canvas, @Nullable KLine prevPoint, @NonNull KLine currPoint, float prevX, float currX) {
        float r = mChartItemWidth / 2;
        currX+=r; prevX+=r;
        drawLine(canvas, mKPaint, prevX, prevPoint.kdj.k, currX, currPoint.kdj.k);
        drawLine(canvas, mDPaint, prevX, prevPoint.kdj.d, currX, currPoint.kdj.d);
        drawLine(canvas, mJPaint, prevX, prevPoint.kdj.j, currX, currPoint.kdj.j);
    }

    public void drawText(@NonNull Canvas canvas, @NonNull IChartData chartData, float x, float y) {
        Kdj point = (Kdj) chartData;
        String text = "";
        text = "K:" + KLine.getValueFormatter(ChartEnum.KDJ.name()).format(point.k) + "  ";
        canvas.drawText(text, x, y, mKPaint);
        x += mKPaint.measureText(text);
        text = "D:" + KLine.getValueFormatter(ChartEnum.KDJ.name()).format(point.d) + "  ";
        canvas.drawText(text, x, y, mDPaint);
        x += mDPaint.measureText(text);
        text = "J:" + KLine.getValueFormatter(ChartEnum.KDJ.name()).format(point.j) + "  ";
        canvas.drawText(text, x, y, mJPaint);
    }

    /**
     * 设置K颜色
     */
    public void setKColor(int color) {
        mKPaint.setColor(color);
    }

    /**
     * 设置D颜色
     */
    public void setDColor(int color) {
        mDPaint.setColor(color);
    }

    /**
     * 设置J颜色
     */
    public void setJColor(int color) {
        mJPaint.setColor(color);
    }

    /**
     * 设置曲线宽度
     */
    public void setLineWidth(float width)
    {
        mKPaint.setStrokeWidth(width);
        mDPaint.setStrokeWidth(width);
        mJPaint.setStrokeWidth(width);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize)
    {
        mKPaint.setTextSize(textSize);
        mDPaint.setTextSize(textSize);
        mJPaint.setTextSize(textSize);
    }
}
