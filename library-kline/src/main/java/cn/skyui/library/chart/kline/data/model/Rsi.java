package cn.skyui.library.chart.kline.data.model;

import cn.skyui.library.chart.kline.base.IChartData;

/**
 * RSI指标接口
 * @see <a href="https://baike.baidu.com/item/RSI%E6%8C%87%E6%A0%87"/>相关说明</a>
 */

public class Rsi implements IChartData {

    /**
     * RSI1值
     */
    public float rsi1;
    /**
     * RSI2值
     */
    public float rsi2;
    /**
     * RSI3值
     */
    public float rsi3;

    @Override
    public float getMaxValue() {
        return Math.max(rsi1, Math.max(rsi2, rsi3));
    }

    @Override
    public float getMinValue() {
        return Math.min(rsi1, Math.min(rsi2, rsi3));
    }

}
