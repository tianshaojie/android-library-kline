package cn.skyui.library.chart.kline.formatter;

import cn.skyui.library.chart.kline.base.IValueFormatter;

/**
 * Value格式化类
 */

public class ValueFormatter implements IValueFormatter {
    @Override
    public String format(float value) {
        return String.format("%.2f", value);
    }
}
