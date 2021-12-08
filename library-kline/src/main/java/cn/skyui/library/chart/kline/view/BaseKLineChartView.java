package cn.skyui.library.chart.kline.view;

import android.animation.ValueAnimator;
import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.core.view.GestureDetectorCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import cn.skyui.library.chart.kline.R;

import cn.skyui.library.chart.kline.base.IAdapter;
import cn.skyui.library.chart.kline.base.IChartDraw;
import cn.skyui.library.chart.kline.base.IDateTimeFormatter;
import cn.skyui.library.chart.kline.base.IValueFormatter;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.data.model.Candle;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.formatter.TimeFormatter;
import cn.skyui.library.chart.kline.formatter.ValueFormatter;
import cn.skyui.library.chart.kline.utils.ViewUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * K线图
 */
public abstract class BaseKLineChartView extends ScrollAndScaleView {

    private float mTranslateX = Float.MIN_VALUE;
    private int mWidth = 0;

    private int mTopPadding;
    private int mChildPadding;
    private int mBottomPadding;

    private float mMainScaleY = 1;
    private float mVolScaleY = 1;
    private float mChildScaleY = 1;
    private float mDataLen = 0;

    private float mMainMaxValue = Float.MAX_VALUE;
    private float mMainMinValue = Float.MIN_VALUE;
    private float mMainHighMaxValue = 0;
    private float mMainLowMinValue = 0;
    private int mMainMaxIndex = 0;
    private int mMainMinIndex = 0;

    private Float mVolMaxValue = Float.MAX_VALUE;
    private Float mVolMinValue = Float.MIN_VALUE;

    private float mChildMaxValue = Float.MAX_VALUE;
    private float mChildMinValue = Float.MIN_VALUE;

    private int mStartIndex = 0;
    private int mStopIndex = 0;

    private float mPointWidth = 6;
    private int mGridRows = 4;
    private int mGridColumns = 4;

    private Rect mMainRect;
    private Rect mVolRect;
    private Rect mChildRect;
    private float mLineWidth;

    private Paint mGridPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mMaxMinPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//    private Paint mSelectedLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 长按浮窗
    private Paint mSelectorWindowTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectorWindowBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    // 长按十字线/文字
    private Paint mSelectedXLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectedYLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectedPointPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mSelectorFramePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int mSelectedIndex;
    private IChartDraw mMainDraw;
    private IChartDraw mVolDraw;
    private IAdapter mAdapter;

    private Boolean isWR = false;
    private Boolean isShowChild = false;
    private Boolean isShowVol = true;

    //当前点的个数
    private int mItemCount;
    private IChartDraw mChildDraw;
    private String mChildDrawType;
    private Map<String, IChartDraw> mChildDraws = new HashMap<>();

    private IValueFormatter mValueFormatter;
    private IDateTimeFormatter mDateTimeFormatter;

    private ValueAnimator mAnimator;
    private long mAnimationDuration = 500;
    private float mOverScrollRange = 0;

    private OnSelectedChangedListener mOnSelectedChangedListener = null;

