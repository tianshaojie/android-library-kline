package cn.skyui.library.chart.kline.base;

import android.graphics.Canvas;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;


/**
 * 画图的基类 根据实体来画图形
 */

public interface IChartDrawV2<T> {
    /**
     * 需要滑动 物体draw方法
     *
     * @param canvas    canvas
     * @param position  当前点的位置
     * @param lastPoint 上一个点
     * @param curPoint  当前点
     * @param lastX     上一个点的x坐标
     * @param curX      当前点的X坐标
     */
    void drawTranslated(@Nullable T lastPoint, @NonNull T curPoint, float lastX, float curX, @NonNull Canvas canvas, int position);

    /**
     * @param canvas
     * @param point
     * @param x        x的起始坐标
     * @param y        y的起始坐标
     */
    void drawText(@NonNull Canvas canvas, @NonNull IChartData point, float x, float y);

}
