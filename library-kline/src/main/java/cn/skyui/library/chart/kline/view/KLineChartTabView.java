package cn.skyui.library.chart.kline.view;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.skyui.library.chart.kline.R;
import cn.skyui.library.chart.kline.utils.ViewUtil;
import cn.skyui.library.chart.kline.view.TabView;


/**
 * K线图中间位置的TabBar
 */
public class KLineChartTabView extends RelativeLayout implements View.OnClickListener {

    LinearLayout mLlContainer;
    TextView mTvFullScreen;
    private TabSelectListener mTabSelectListener = null;
    //当前选择的index
    private String mSelectedChartName;
    private ColorStateList mColorStateList;
    private int mIndicatorColor;

    public KLineChartTabView(Context context) {
        super(context);
        init();
    }

    public KLineChartTabView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public KLineChartTabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_tab, null, false);
        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewUtil.Dp2Px(getContext(), 30));
        view.setLayoutParams(layoutParams);
        addView(view);
        mLlContainer = (LinearLayout) findViewById(R.id.ll_container);
        mTvFullScreen = (TextView) findViewById(R.id.tv_fullScreen);
        mTvFullScreen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) getContext();
                boolean isVertical = (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT);
                if (isVertical) {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

                } else {
                    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
                }
            }
        });
        mTvFullScreen.setSelected(true);
        if (mColorStateList != null) {
            mTvFullScreen.setTextColor(mColorStateList);
        }
    }

    @Override
    public void onClick(View v) {
        for (int i = 0; i < mLlContainer.getChildCount(); i++) {
            mLlContainer.getChildAt(i).setSelected(false);
        }
        v.setSelected(true);
        mTabSelectListener.onTabSelected(((TabView)v).getText());
    }

    public interface TabSelectListener {
        /**
         * 选择tab的位置序号
         *
         * @param mSelectedChartName
         */
        void onTabSelected(String mSelectedChartName);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        getParent().requestDisallowInterceptTouchEvent(true);
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 添加选项卡
     *
     * @param chartName 选项卡文字
     */
    public void addTab(String chartName) {
        TabView tabView = new TabView(getContext());
        tabView.setOnClickListener(this);
        tabView.setText(chartName);
        tabView.setTextColor(mColorStateList);
        tabView.setIndicatorColor(mIndicatorColor);
        mLlContainer.addView(tabView);
        //第一个默认选中
        if (mLlContainer.getChildCount() == 1) {
            tabView.setSelected(true);
            mSelectedChartName = chartName;
            onTabSelected(mSelectedChartName);
        }
    }

    /**
     * 设置选项卡监听
     *
     * @param listener TabSelectListener
     */
    public void setOnTabSelectListener(TabSelectListener listener) {
        this.mTabSelectListener = listener;
        //默认选中上一个位置
        if (mLlContainer.getChildCount() > 0 && mTabSelectListener != null) {
            mTabSelectListener.onTabSelected(mSelectedChartName);
        }
    }

    private void onTabSelected(String mSelectedChartName) {
        if (mTabSelectListener != null) {
            mTabSelectListener.onTabSelected(mSelectedChartName);
        }
    }

    public void setTextColor(ColorStateList color) {
        mColorStateList = color;
        for (int i = 0; i < mLlContainer.getChildCount(); i++) {
            TabView tabView = (TabView) mLlContainer.getChildAt(i);
            tabView.setTextColor(mColorStateList);
        }
        if (mColorStateList != null) {
            mTvFullScreen.setTextColor(mColorStateList);
        }
    }

    public void setIndicatorColor(int color) {
        mIndicatorColor = color;
        for (int i = 0; i < mLlContainer.getChildCount(); i++) {
            TabView tabView = (TabView) mLlContainer.getChildAt(i);
            tabView.setIndicatorColor(mIndicatorColor);
        }
    }

}