    private DataSetObserver mDataSetObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            mItemCount = getAdapter().getCount();
            notifyChanged();
        }

        @Override
        public void onInvalidated() {
            mItemCount = getAdapter().getCount();
            notifyChanged();
        }
    };

    public BaseKLineChartView(Context context) {
        super(context);
        init();
    }

    public BaseKLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BaseKLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setWillNotDraw(false);
        mDetector = new GestureDetectorCompat(getContext(), this);
        mScaleDetector = new ScaleGestureDetector(getContext(), this);
        mTopPadding = (int) getResources().getDimension(R.dimen.chart_top_padding);
        mChildPadding = (int) getResources().getDimension(R.dimen.child_top_padding);
        mBottomPadding = (int) getResources().getDimension(R.dimen.chart_bottom_padding);

        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAnimator.setDuration(mAnimationDuration);
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                invalidate();
            }
        });

        mSelectorFramePaint.setStrokeWidth(ViewUtil.dp2Px(getContext(), 0.6f));
        mSelectorFramePaint.setStyle(Paint.Style.STROKE);
        mSelectorFramePaint.setColor(Color.WHITE);
    }

    int displayHeight = 0;

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.mWidth = w;
        displayHeight = h - mTopPadding - mBottomPadding;
        initRect();
        setTranslateXFromScrollX(mScrollX);
    }

    private void initRect() {
        if (isShowChild) {
            int mMainHeight = (int) (displayHeight * 0.6f);
            int mVolHeight = (int) (displayHeight * 0.2f);
            int mChildHeight = (int) (displayHeight * 0.2f);
            mMainRect = new Rect(0, mTopPadding, mWidth, mTopPadding + mMainHeight);
            mVolRect = new Rect(0, mMainRect.bottom + mChildPadding, mWidth, mMainRect.bottom + mVolHeight);
            mChildRect = new Rect(0, mVolRect.bottom + mChildPadding, mWidth, mVolRect.bottom + mChildHeight);
        } else if(isShowVol) {
            int mMainHeight = (int) (displayHeight * 0.75f);
            int mVolHeight = (int) (displayHeight * 0.25f);
            mMainRect = new Rect(0, mTopPadding, mWidth, mTopPadding + mMainHeight);
            mVolRect = new Rect(0, mMainRect.bottom + mChildPadding, mWidth, mMainRect.bottom + mVolHeight);
        } else {
            mMainRect = new Rect(0, mTopPadding, mWidth, mTopPadding + displayHeight);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(mBackgroundPaint.getColor());
        if (mWidth == 0 || mMainRect.height() == 0 || mItemCount == 0) {
            return;
        }
        calculateValue();
        canvas.save();
        canvas.scale(1, 1);
        drawGird(canvas);
        drawK(canvas);
        drawText(canvas);
        drawMaxAndMin(canvas);
        drawValue(canvas, isLongPress ? mSelectedIndex : mStopIndex);
        canvas.restore();
    }

    /**
     * 画表格
     *
     * @param canvas
     */
    private void drawGird(Canvas canvas) {
        //-----------------------TopMargin区域------------------------
        canvas.drawLine(0, mMainRect.top - mTopPadding, mWidth, mMainRect.top - mTopPadding, mGridPaint);
        canvas.drawLine(0, mMainRect.top - mTopPadding, 0, mMainRect.top + mTopPadding, mGridPaint);
        canvas.drawLine(mWidth, mMainRect.top - mTopPadding, mWidth, mMainRect.top, mGridPaint);
        //-----------------------上方k线图------------------------
        //横向的grid
        float rowSpace = mMainRect.height() / mGridRows;
        for (int i = 0; i <= mGridRows; i++) {
            canvas.drawLine(0, rowSpace * i + mMainRect.top, mWidth, rowSpace * i + mMainRect.top, mGridPaint);
        }
        //-----------------------下方子图------------------------
        if (mChildDraw != null) {
            canvas.drawLine(0, mVolRect.bottom, mWidth, mVolRect.bottom, mGridPaint);
            canvas.drawLine(0, mChildRect.bottom, mWidth, mChildRect.bottom, mGridPaint);
        } else {
            canvas.drawLine(0, mVolRect.bottom, mWidth, mVolRect.bottom, mGridPaint);
        }

        //纵向的grid
//        float columnSpace = mWidth / mGridColumns;
//        for (int i = 0; i <= mGridColumns; i++) {
//            canvas.drawLine(columnSpace * i, mMainRect.top, columnSpace * i, mMainRect.bottom, mGridPaint);
//            canvas.drawLine(columnSpace * i, mMainRect.bottom, columnSpace * i, mVolRect.bottom, mGridPaint);
//            if (mChildDraw != null) {
//                canvas.drawLine(columnSpace * i, mVolRect.bottom, columnSpace * i, mChildRect.bottom, mGridPaint);
//            }
//        }
    }

    /**
     * 画k线图
     *
     * @param canvas
     */
    private void drawK(Canvas canvas) {
        //保存之前的平移，缩放
        canvas.save();
        canvas.translate(mTranslateX * mScaleX, 0); // mTranslateX * mScaleX = -1131
        Log.i("KLineView", " mScaleX=" + mScaleX);
        Log.i("KLineView", "mTranslateX * mScaleX=" + mTranslateX * mScaleX);
        canvas.scale(mScaleX, 1); // mScaleX = 1
        // 51, 100
        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine currentPoint = getItem(i); // data.get(51)
            float currentPointX = getX(i); // 1122
            KLine lastPoint = i == 0 ? currentPoint : getItem(i - 1); // data.get(50)
            float lastX = i == 0 ? currentPointX : getX(i - 1); // 1100
            if (mMainDraw != null) {
                mMainDraw.drawTranslated(lastPoint, currentPoint, lastX, currentPointX, canvas, this, i);
            }
            if (mVolDraw != null) {
                mVolDraw.drawTranslated(lastPoint.getChildData(ChartEnum.VOL.name()), currentPoint.getChildData(ChartEnum.VOL.name()), lastX, currentPointX, canvas, this, i);
            }
            if (mChildDraw != null) {
                mChildDraw.drawTranslated(lastPoint.getChildData(mChildDrawType), currentPoint.getChildData(mChildDrawType), lastX, currentPointX, canvas, this, i);
            }

        }
        //画选择线
        if (isLongPress) {
            KLine point = (KLine) getItem(mSelectedIndex);
            float x = getX(mSelectedIndex);
            float y = getMainY(point.close);

            // k线图竖线
            canvas.drawLine(x, mMainRect.top, x, mMainRect.bottom, mSelectedYLinePaint);
            // k线图横线
            canvas.drawLine(-mTranslateX, y, -mTranslateX + mWidth / mScaleX, y, mSelectedXLinePaint);
            // 柱状图竖线
            canvas.drawLine(x, mMainRect.bottom, x, mVolRect.bottom, mSelectedYLinePaint);
            if (mChildDraw != null) {
                // 子线图竖线
                canvas.drawLine(x, mVolRect.bottom, x, mChildRect.bottom, mSelectedYLinePaint);
            }
        }
        //还原 平移缩放
        canvas.restore();
    }

    /**
     * 画文字
     *
     * @param canvas
     */
    private void drawText(Canvas canvas) {
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        //--------------画上方k线图的值-------------
        if (mMainDraw != null) {
            canvas.drawText(formatValue(mMainMaxValue), 0, baseLine + mMainRect.top, mTextPaint);
            canvas.drawText(formatValue(mMainMinValue), 0, mMainRect.bottom - textHeight + baseLine, mTextPaint);
            float rowValue = (mMainMaxValue - mMainMinValue) / mGridRows;
            float rowSpace = mMainRect.height() / mGridRows;
            for (int i = 1; i < mGridRows; i++) {
                String text = formatValue(rowValue * (mGridRows - i) + mMainMinValue);
                canvas.drawText(text, 0, fixTextY(rowSpace * i + mMainRect.top), mTextPaint);
            }
        }
        //--------------画中间子图的值，最大值最小值-------------
        if (mVolDraw != null) {
            canvas.drawText(KLine.getValueFormatter(ChartEnum.VOL.name()).format(mVolMaxValue),
                    mWidth - calculateWidth(KLine.getValueFormatter(ChartEnum.VOL.name()).format(mVolMaxValue)), mMainRect.bottom + baseLine, mTextPaint);
            /*canvas.drawText(KLine.getValueFormatter(mChildDrawType).format(mVolMinValue),
                    mWidth - calculateWidth(KLine.getValueFormatter(mChildDrawType).format(mVolMinValue)), mVolRect.bottom, mTextPaint);*/
        }
        //--------------画下方子图的值，最大值最小值-------------
        if (mChildDraw != null) {
            canvas.drawText(KLine.getValueFormatter(mChildDrawType).format(mChildMaxValue),
                    mWidth - calculateWidth(KLine.getValueFormatter(mChildDrawType).format(mChildMaxValue)), mChildRect.top + baseLine - textHeight, mTextPaint);
//            canvas.drawText(KLine.getValueFormatter(mChildDrawType).format(mChildMinValue),
//                    mWidth - calculateWidth(KLine.getValueFormatter(mChildDrawType).format(mChildMinValue)), mChildRect.bottom, mTextPaint);
        }
        //--------------画时间---------------------
        float columnSpace = mWidth / mGridColumns;
        float y;
        if (isShowChild) {
            y = mChildRect.bottom + baseLine + 5;
        } else {
            y = mVolRect.bottom + baseLine + 5;
        }

        float startX = getX(mStartIndex) - mPointWidth / 2;
        float stopX = getX(mStopIndex) + mPointWidth / 2;

        for (int i = 1; i < mGridColumns; i++) {
            float translateX = xToTranslateX(columnSpace * i);
            if (translateX >= startX && translateX <= stopX) {
                int index = indexOfTranslateX(translateX);
                String text = formatDateTime(mAdapter.getDate(index));
                canvas.drawText(text, columnSpace * i - mTextPaint.measureText(text) / 2, y, mTextPaint);
            }
        }

        float translateX = xToTranslateX(0);
        if (translateX >= startX && translateX <= stopX) {
            canvas.drawText(formatDateTime(getAdapter().getDate(mStartIndex)), 0, y, mTextPaint);
        }
        translateX = xToTranslateX(mWidth);
        if (translateX >= startX && translateX <= stopX) {
            String text = formatDateTime(getAdapter().getDate(mStopIndex));
            canvas.drawText(text, mWidth - mTextPaint.measureText(text), y, mTextPaint);
        }
        if (isLongPress) {
            // 画Y值
            KLine point = (KLine) getItem(mSelectedIndex);
            float w1 = ViewUtil.dp2Px(getContext(), 5);
            float w2 = ViewUtil.dp2Px(getContext(), 3);
            float r = textHeight / 2 + w2;
            y = getMainY(point.close);
            float x;
            String text = formatValue(point.close);
            float textWidth = mTextPaint.measureText(text);
            if (translateXtoX(getX(mSelectedIndex)) < getChartWidth() / 2) {
                x = 1;
                Path path = new Path();
                path.moveTo(x, y - r);
                path.lineTo(x, y + r);
                path.lineTo(textWidth + 2 * w1, y + r);
                path.lineTo(textWidth + 2 * w1 + w2, y);
                path.lineTo(textWidth + 2 * w1, y - r);
                path.close();
                canvas.drawPath(path, mSelectedPointPaint);
                canvas.drawPath(path, mSelectorFramePaint);
                canvas.drawText(text, x + w1, fixTextY1(y), mTextPaint);
            } else {
                x = mWidth - textWidth - 1 - 2 * w1 - w2;
                Path path = new Path();
                path.moveTo(x, y);
                path.lineTo(x + w2, y + r);
                path.lineTo(mWidth - 2, y + r);
                path.lineTo(mWidth - 2, y - r);
                path.lineTo(x + w2, y - r);
                path.close();
                canvas.drawPath(path, mSelectedPointPaint);
                canvas.drawPath(path, mSelectorFramePaint);
                canvas.drawText(text, x + w1 + w2, fixTextY1(y), mTextPaint);
            }

            // 画X值
            String date = formatDateTime(mAdapter.getDate(mSelectedIndex));
            textWidth = mTextPaint.measureText(date);
            r = textHeight / 2;
            x = translateXtoX(getX(mSelectedIndex));
            if (isShowChild) {
                y = mChildRect.bottom;
            } else {
                y = mVolRect.bottom;
            }

            if (x < textWidth + 2 * w1) {
                x = 1 + textWidth / 2 + w1;
            } else if (mWidth - x < textWidth + 2 * w1) {
                x = mWidth - 1 - textWidth / 2 - w1;
            }

            canvas.drawRect(x - textWidth / 2 - w1, y, x + textWidth / 2 + w1, y + baseLine + r, mSelectedPointPaint);
            canvas.drawRect(x - textWidth / 2 - w1, y, x + textWidth / 2 + w1, y + baseLine + r, mSelectorFramePaint);
            canvas.drawText(date, x - textWidth / 2, y + baseLine + 5, mTextPaint);

            drawSelector(canvas);
        }
    }

    /**
     * 解决text居中的问题
     */
    public float fixTextY1(float y) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        return (y + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private int calculateWidth(String text) {
        Rect rect = new Rect();
        mTextPaint.getTextBounds(text, 0, text.length(), rect);
        return rect.width() + 5;
    }

    /**
     * 计算文本长度
     *
     * @return
     */
    private Rect calculateMaxMin(String text) {
        Rect rect = new Rect();
        mMaxMinPaint.getTextBounds(text, 0, text.length(), rect);
        return rect;
    }


    /**
     * draw选择器
     * @param canvas
     */
    public void drawSelector(Canvas canvas) {
        Paint.FontMetrics metrics = mTextPaint.getFontMetrics();
        float textHeight = metrics.descent - metrics.ascent;

        int index = getSelectedIndex();
        float padding = ViewUtil.dp2Px(getContext(), 5);
        float margin = ViewUtil.dp2Px(getContext(), 5);
        float width = 0;
        float left;
        float top = margin+getTopPadding();
        float height = padding * 8 + textHeight * 5;

        Candle point = (Candle) getItem(index);
        List<String> strings = new ArrayList<>();
        strings.add(formatDateTime(getAdapter().getDate(index)));
        strings.add("高:" + point.high);
        strings.add("低:" + point.low);
        strings.add("开:" + point.open);
        strings.add("收:" + point.close);

        for (String s : strings) {
            width = Math.max(width, mTextPaint.measureText(s));
        }
        width += padding * 2;

        float x = translateXtoX(getX(index));
        if (x > getChartWidth() / 2) {
            left = margin;
        } else {
            left = getChartWidth() - width - margin;
        }

        RectF r = new RectF(left, top, left + width, top + height);
        canvas.drawRoundRect(r, padding, padding, mSelectorWindowBackgroundPaint);
        float y = top + padding * 2 + (textHeight - metrics.bottom - metrics.top) / 2;

        for (String s : strings) {
            canvas.drawText(s, left + padding, y, mSelectorWindowTextPaint);
            y += textHeight + padding;
        }

    }

    /**
     * 画文字
     *
     * @param canvas
     */
    private void drawMaxAndMin(Canvas canvas) {
        //绘制最大值和最小值
        float x = translateXtoX(getX(mMainMinIndex));
        float y = getMainY(mMainLowMinValue);
        String LowString = "── " + mMainLowMinValue;
        //计算显示位置
        //计算文本宽度
        int lowStringWidth = calculateMaxMin(LowString).width();
        int lowStringHeight = calculateMaxMin(LowString).height();
        if (x < getWidth() / 2) {
            //画右边
            canvas.drawText(LowString, x, y + lowStringHeight / 2, mMaxMinPaint);
        } else {
            //画左边
            LowString = mMainLowMinValue + " ──";
            canvas.drawText(LowString, x - lowStringWidth, y + lowStringHeight / 2, mMaxMinPaint);
        }

        x = translateXtoX(getX(mMainMaxIndex));
        y = getMainY(mMainHighMaxValue);

        String highString = "── " + mMainHighMaxValue;
        int highStringWidth = calculateMaxMin(highString).width();
        int highStringHeight = calculateMaxMin(highString).height();
        if (x < getWidth() / 2) {
            //画右边
            canvas.drawText(highString, x, y + highStringHeight / 2, mMaxMinPaint);
        } else {
            //画左边
            highString = mMainHighMaxValue + " ──";
            canvas.drawText(highString, x - highStringWidth, y + highStringHeight / 2, mMaxMinPaint);
        }
    }

    /**
     * 画值
     *
     * @param canvas
     * @param position 显示某个点的值
     */
    private void drawValue(Canvas canvas, int position) {
        Paint.FontMetrics fm = mTextPaint.getFontMetrics();
        float textHeight = fm.descent - fm.ascent;
        float baseLine = (textHeight - fm.bottom - fm.top) / 2;
        if (position >= 0 && position < mItemCount) {
            KLine point = (KLine) getItem(position);
            if (mMainDraw != null) {
                float y = mMainRect.top + baseLine - textHeight;
                float x = 0;
                mMainDraw.drawText(canvas, point, x, y);
            }
            if (mVolDraw != null) {
                float y = mMainRect.bottom + baseLine;
                mVolDraw.drawText(canvas, point.vol, 0, y);
            }
            if (mChildDraw != null) {
                float y = mChildRect.top + baseLine - textHeight;
                mChildDraw.drawText(canvas, point.getChildData(mChildDrawType), 0, y);
            }
        }
    }

    public int dp2px(float dp) {
        final float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int sp2px(float spValue) {
        final float fontScale = getContext().getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }

    public float getMainY(float value) {
        return (mMainMaxValue - value) * mMainScaleY + mMainRect.top;
    }
    public float getVolY(float value) {
        return (mVolMaxValue - value) * mVolScaleY + mVolRect.top;
    }

    public float getChildY(float value) {
        return (mChildMaxValue - value) * mChildScaleY + mChildRect.top;
    }

    /**
     * 解决text居中的问题
     */
    public float fixTextY(float y) {
        Paint.FontMetrics fontMetrics = mTextPaint.getFontMetrics();
        return (y + (fontMetrics.descent - fontMetrics.ascent) / 2 - fontMetrics.descent);
    }

    /**
     * 格式化值
     */
    public String formatValue(float value) {
        if (getValueFormatter() == null) {
            setValueFormatter(new ValueFormatter());
        }
        return getValueFormatter().format(value);
    }

    /**
     * 重新计算并刷新线条
     */
    public void notifyChanged() {
        if (mItemCount != 0) {
            mDataLen = (mItemCount - 1) * mPointWidth;
            checkAndFixScrollX();
            setTranslateXFromScrollX(mScrollX);
        } else {
            setScrollX(0);
        }
        invalidate();
    }

    private void calculateSelectedX(float x) {
        mSelectedIndex = indexOfTranslateX(xToTranslateX(x));
        if (mSelectedIndex < mStartIndex) {
            mSelectedIndex = mStartIndex;
        }
        if (mSelectedIndex > mStopIndex) {
            mSelectedIndex = mStopIndex;
        }
    }

    @Override
    public void onLongPress(MotionEvent e) {
        super.onLongPress(e);
        int lastIndex = mSelectedIndex;
        calculateSelectedX(e.getX());
        if (lastIndex != mSelectedIndex) {
            onSelectedChanged(this, getItem(mSelectedIndex), mSelectedIndex);
        }
        invalidate();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        setTranslateXFromScrollX(mScrollX);
    }

    @Override
    protected void onScaleChanged(float scale, float oldScale) {
        checkAndFixScrollX();
        setTranslateXFromScrollX(mScrollX);
        Log.i("KLineView", "mTranslateX=" + mTranslateX);
        super.onScaleChanged(scale, oldScale);
    }

    /**
     * 计算当前的显示区域
     */
    private void calculateValue() {
        if (!isLongPress()) {
            mSelectedIndex = -1;
        }
        mMainMaxValue = Float.MIN_VALUE;
        mMainMinValue = Float.MAX_VALUE;
        mVolMaxValue = Float.MIN_VALUE;
        mVolMinValue = Float.MAX_VALUE;
        mChildMaxValue = Float.MIN_VALUE;
        mChildMinValue = Float.MAX_VALUE;
        mStartIndex = indexOfTranslateX(xToTranslateX(0));
        mStopIndex = indexOfTranslateX(xToTranslateX(mWidth));

        mMainMaxIndex = mStartIndex;
        mMainMinIndex = mStartIndex;
        mMainHighMaxValue = Float.MIN_VALUE;
        mMainLowMinValue = Float.MAX_VALUE;

        for (int i = mStartIndex; i <= mStopIndex; i++) {
            KLine point = (KLine) getItem(i);
            if (mMainDraw != null) {
                mMainMaxValue = Math.max(mMainMaxValue, point.getMaxValue());
                mMainMinValue = Math.min(mMainMinValue, point.getMinValue());
                if (mMainHighMaxValue != Math.max(mMainHighMaxValue, point.high)) {
                    mMainHighMaxValue = point.high;
                    mMainMaxIndex = i;
                }
                if (mMainLowMinValue != Math.min(mMainLowMinValue, point.low)) {
                    mMainLowMinValue = point.low;
                    mMainMinIndex = i;
                }
            }
            if (isShowVol && mVolDraw != null) {
                mVolMaxValue = Math.max(mVolMaxValue, point.vol.getMaxValue());
                mVolMinValue = Math.min(mVolMinValue, point.vol.getMinValue());
            }
            if (isShowChild && mChildDraw != null) {
                mChildMaxValue = Math.max(mChildMaxValue, point.getChildData(mChildDrawType).getMaxValue());
                mChildMinValue = Math.min(mChildMinValue, point.getChildData(mChildDrawType).getMinValue());
            }
        }
        if (mMainMaxValue != mMainMinValue) {
            float padding = (mMainMaxValue - mMainMinValue) * 0.05f;
            mMainMaxValue += padding;
            mMainMinValue -= padding;
        } else {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mMainMaxValue += Math.abs(mMainMaxValue * 0.05f);
            mMainMinValue -= Math.abs(mMainMinValue * 0.05f);
            if (mMainMaxValue == 0) {
                mMainMaxValue = 1;
            }
        }
        if (Math.abs(mVolMaxValue) < 0.01) {
            mVolMaxValue = 15.00f;
        }

        if (Math.abs(mChildMaxValue) < 0.01 && Math.abs(mChildMinValue) < 0.01) {
            mChildMaxValue = 1f;
        } else if (mChildMaxValue == mChildMinValue) {
            //当最大值和最小值都相等的时候 分别增大最大值和 减小最小值
            mChildMaxValue += Math.abs(mChildMaxValue * 0.05f);
            mChildMinValue -= Math.abs(mChildMinValue * 0.05f);
            if (mChildMaxValue == 0) {
                mChildMaxValue = 1f;
            }
        }

        if (isWR) {
            mChildMaxValue = 0f;
            if (Math.abs(mChildMinValue) < 0.01)
                mChildMinValue = -10.00f;
        }
        mMainScaleY = mMainRect.height() * 1f / (mMainMaxValue - mMainMinValue);
        mVolScaleY = mVolRect.height() * 1f / (mVolMaxValue - mVolMinValue);
        if (mChildRect != null) {
            mChildScaleY = mChildRect.height() * 1f / (mChildMaxValue - mChildMinValue);
        }
        if (mAnimator.isRunning()) {
            float value = (float) mAnimator.getAnimatedValue();
            mStopIndex = mStartIndex + Math.round(value * (mStopIndex - mStartIndex));
        }
    }

    /**
     * 获取平移的最小值
     *
     * @return
     */
    private float getMinTranslateX() {
        Log.i("KLineView", "mWidth=" + mWidth + ", mPointWidth=" + mPointWidth);
        return -mDataLen + mWidth / mScaleX - mPointWidth / 2;
    }

    /**
     * 获取平移的最大值
     *
     * @return
     */
    private float getMaxTranslateX() {
        if (!isFullScreen()) {
            return getMinTranslateX();
        }
        return mPointWidth / 2;
    }

    @Override
    public int getMinScrollX() {
        return (int) -(mOverScrollRange / mScaleX);
    }

    public int getMaxScrollX() {
        return Math.round(getMaxTranslateX() - getMinTranslateX());
    }

    public int indexOfTranslateX(float translateX) {
        return indexOfTranslateX(translateX, 0, mItemCount - 1);
    }

    /**
     * 在主区域画线
     *
     * @param startX    开始点的横坐标
     * @param stopX     开始点的值
     * @param stopX     结束点的横坐标
     * @param stopValue 结束点的值
     */
    public void drawMainLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getMainY(startValue), stopX, getMainY(stopValue), paint);
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawVolLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getVolY(startValue), stopX, getVolY(stopValue), paint);
    }

    /**
     * 在子区域画线
     *
     * @param startX     开始点的横坐标
     * @param startValue 开始点的值
     * @param stopX      结束点的横坐标
     * @param stopValue  结束点的值
     */
    public void drawChildLine(Canvas canvas, Paint paint, float startX, float startValue, float stopX, float stopValue) {
        canvas.drawLine(startX, getChildY(startValue), stopX, getChildY(stopValue), paint);
    }

    /**
     * 根据索引获取实体
     *
     * @param position 索引值
     * @return
     */
    public KLine getItem(int position) {
        if (mAdapter != null) {
            return mAdapter.getItem(position);
        } else {
            return null;
        }
    }

    /**
     * 根据索引索取x坐标
     *
     * @param position 索引值
     * @return
     */
    public float getX(int position) {
        return position * mPointWidth;
    }

    /**
     * 获取适配器
     *
     * @return
     */
    public IAdapter getAdapter() {
        return mAdapter;
    }

    /**
     * 隐藏子图
     */
    public void hideChildDraw() {
        isShowChild = false;
        mChildDraw = null;
        this.mChildDrawType = null;
        initRect();
        invalidate();
    }

    /**
     * 设置子图的绘制方法
     *
     * @param mSelectedChartName
     */
    public void setChildDraw(String mSelectedChartName) {
        this.mChildDrawType = mSelectedChartName;
        this.mChildDraw = mChildDraws.get(mSelectedChartName);
        if (!isShowChild) {
            isShowChild = true;
            initRect();
        }
        invalidate();
    }

    /**
     * 给子区域添加画图方法
     *
     * @param name      显示的文字标签
     * @param childDraw IChartDraw
     */
    public void addChildDraw(String name, IChartDraw childDraw) {
        mChildDraws.put(name, childDraw);
    }

    /**
     * scrollX 转换为 TranslateX
     *
     * @param scrollX
     */
    private void setTranslateXFromScrollX(int scrollX) {
        mTranslateX = scrollX + getMinTranslateX();
    }

    /**
     * 获取ValueFormatter
     *
     * @return
     */
    public IValueFormatter getValueFormatter() {
        return mValueFormatter;
    }

    /**
     * 设置ValueFormatter
     *
     * @param valueFormatter value格式化器
     */
    public void setValueFormatter(IValueFormatter valueFormatter) {
        this.mValueFormatter = valueFormatter;
    }

    /**
     * 获取DatetimeFormatter
     *
     * @return 时间格式化器
     */
    public IDateTimeFormatter getDateTimeFormatter() {
        return mDateTimeFormatter;
    }

    /**
     * 设置dateTimeFormatter
     *
     * @param dateTimeFormatter 时间格式化器
     */
    public void setDateTimeFormatter(IDateTimeFormatter dateTimeFormatter) {
        mDateTimeFormatter = dateTimeFormatter;
    }

    /**
     * 格式化时间
     *
     * @param date
     */
    public String formatDateTime(Date date) {
        if (getDateTimeFormatter() == null) {
            setDateTimeFormatter(new TimeFormatter());
        }
        return getDateTimeFormatter().format(date);
    }

    /**
     * 获取主区域的 IChartDraw
     *
     * @return IChartDraw
     */
    public IChartDraw getMainDraw() {
        return mMainDraw;
    }

    /**
     * 设置主区域的 IChartDraw
     *
     * @param mainDraw IChartDraw
     */
    public void setMainDraw(IChartDraw mainDraw) {
        mMainDraw = mainDraw;
    }

    public IChartDraw getVolDraw() {
        return mVolDraw;
    }

    public void setVolDraw(IChartDraw mVolDraw) {
        this.mVolDraw = mVolDraw;
    }


    /**
     * 二分查找当前值的index
     *
     * @return
     */
    public int indexOfTranslateX(float translateX, int start, int end) {
        if (end == start) {
            return start;
        }
        if (end - start == 1) {
            float startValue = getX(start);
            float endValue = getX(end);
            return Math.abs(translateX - startValue) < Math.abs(translateX - endValue) ? start : end;
        }
        int mid = start + (end - start) / 2;
        float midValue = getX(mid);
        if (translateX < midValue) {
            return indexOfTranslateX(translateX, start, mid);
        } else if (translateX > midValue) {
            return indexOfTranslateX(translateX, mid, end);
        } else {
            return mid;
        }
    }

    /**
     * 设置数据适配器
     */
    public void setAdapter(IAdapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
        }
        mAdapter = adapter;
        if (mAdapter != null) {
            mAdapter.registerDataSetObserver(mDataSetObserver);
            mItemCount = mAdapter.getCount();
        } else {
            mItemCount = 0;
        }
        notifyChanged();
    }

    /**
     * 开始动画
     */
    public void startAnimation() {
        if (mAnimator != null) {
            mAnimator.start();
        }
    }

    /**
     * 设置动画时间
     */
    public void setAnimationDuration(long duration) {
        if (mAnimator != null) {
            mAnimator.setDuration(duration);
        }
    }

    /**
     * 设置表格行数
     */
    public void setGridRows(int gridRows) {
        if (gridRows < 1) {
            gridRows = 1;
        }
        mGridRows = gridRows;
    }

    /**
     * 设置表格列数
     */
    public void setGridColumns(int gridColumns) {
        if (gridColumns < 1) {
            gridColumns = 1;
        }
        mGridColumns = gridColumns;
    }

    /**
     * view中的x转化为TranslateX
     *
     * @param x
     * @return
     */
    public float xToTranslateX(float x) {
        return -mTranslateX + x / mScaleX;
    }

    /**
     * translateX转化为view中的x
     *
     * @param translateX
     * @return
     */
    public float translateXtoX(float translateX) {
        return (translateX + mTranslateX) * mScaleX;
    }

    /**
     * 获取上方padding
     */
    public float getTopPadding() {
        return mTopPadding;
    }

    /**
     * 获取上方padding
     */
    public float getChildPadding() {
        return mChildPadding;
    }

    /**
     * 获取图的宽度
     *
     * @return
     */
    public int getChartWidth() {
        return mWidth;
    }

    /**
     * 是否长按
     */
    public boolean isLongPress() {
        return isLongPress;
    }

    /**
     * 获取选择索引
     */
    public int getSelectedIndex() {
        return mSelectedIndex;
    }

    public Rect getVolRect() {
        return mVolRect;
    }

    public Rect getChildRect() {
        return mChildRect;
    }

    /**
     * 设置选择监听
     */
    public void setOnSelectedChangedListener(OnSelectedChangedListener l) {
        this.mOnSelectedChangedListener = l;
    }

    public void onSelectedChanged(BaseKLineChartView view, Object point, int index) {
        if (this.mOnSelectedChangedListener != null) {
            mOnSelectedChangedListener.onSelectedChanged(view, point, index);
        }
    }

    /**
     * 数据是否充满屏幕
     *
     * @return
     */
    public boolean isFullScreen() {
        return mDataLen >= mWidth / mScaleX;
    }

    /**
     * 设置超出右方后可滑动的范围
     */
    public void setOverScrollRange(float overScrollRange) {
        if (overScrollRange < 0) {
            overScrollRange = 0;
        }
        mOverScrollRange = overScrollRange;
    }

    /**
     * 设置上方padding
     *
     * @param topPadding
     */
    public void setTopPadding(int topPadding) {
        mTopPadding = topPadding;
    }

    /**
     * 设置下方padding
     *
     * @param bottomPadding
     */
    public void setBottomPadding(int bottomPadding) {
        mBottomPadding = bottomPadding;
    }

    /**
     * 设置表格线宽度
     */
    public void setGridLineWidth(float width) {
        mGridPaint.setStrokeWidth(width);
    }

    /**
     * 设置表格线颜色
     */
    public void setGridLineColor(int color) {
        mGridPaint.setColor(color);
    }

