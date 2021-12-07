package cn.skyui.library.chart.kline.view;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ProgressBar;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.draw.BollDraw;
import cn.skyui.library.chart.kline.draw.CandleDraw;
import cn.skyui.library.chart.kline.draw.KdjDraw;
import cn.skyui.library.chart.kline.draw.MacdDraw;
import cn.skyui.library.chart.kline.draw.RsiDraw;
import cn.skyui.library.chart.kline.draw.VolumeDraw;

/**
 * k线图
 */
public class KLineChartView extends BaseKLineChartView {

    ProgressBar mProgressBar;
    private boolean isRefreshing = false;
    private boolean isLoadMoreEnd = false;
    private boolean mLastScrollEnable;
    private boolean mLastScaleEnable;

    private KChartRefreshListener mRefreshListener;

    private MacdDraw mMACDDraw;
    private BollDraw mBOLLDraw;
    private RsiDraw mRSIDraw;
    private CandleDraw mCandleDraw;
    private KdjDraw mKDJDraw;
    private VolumeDraw mVolumeDraw;


    public KLineChartView(Context context) {
        this(context, null);
    }

    public KLineChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KLineChartView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initAttrs(attrs);
    }

    private void initView() {
        mProgressBar = new ProgressBar(getContext());
        LayoutParams layoutParams = new LayoutParams(dp2px(50), dp2px(50));
        layoutParams.addRule(CENTER_IN_PARENT);
        addView(mProgressBar, layoutParams);
        mProgressBar.setVisibility(GONE);
        mVolumeDraw = new VolumeDraw(getContext());
        mMACDDraw = new MacdDraw(getContext());
        mKDJDraw = new KdjDraw(getContext());
        mRSIDraw = new RsiDraw(getContext());
        mBOLLDraw = new BollDraw(getContext());
        mCandleDraw = new CandleDraw(getContext());
        addChildDraw(ChartEnum.MACD.name(),mMACDDraw);
        addChildDraw(ChartEnum.KDJ.name(), mKDJDraw);
        addChildDraw(ChartEnum.RSI.name(), mRSIDraw);
        addChildDraw(ChartEnum.BOOL.name(),mBOLLDraw);
        setChildDraw(ChartEnum.MACD.name());
        setVolDraw(mVolumeDraw);
        setMainDraw(mCandleDraw);
    }

    private void initAttrs(AttributeSet attrs) {
        TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.KChartView);
        if (array != null) {
            try {
                // public
                // 背景色
                setBackgroundColor(array.getColor(R.styleable.KChartView_kc_background_color, getColor(R.color.chart_background)));
                // 坐标格线条颜色宽度
                setGridLineWidth(array.getDimension(R.styleable.KChartView_kc_grid_line_width, getDimension(R.dimen.chart_grid_line_width)));
                setGridLineColor(array.getColor(R.styleable.KChartView_kc_grid_line_color, getColor(R.color.chart_grid_line_color)));
                // 各类曲线宽度
                setLineWidth(array.getDimension(R.styleable.KChartView_kc_line_width, getDimension(R.dimen.chart_line_width)));
                // 单个日期点的宽度
                setPointWidth(array.getDimension(R.styleable.KChartView_kc_point_width, getDimension(R.dimen.chart_point_width)));
                // 默认字体大小颜色
                setTextSize(array.getDimension(R.styleable.KChartView_kc_text_size, getDimension(R.dimen.chart_text_size)));
                setTextColor(array.getColor(R.styleable.KChartView_kc_text_color, getColor(R.color.chart_text)));
                // 设置最大值/最小值文字大小颜色
                setMaxMinTextSize(array.getDimension(R.styleable.KChartView_kc_max_min_text_size, getDimension(R.dimen.chart_max_min_text_size)));
                setMaxMinTextColor(array.getColor(R.styleable.KChartView_kc_max_min_text_color, getColor(R.color.chart_max_min_text)));
                // 选中十字线颜色宽度
                setSelectedXLineColor(array.getColor(R.styleable.KChartView_kc_selected_x_line_color, getColor(R.color.chart_selected_x_line_color)));
                setSelectedXLineWidth(array.getDimension(R.styleable.KChartView_kc_selected_x_line_width, getDimension(R.dimen.chart_selected_x_line_width)));
                setSelectedYLineColor(array.getColor(R.styleable.KChartView_kc_selected_y_line_color, getColor(R.color.chart_selected_y_line_color)));
                setSelectedYLineWidth(array.getDimension(R.styleable.KChartView_kc_selected_y_line_width, getDimension(R.dimen.chart_selected_y_line_width)));
                // 选中十字线文字框的背景色
                setSelectedPointTextBackgroundColor(array.getColor(R.styleable.KChartView_kc_selected_point_text_bg_color, getColor(R.color.chart_selected_point_text_bg_color)));
                // 选中浮窗背景色字体色
                setSelectorWindowBackgroundColor(array.getColor(R.styleable.KChartView_kc_selector_window_bg_color, getColor(R.color.chart_selector_window_bg_color)));
                setSelectorWindowTextSize(array.getDimension(R.styleable.KChartView_kc_selected_window_text_size, getDimension(R.dimen.chart_selected_window_text_size)));

                //candle
                setMa5Color(array.getColor(R.styleable.KChartView_kc_dif_color, getColor(R.color.chart_ma5)));
                setMa10Color(array.getColor(R.styleable.KChartView_kc_dea_color, getColor(R.color.chart_ma10)));
                setMa20Color(array.getColor(R.styleable.KChartView_kc_macd_color, getColor(R.color.chart_ma20)));
                setCandleWidth(array.getDimension(R.styleable.KChartView_kc_candle_width, getDimension(R.dimen.chart_candle_width)));
                setCandleLineWidth(array.getDimension(R.styleable.KChartView_kc_candle_line_width, getDimension(R.dimen.chart_candle_line_width)));
                setCandleSolid(array.getBoolean(R.styleable.KChartView_kc_candle_solid, true));

                //macd
                setMACDWidth(array.getDimension(R.styleable.KChartView_kc_macd_width, getDimension(R.dimen.chart_candle_width)));
                setDIFColor(array.getColor(R.styleable.KChartView_kc_dif_color, getColor(R.color.chart_ma5)));
                setDEAColor(array.getColor(R.styleable.KChartView_kc_dea_color, getColor(R.color.chart_ma10)));
                setMACDColor(array.getColor(R.styleable.KChartView_kc_macd_color, getColor(R.color.chart_ma20)));
                //kdj
                setKColor(array.getColor(R.styleable.KChartView_kc_dif_color, getColor(R.color.chart_ma5)));
                setDColor(array.getColor(R.styleable.KChartView_kc_dea_color, getColor(R.color.chart_ma10)));
                setJColor(array.getColor(R.styleable.KChartView_kc_macd_color, getColor(R.color.chart_ma20)));
                //rsi
                setRSI1Color(array.getColor(R.styleable.KChartView_kc_dif_color, getColor(R.color.chart_ma5)));
                setRSI2Color(array.getColor(R.styleable.KChartView_kc_dea_color, getColor(R.color.chart_ma10)));
                setRSI3Color(array.getColor(R.styleable.KChartView_kc_macd_color, getColor(R.color.chart_ma20)));
                //boll
                setUpColor(array.getColor(R.styleable.KChartView_kc_dif_color, getColor(R.color.chart_ma5)));
                setMbColor(array.getColor(R.styleable.KChartView_kc_dea_color, getColor(R.color.chart_ma10)));
                setDnColor(array.getColor(R.styleable.KChartView_kc_macd_color, getColor(R.color.chart_ma20)));

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                array.recycle();
            }
        }
    }

    private float getDimension(@DimenRes int resId) {
        return getResources().getDimension(resId);
    }

    private int getColor(@ColorRes int resId) {
        return ContextCompat.getColor(getContext(), resId);
    }

    @Override
    public void onLeftSide() {
        showLoading();
    }

    @Override
    public void onRightSide() {
    }

    public void showLoading() {
        if (!isLoadMoreEnd && !isRefreshing) {
            isRefreshing = true;
            if (mProgressBar != null) {
                mProgressBar.setVisibility(View.VISIBLE);
            }
            if (mRefreshListener != null) {
                mRefreshListener.onLoadMoreBegin(this);
            }
            mLastScaleEnable = isScaleEnable();
            mLastScrollEnable = isScrollEnable();
            super.setScrollEnable(false);
            super.setScaleEnable(false);
        }
    }

    private void hideLoading() {
        if (mProgressBar != null) {
            mProgressBar.setVisibility(View.GONE);
        }
        super.setScrollEnable(mLastScrollEnable);
        super.setScaleEnable(mLastScaleEnable);
    }

    /**
     * 刷新完成
     */
    public void refreshComplete() {
        isRefreshing = false;
        hideLoading();
    }

    /**
     * 刷新完成，没有数据
     */
    public void refreshEnd() {
        isLoadMoreEnd = true;
        isRefreshing = false;
        hideLoading();
    }

    /**
     * 重置加载更多
     */
    public void resetLoadMoreEnd() {
        isLoadMoreEnd = false;
    }

    public interface KChartRefreshListener {
        /**
         * 加载更多
         *
         * @param chart
         */
        void onLoadMoreBegin(KLineChartView chart);
    }

    @Override
    public void setScaleEnable(boolean scaleEnable) {
        if (isRefreshing) {
            throw new IllegalStateException("请勿在刷新状态设置属性");
        }
        super.setScaleEnable(scaleEnable);

    }

    @Override
    public void setScrollEnable(boolean scrollEnable) {
        if (isRefreshing) {
            throw new IllegalStateException("请勿在刷新状态设置属性");
        }
        super.setScrollEnable(scrollEnable);
    }

    /**
     * 设置DIF颜色
     */
    public void setDIFColor(int color) {
        mMACDDraw.setDIFColor(color);
    }

    /**
     * 设置DEA颜色
     */
    public void setDEAColor(int color) {
        mMACDDraw.setDEAColor(color);
    }

    /**
     * 设置MACD颜色
     */
    public void setMACDColor(int color) {
        mMACDDraw.setMACDColor(color);
    }

    /**
     * 设置MACD的宽度
     *
     * @param MACDWidth
     */
    public void setMACDWidth(float MACDWidth) {
        mMACDDraw.setMACDWidth(MACDWidth);
    }

    /**
     * 设置up颜色
     */
    public void setUpColor(int color) {
        mBOLLDraw.setUpColor(color);
    }

    /**
     * 设置mb颜色
     *
     * @param color
     */
    public void setMbColor(int color) {
        mBOLLDraw.setMbColor(color);
    }

    /**
     * 设置dn颜色
     */
    public void setDnColor(int color) {
        mBOLLDraw.setDnColor(color);
    }

    /**
     * 设置K颜色
     */
    public void setKColor(int color) {
        mKDJDraw.setKColor(color);
    }

    /**
     * 设置D颜色
     */
    public void setDColor(int color) {
        mKDJDraw.setDColor(color);
    }

    /**
     * 设置J颜色
     */
    public void setJColor(int color) {
        mKDJDraw.setJColor(color);
    }

    /**
     * 设置ma5颜色
     *
     * @param color
     */
    public void setMa5Color(int color) {
        mCandleDraw.setMa5Color(color);
        mVolumeDraw.setMa5Color(color);
    }

    /**
     * 设置ma10颜色
     *
     * @param color
     */
    public void setMa10Color(int color) {
        mCandleDraw.setMa10Color(color);
        mVolumeDraw.setMa10Color(color);
    }

    /**
     * 设置ma20颜色
     *
     * @param color
     */
    public void setMa20Color(int color) {
        mCandleDraw.setMa20Color(color);
    }

    /**
     * 设置选择器文字大小
     *
     * @param textSize
     */
    public void setSelectorWindowTextSize(float textSize) {
        super.setSelectorWindowTextSize(textSize);
    }

    /**
     * 设置选择器背景
     *
     * @param color
     */
    public void setSelectorWindowBackgroundColor(int color) {
        super.setSelectorWindowBackgroundColor(color);
    }

    /**
     * 设置蜡烛宽度
     *
     * @param candleWidth
     */
    public void setCandleWidth(float candleWidth) {
        mCandleDraw.setCandleWidth(candleWidth);
    }

    /**
     * 设置蜡烛线宽度
     *
     * @param candleLineWidth
     */
    public void setCandleLineWidth(float candleLineWidth) {
        mCandleDraw.setCandleLineWidth(candleLineWidth);
    }

    /**
     * 蜡烛是否空心
     */
    public void setCandleSolid(boolean candleSolid) {
        mCandleDraw.setCandleSolid(candleSolid);
    }

    public void setRSI1Color(int color) {
        mRSIDraw.setRSI1Color(color);
    }

    public void setRSI2Color(int color) {
        mRSIDraw.setRSI2Color(color);
    }

    public void setRSI3Color(int color) {
        mRSIDraw.setRSI3Color(color);
    }

    @Override
    public void setTextSize(float textSize) {
        super.setTextSize(textSize);
        mCandleDraw.setTextSize(textSize);
        mBOLLDraw.setTextSize(textSize);
        mRSIDraw.setTextSize(textSize);
        mMACDDraw.setTextSize(textSize);
        mKDJDraw.setTextSize(textSize);
        mVolumeDraw.setTextSize(textSize);
    }

    @Override
    public void setLineWidth(float lineWidth) {
        super.setLineWidth(lineWidth);
        mCandleDraw.setLineWidth(lineWidth);
        mBOLLDraw.setLineWidth(lineWidth);
        mRSIDraw.setLineWidth(lineWidth);
        mMACDDraw.setLineWidth(lineWidth);
        mKDJDraw.setLineWidth(lineWidth);
        mVolumeDraw.setLineWidth(lineWidth);
    }

    @Override
    public void setTextColor(int color) {
        super.setTextColor(color);
        mVolumeDraw.setTextColor(color);
        mMACDDraw.setTextColor(color);
    }

    /**
     * 设置刷新监听
     */
    public void setRefreshListener(KChartRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }
}
