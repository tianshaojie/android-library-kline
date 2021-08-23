package cn.skyui.library.chart.kline.data.model;

import cn.skyui.library.chart.kline.base.IChartData;

/**
 * MACD指标(指数平滑移动平均线)接口
 * @see <a href="https://baike.baidu.com/item/MACD指标"/>相关说明</a>
 */

public class Macd implements IChartData {


    /**
     * DEA值
     */
    public float dea;

    /**
     * DIF值
     */
    public float dif;

    /**
     * MACD值
     */
    public float macd;

    @Override
    public float getMaxValue() {
        return Math.max(macd, Math.max(dea, dif));
    }

    @Override
    public float getMinValue() {
        return Math.min(macd, Math.min(dea, dif));
    }

}
