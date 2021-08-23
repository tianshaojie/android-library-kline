package cn.skyui.library.chart.kline.demo;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;

import cn.skyui.library.chart.kline.demo.R;

import cn.skyui.library.chart.kline.demo.DataRequest;
import cn.skyui.library.chart.kline.view.MinuteChartView;
import cn.skyui.library.chart.kline.data.model.MinuteLine;
import cn.skyui.library.chart.kline.utils.DateUtil;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MinuteChartActivity extends AppCompatActivity {


    @BindView(R.id.minuteChartView)
    MinuteChartView mMinuteChartView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_minute_chart);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window window = getWindow();
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        ButterKnife.bind(this);
        initView();
        initData();
    }

    private void initView() {
    }

    private void initData() {
        try {
            //整体开始时间
            Date startTime = DateUtil.shortTimeFormat.parse("09:30");
            //整体的结束时间
            Date endTime = DateUtil.shortTimeFormat.parse("15:00");
            //休息开始时间
            Date firstEndTime = DateUtil.shortTimeFormat.parse("11:30");
            //休息结束时间
            Date secondStartTime = DateUtil.shortTimeFormat.parse("13:00");
            //获取随机生成的数据
            List<MinuteLine> minuteData =
                    DataRequest.getMinuteData(startTime,
                            endTime,
                            firstEndTime,
                            secondStartTime);
            mMinuteChartView.initData(minuteData,
                    startTime,
                    endTime,
                    firstEndTime,
                    secondStartTime,
                    (float) (minuteData.get(0).price - 0.5 + Math.random()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
