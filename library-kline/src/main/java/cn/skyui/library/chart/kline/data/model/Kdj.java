package cn.skyui.library.chart.kline.data.model;

import cn.skyui.library.chart.kline.base.IChartData;

/**
 * KDJ指标(随机指标)接口
 * @see <a href="https://baike.baidu.com/item/KDJ%E6%8C%87%E6%A0%87/6328421?fr=aladdin&fromid=3423560&fromtitle=kdj"/>相关说明</a>
 */
public class Kdj implements IChartData {

    /**
     * K值
     */
    public float k;

    /**
     * D值
     */
    public float d;

    /**
     * J值
     */
    public float j;

    @Override
    public float getMaxValue() {
        return Math.max(k, Math.max(d, j));
    }

    @Override
    public float getMinValue() {
        return Math.min(k, Math.min(d, j));
    }

}
