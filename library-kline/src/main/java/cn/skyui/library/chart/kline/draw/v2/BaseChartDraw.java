package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.data.model.KLine;

public abstract class BaseChartDraw {

    private Context mContext;
    private float mChartWidth = 0;
    private float mChartPadding = 0;

    private Rect mRect;
    private int mRectWidth;

    private Float mMaxValue = Float.MIN_VALUE;
    private Float mMinValue = Float.MAX_VALUE;
    private float mScaleY = 1;

    public BaseChartDraw(Context context) {
        mContext = context;
    }

    public void setRect(Rect rect) {
        mRect = rect;
        mRectWidth = mRect.width();
    }

    public void calculateValue(int mStartIndex, int mStopIndex) {
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine point = (KLine) getItem(i);
            mMaxValue = Math.max(mMaxValue, point.vol.getMaxValue());
            mMinValue = Math.min(mMinValue, point.vol.getMinValue());
        }
        if (Math.abs(mMaxValue) < 0.01) {
            mMaxValue = 15.00f;
        }
        mScaleY = mRect.height() * 1f / (mMaxValue - mMinValue);
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getY(startValue), stopX, getY(stopValue), paint);
    }

    public float getX(int position) {
        return position * (mChartWidth + mChartPadding);
    }

    public float getY(float value) {
        return (mMaxValue - value) * mScaleY + mRect.top;
    }

    public abstract void drawChart(@Nullable IChartData lastPoint, @NonNull IChartData curPoint, float lastX, float curX, @NonNull Canvas canvas, int position);

    public abstract KLine getItem(int position);

    public abstract int getCount();



}
