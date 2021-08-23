package cn.skyui.library.chart.kline.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import cn.skyui.library.chart.kline.view.BaseKLineChartView;
import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.base.IChartDraw;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.Boll;
import cn.skyui.library.chart.kline.data.model.KLine;

/**
 * BOLL实现类
 */

public class BollDraw implements IChartDraw<Boll> {

    private Paint mUpPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mMbPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mDnPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public BollDraw(BaseKLineChartView view) {

    }

    @Override
    public void drawTranslated(@Nullable Boll lastPoint, @NonNull Boll curPoint, float lastX, float curX, @NonNull Canvas canvas, @NonNull BaseKLineChartView view, int position) {
        view.drawChildLine(canvas, mUpPaint, lastX, lastPoint.up, curX, curPoint.up);
        view.drawChildLine(canvas, mMbPaint, lastX, lastPoint.mb, curX, curPoint.mb);
        view.drawChildLine(canvas, mDnPaint, lastX, lastPoint.dn, curX, curPoint.dn);
    }

    @Override
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
