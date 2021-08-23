package cn.skyui.library.chart.kline.demo;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.skyui.library.chart.kline.demo.R;

import cn.skyui.library.chart.kline.view.BaseKLineChartView;
import cn.skyui.library.chart.kline.adapter.KLineChartAdapter;
import cn.skyui.library.chart.kline.view.KLineChartView;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.formatter.DateFormatter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ExampleActivity extends AppCompatActivity {


    @BindView(R.id.kchart_view)
    KLineChartView mKChartView;
    private KLineChartAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int type = getIntent().getIntExtra("type", 0);
        if (type == 0) {
            setContentView(R.layout.activity_example);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Window window = getWindow();
                window.setFlags(
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            }
        } else {
            setContentView(R.layout.activity_example_light);
        }
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
        mAdapter = new KLineChartAdapter();
        mKChartView.setAdapter(mAdapter);
        mKChartView.setDateTimeFormatter(new DateFormatter());
        mKChartView.setGridRows(4);
        mKChartView.setGridColumns(4);
        mKChartView.setOnSelectedChangedListener(new BaseKLineChartView.OnSelectedChangedListener() {
            @Override
            public void onSelectedChanged(BaseKLineChartView view, Object point, int index) {
                KLine data = (KLine) point;
                Log.i("onSelectedChanged", "index:" + index + " closePrice:" + data.close);
            }
        });
    }

    private void initData() {
        mKChartView.showLoading();
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<KLine> data = DataRequest.getALL(ExampleActivity.this);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addFooterData(data);
                        mKChartView.startAnimation();
                        mKChartView.refreshEnd();
                    }
                });
            }
        }).start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mKChartView.setGridRows(3);
            mKChartView.setGridColumns(8);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mKChartView.setGridRows(4);
            mKChartView.setGridColumns(4);
        }
    }
}
