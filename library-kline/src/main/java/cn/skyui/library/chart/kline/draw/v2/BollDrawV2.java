package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.base.IChartDraw;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.Boll;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.view.BaseKLineChartView;

/**
 * BOLL实现类
 */

public class BollDrawV2 extends BaseChartDraw {

    private Paint mUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mMbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BollDrawV2(Context context) {
        super(context, ChartEnum.BOOL);
    }

    @Override
    public void drawSingleChart(@NonNull Canvas canvas, @Nullable KLine prevPoint, @NonNull KLine currPoint, float prevX, float curX) {
        drawLine(canvas, mUpPaint, prevX, prevPoint.boll.up, curX, currPoint.boll.up);
        drawLine(canvas, mMbPaint, prevX, prevPoint.boll.mb, curX, currPoint.boll.mb);
        drawLine(canvas, mDnPaint, prevX, prevPoint.boll.dn, curX, currPoint.boll.dn);
    }

    public void drawText(@NonNull Canvas canvas, @NonNull IChartData chartData, float x, float y) {
        Boll point = (Boll) chartData;
        String text = "";
        text = "UP:" + KLine.getValueFormatter(ChartEnum.BOOL.name()).format(point.up) + "  ";
        canvas.drawText(text, x, y, mUpPaint);
        x += mUpPaint.measureText(text);
        text = "MB:" + KLine.getValueFormatter(ChartEnum.BOOL.name()).format(point.mb) + "  ";
        canvas.drawText(text, x, y, mMbPaint);
        x += mMbPaint.measureText(text);
        text = "DN:" + KLine.getValueFormatter(ChartEnum.BOOL.name()).format(point.dn) + "  ";
        canvas.drawText(text, x, y, mDnPaint);
    }

    /**
     * 设置up颜色
     */
    public void setUpColor(int color) {
        mUpPaint.setColor(color);
    }

    /**
     * 设置mb颜色
     * @param color
     */
    public void setMbColor(int color) {
        mMbPaint.setColor(color);
    }

    /**
     * 设置dn颜色
     */
    public void setDnColor(int color) {
        mDnPaint.setColor(color);
    }

    /**
     * 设置曲线宽度
     */
    public void setLineWidth(float width)
    {
        mUpPaint.setStrokeWidth(width);
        mMbPaint.setStrokeWidth(width);
        mDnPaint.setStrokeWidth(width);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize)
    {
        mUpPaint.setTextSize(textSize);
        mMbPaint.setTextSize(textSize);
        mDnPaint.setTextSize(textSize);
    }
}
