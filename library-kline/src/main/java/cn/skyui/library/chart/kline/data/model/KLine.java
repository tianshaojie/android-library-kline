package cn.skyui.library.chart.kline.data.model;

import cn.skyui.library.chart.kline.base.IChartData;
import cn.skyui.library.chart.kline.base.IValueFormatter;
import cn.skyui.library.chart.kline.data.ChartEnum;
import cn.skyui.library.chart.kline.formatter.BigValueFormatter;
import cn.skyui.library.chart.kline.formatter.ValueFormatter;

/**
 * k线实体接口
 */

public class KLine extends Candle {

    public Boll boll;
    public Kdj kdj;
    public Macd macd;
    public Rsi rsi;
    public Volume vol;

    public KLine() {
        this.boll = new Boll();
        this.kdj = new Kdj();
        this.macd = new Macd();
        this.rsi = new Rsi();
        this.vol = new Volume();
    }

    public IChartData getChildData(String type) {
        if (ChartEnum.CANDLE.name().equals(type)) {
            return this;
        } else if (ChartEnum.BOOL.name().equals(type)) {
            return boll;
        } else if (ChartEnum.KDJ.name().equals(type)) {
            return kdj;
        } else if (ChartEnum.MACD.name().equals(type)) {
            return macd;
        } else if (ChartEnum.RSI.name().equals(type)) {
            return rsi;
        } else if (ChartEnum.VOL.name().equals(type)) {
            return vol;
        }
        return this;
    }


    public static IValueFormatter getValueFormatter(String type) {
        if (ChartEnum.BOOL.name().equals(type)) {
            return new ValueFormatter();
        } else if (ChartEnum.KDJ.name().equals(type)) {
            return new ValueFormatter();
        } else if (ChartEnum.MACD.name().equals(type)) {
            return new ValueFormatter();
        } else if (ChartEnum.RSI.name().equals(type)) {
            return new ValueFormatter();
        } else if (ChartEnum.VOL.name().equals(type)) {
            return new BigValueFormatter();
        }
        return new ValueFormatter();
    }
}
