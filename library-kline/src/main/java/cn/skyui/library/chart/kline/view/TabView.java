package cn.skyui.library.chart.kline.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.skyui.library.chart.kline.R;

/**
 * TabView
 */

public class TabView extends RelativeLayout {

    private TextView mTextView;
    private View mIndicator;

    public TabView(Context context) {
        this(context, null);
    }

    public TabView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TabView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        addView(LayoutInflater.from(getContext()).inflate(R.layout.item_tab, null));
        mTextView = (TextView) findViewById(R.id.tab_text);
        mIndicator = findViewById(R.id.indicator);
    }

    public void setTextColor(ColorStateList color) {
        if (color != null) {
            mTextView.setTextColor(color);
        }
    }

    public void setText(String text) {
        mTextView.setText(text);
    }

    public String getText() {
        return mTextView.getText().toString();
    }

    public void setIndicatorColor(int color) {
        mIndicator.setBackgroundColor(color);
    }

    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        mIndicator.setVisibility(selected ? VISIBLE : INVISIBLE);
    }
}
