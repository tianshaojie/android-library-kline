package cn.skyui.library.chart.kline.data.model;

import cn.skyui.library.chart.kline.base.IChartData;

/**
 * 蜡烛图数据模型
 */
public class Candle implements IChartData {

    // 原始值
    /**
     * 日期
     */
    public String date;
    /**
     * 开盘价
     */
    public float open;
    /**
     * 最高价
     */
    public float high;
    /**
     * 最低价
     */
    public float low;
    /**
     * 收盘价
     */
    public float close;
    /**
     * 收盘价
     */
    public float volume;


    // 计算值
    /**
     * 五(月，日，时，分，5分等)均价
     */
    public float ma5Price;
    /**
     * 十(月，日，时，分，5分等)均价
     */
    public float ma10Price;
    /**
     * 二十(月，日，时，分，5分等)均价
     */
    public float ma20Price;

    @Override
    public float getMaxValue() {
        return Math.max(high, ma20Price);
    }

    @Override
    public float getMinValue() {
        return Math.min(ma20Price, low);
    }

}
