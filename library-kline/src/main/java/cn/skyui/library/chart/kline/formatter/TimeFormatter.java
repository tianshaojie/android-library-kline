package cn.skyui.library.chart.kline.formatter;

import cn.skyui.library.chart.kline.base.IDateTimeFormatter;
import cn.skyui.library.chart.kline.utils.DateUtil;

import java.util.Date;

/**
 * 时间格式化器
 */

public class TimeFormatter implements IDateTimeFormatter {
    @Override
    public String format(Date date) {
        if (date == null) {
            return "";
        }
        return DateUtil.shortTimeFormat.format(date);
    }
}
