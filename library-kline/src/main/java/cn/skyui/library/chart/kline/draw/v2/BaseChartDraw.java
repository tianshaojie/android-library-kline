package cn.skyui.library.chart.kline.draw.v2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.KLine;

public abstract class BaseChartDraw {

    protected Context mContext;
    protected ChartEnum mChartType;
    protected List<KLine> mDateList;
    protected int mStartIndex;
    protected int mStopIndex;

    protected float mChartWidth;
    protected float mChartPadding;

    protected Rect mRect;
    protected int mRectWidth;

    protected float mMaxValue = Float.MIN_VALUE;
    protected float mMinValue = Float.MAX_VALUE;

    protected float mScaleX = 1;
    protected float mScaleY = 1;

    public BaseChartDraw(Context context, ChartEnum chartType) {
        mContext = context;
        mChartType = chartType;
        mChartWidth = (int) context.getResources().getDimension(R.dimen.chart_candle_width);
        mChartPadding = (int) context.getResources().getDimension(R.dimen.chart_candle_padding);
    }

    public void setRect(Rect rect) {
        mRect = rect;
        mRectWidth = mRect.width();
    }

    public void calculateValue(List<KLine> dataList, int startIndex, int stopIndex) {
        mDateList = dataList;
        mStartIndex = startIndex;
        mStopIndex = stopIndex;
        mMaxValue = Float.MIN_VALUE;
        mMinValue = Float.MAX_VALUE;
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine point = (KLine) dataList.get(i);
            mMaxValue = Math.max(mMaxValue, point.getChildData(mChartType.name()).getMaxValue());
            mMinValue = Math.min(mMinValue, point.getChildData(mChartType.name()).getMinValue());
        }
        if (Math.abs(mMaxValue) < 0.01) {
            mMaxValue = 15.00f;
        }
        mScaleY = mRect.height() * 1f / (mMaxValue - mMinValue);
    }

    public void onDraw(Canvas canvas, int scrollX) {
        if(mRect == null || mDateList == null || mDateList.size() == 0) {
            return;
        }
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine currentPoint = mDateList.get(i);
            int scrollOutCount = mDateList.size() - i;
            float currentPointX = mRectWidth - getX(scrollOutCount) + mChartPadding / 2 + scrollX;
            KLine prevPoint = i == 0 ? currentPoint : mDateList.get(i - 1);
            float prevX = i == 0 ? currentPointX : mRectWidth - getX(scrollOutCount + 1) + mChartPadding / 2 + scrollX;
            drawChart(canvas, prevPoint, currentPoint, prevX, currentPointX);
        }
    }

    protected abstract void drawChart(@NonNull Canvas canvas, @Nullable KLine prevPoint, @NonNull KLine currPoint, float prevX, float curX);

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    protected void drawLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getY(startValue), stopX, getY(stopValue), paint);
    }

    /**
     * 根据索引索取X坐标
     *
     * @param position 索引值
     * @return X坐标
     */
    protected float getX(int position) {
        return position * (mChartWidth + mChartPadding);
    }

    /**
     * 获取架构对应的Y坐标
     *
     * @param value 价格
     * @return Y坐标
     */
    protected float getY(float value) {
        return (mMaxValue - value) * mScaleY + mRect.top;
    }
}