//    /**
//     * 设置选择线宽度
//     */
//    public void setSelectedLineWidth(float width) {
//        mSelectedLinePaint.setStrokeWidth(width);
//    }
//
//    /**
//     * 设置表格线颜色
//     */
//    public void setSelectedLineColor(int color) {
//        mSelectedLinePaint.setColor(color);
//    }

    /**
     * 设置文字颜色
     */
    public void setTextColor(int color) {
        mTextPaint.setColor(color);
        mSelectorWindowTextPaint.setColor(color);
    }

    /**
     * 设置选择器文字大小
     * @param textSize
     */
    public void setSelectorWindowTextSize(float textSize){
        mSelectorWindowTextPaint.setTextSize(textSize);
    }

    /**
     * 设置选择器背景
     * @param color
     */
    public void setSelectorWindowBackgroundColor(int color) {
        mSelectorWindowBackgroundPaint.setColor(color);
    }

    /**
     * 设置文字大小
     */
    public void setTextSize(float textSize) {
        mTextPaint.setTextSize(textSize);
    }

    /**
     * 设置最大值/最小值文字颜色
     */
    public void setMaxMinTextColor(int color) {
        mMaxMinPaint.setColor(color);
    }

    /**
     * 设置最大值/最小值文字大小
     */
    public void setMaxMinTextSize(float textSize) {
        mMaxMinPaint.setTextSize(textSize);
    }

    /**
     * 设置背景颜色
     */
    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
    }


    /**
     * 选中点变化时的监听
     */
    public interface OnSelectedChangedListener {
        /**
         * 当选点中变化时
         *
         * @param view  当前view
         * @param point 选中的点
         * @param index 选中点的索引
         */
        void onSelectedChanged(BaseKLineChartView view, Object point, int index);
    }

    /**
     * 获取文字大小
     */
    public float getTextSize() {
        return mTextPaint.getTextSize();
    }

    /**
     * 获取曲线宽度
     */
    public float getLineWidth() {
        return mLineWidth;
    }

    /**
     * 设置曲线的宽度
     */
    public void setLineWidth(float lineWidth) {
        mLineWidth = lineWidth;
    }

    /**
     * 设置每个点的宽度
     */
    public void setPointWidth(float pointWidth) {
        mPointWidth = pointWidth;
    }

    public Paint getGridPaint() {
        return mGridPaint;
    }

    public Paint getTextPaint() {
        return mTextPaint;
    }

    public Paint getBackgroundPaint() {
        return mBackgroundPaint;
    }

//    public Paint getSelectedLinePaint() {
//        return mSelectedLinePaint;
//    }

    /**
     * 设置选中point 值显示背景
     */
    public void setSelectedPointTextBackgroundColor(int color) {
        mSelectedPointPaint.setColor(color);
    }

    /**
     * 设置选择器横线宽度
     */
    public void setSelectedXLineWidth(float width) {
        mSelectedXLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置选择器横线颜色
     */
    public void setSelectedXLineColor(int color) {
        mSelectedXLinePaint.setColor(color);
    }

    /**
     * 设置选择器竖线宽度
     */
    public void setSelectedYLineWidth(float width) {
        mSelectedYLinePaint.setStrokeWidth(width);
    }

    /**
     * 设置选择器竖线颜色
     */
    public void setSelectedYLineColor(int color) {
        mSelectedYLinePaint.setColor(color);
    }
}
