package cn.skyui.library.chart.kline.data.model;

import cn.skyui.library.chart.kline.base.IChartData;

/**
 * 布林线指标接口
 * @see <a href="https://baike.baidu.com/item/%E5%B8%83%E6%9E%97%E7%BA%BF%E6%8C%87%E6%A0%87/3325894"/>相关说明</a>
 */

public class Boll implements IChartData {

    /**
     * 上轨线
     */
    public float up;

    /**
     * 中轨线
     */
    public float mb;

    /**
     * 下轨线
     */
    public float dn;

    @Override
    public float getMaxValue() {
        if (Float.isNaN(up)) {
            return mb;
        }
        return up;
    }

    @Override
    public float getMinValue() {
        if (Float.isNaN(dn)) {
            return mb;
        }
        return dn;
    }
}
