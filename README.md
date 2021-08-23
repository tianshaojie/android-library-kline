# KLineChartView
KLine Chart for Android ；股票K线图

## 配置及使用

### xml简单配置
```xml
<cn.skyui.library.chart.kline.view.KLineChartView
        android:id="@+id/kchart_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
    </cn.skyui.library.chart.kline.view.KLineChartView>
```

### xml中配置示例

```xml
<cn.skyui.library.chart.kline.view.KLineChartView
        android:id="@+id/kchart_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:kc_text_size="@dimen/chart_text_size"
        app:kc_text_color="#787878"
        app:kc_line_width="@dimen/chart_line_width"
        app:kc_background_color="#FFF"
        app:kc_selected_line_color="#B1B2B6"
        app:kc_selected_line_width="1dp"
        app:kc_grid_line_color="#d0d0d0"
        app:kc_grid_line_width="0.5dp"
        app:kc_point_width="6dp"
        app:kc_macd_width="4dp"
        app:kc_dif_color="@color/chart_ma5"
        app:kc_dea_color="@color/chart_ma10"
        app:kc_macd_color="@color/chart_ma20"
        app:kc_k_color="@color/chart_ma5"
        app:kc_d_color="@color/chart_ma10"
        app:kc_j_color="@color/chart_ma20"
        app:kc_rsi1_color="@color/chart_ma5"
        app:kc_rsi2_color="@color/chart_ma10"
        app:kc_ris3_color="@color/chart_ma20"
        app:kc_up_color="@color/chart_ma5"
        app:kc_mb_color="@color/chart_ma10"
        app:kc_dn_color="@color/chart_ma20"
        app:kc_ma5_color="@color/chart_ma5"
        app:kc_ma10_color="@color/chart_ma10"
        app:kc_ma20_color="@color/chart_ma20"
        app:kc_candle_line_width="1dp"
        app:kc_candle_width="4dp"
        app:kc_selector_background_color="#c8d0d0d0"
        app:kc_selector_text_size="@dimen/chart_selector_text_size"
        app:kc_tab_text_color="@color/tab_light_text_color_selector"
        app:kc_tab_indicator_color="#4473b1"
        app:kc_tab_background_color="#fff"
        app:kc_candle_solid="false">
    </cn.skyui.library.chart.kline.view.KLineChartView>
```


### 加载数据

```java
mKChartView.showLoading();
new Thread(new Runnable() {
    @Override
    public void run() {
        final List<KLine> data = DataRequest.getALL(ExampleActivity.this);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //追加数据
                mAdapter.addFooterData(data);
                //开始动画
                mKChartView.startAnimation();
                //刷新完成
                mKChartView.refreshEnd();
            }
        });
    }
}).start();
```

License
-------

    Copyright 2021 tianshaojie

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.