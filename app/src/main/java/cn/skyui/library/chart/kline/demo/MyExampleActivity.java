package cn.skyui.library.chart.kline.demo;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.skyui.library.chart.kline.adapter.KLineChartAdapter;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.formatter.DateFormatter;
import cn.skyui.library.chart.kline.view.BaseKLineChartView;
import cn.skyui.library.chart.kline.view.KLineChartView;
import cn.skyui.library.chart.kline.view.KLineView;

public class MyExampleActivity extends AppCompatActivity {


    private KLineView klineView;
    private KLineChartAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_example);
        klineView = findViewById(R.id.kline_view);
        initView();
        initData();
    }

    private void initView() {
        mAdapter = new KLineChartAdapter();
    }

    private void initData() {
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
