package cn.skyui.library.chart.kline.demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

import cn.skyui.library.chart.kline.adapter.KLineChartAdapter;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.view.KLineViewV2;

public class MyExampleActivity extends AppCompatActivity {


    private KLineViewV2 klineViewV2;
    private KLineChartAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_example);
        initView();
        initData();
    }

    private void initView() {
        mAdapter = new KLineChartAdapter();
        klineViewV2 = findViewById(R.id.kline_view);
        klineViewV2.setAdapter(mAdapter);
        klineViewV2.setRefreshListener(new KLineViewV2.KChartRefreshListener() {
            @Override
            public void onLoadMore(KLineViewV2 chart) {
                onLoadMore(chart);
            }
        });
    }

    private void initData() {
        onLoadMore(klineViewV2);
    }

    public void onLoadMore(KLineViewV2 chart) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<KLine> data = DataRequest.getData(MyExampleActivity.this, mAdapter.getCount(), 300);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (!data.isEmpty()) {
                    Log.i("onLoadMoreBegin", "start:" + data.get(0).date + " stop:" + data.get(data.size() - 1).date);
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //第一次加载时开始动画
                        if (mAdapter.getCount() == 0) {
//                            klineViewV2.startAnimation();
                        }
                        mAdapter.addFooterData(data);
                        //加载完成，还有更多数据
                        if (data.size() > 0) {
//                            klineViewV2.refreshComplete();
                        }
                        //加载完成，没有更多数据
                        else {
//                            klineViewV2.refreshEnd();
                        }
                    }
                });
            }
        }).start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
