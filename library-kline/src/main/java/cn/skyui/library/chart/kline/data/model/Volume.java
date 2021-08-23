package cn.skyui.library.chart.kline.data.model;

import cn.skyui.library.chart.kline.base.IChartData;

/**
 * 成交量接口
 */

public class Volume implements IChartData {

    /**
     * 开盘价
     */
    public float openPrice;

    /**
     * 收盘价
     */
    public float closePrice;

    /**
     * 成交量
     */
    public float volume;

    /**
     * 五(月，日，时，分，5分等)均量
     */
    public float ma5Volume;

    /**
     * 十(月，日，时，分，5分等)均量
     */
    public float ma10Volume;

    @Override
    public float getMaxValue() {
        return Math.max(volume, Math.max(ma5Volume, ma10Volume));
    }

    @Override
    public float getMinValue() {
        return Math.min(volume, Math.min(ma5Volume, ma10Volume));
    }

}
