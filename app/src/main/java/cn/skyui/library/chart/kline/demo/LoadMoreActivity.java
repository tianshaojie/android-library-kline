package cn.skyui.library.chart.kline.demo;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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

public class LoadMoreActivity extends AppCompatActivity implements KLineChartView.KChartRefreshListener {

    @BindView(R.id.title_view)
    RelativeLayout mTitleView;
    @BindView(R.id.tv_price)
    TextView mTvPrice;
    @BindView(R.id.tv_percent)
    TextView mTvPercent;
    @BindView(R.id.ll_status)
    LinearLayout mLlStatus;
    @BindView(R.id.kchart_view)
    KLineChartView mKChartView;
    private KLineChartAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_light);
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
        mKChartView.setRefreshListener(this);
        onLoadMoreBegin(mKChartView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mLlStatus.setVisibility(View.GONE);
            mKChartView.setGridRows(3);
            mKChartView.setGridColumns(8);
        } else if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLlStatus.setVisibility(View.VISIBLE);
            mKChartView.setGridRows(4);
            mKChartView.setGridColumns(4);
        }
    }

    @Override
    public void onLoadMoreBegin(KLineChartView chart) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final List<KLine> data = DataRequest.getData(LoadMoreActivity.this, mAdapter.getCount(), 500);
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
                            mKChartView.startAnimation();
                        }
                        mAdapter.addFooterData(data);
                        //加载完成，还有更多数据
                        if (data.size() > 0) {
                            mKChartView.refreshComplete();
                        }
                        //加载完成，没有更多数据
                        else {
                            mKChartView.refreshEnd();
                        }
                    }
                });
            }
        }).start();
    }
}
