package cn.skyui.library.chart.kline.data.model;

import java.util.Date;

/**
 * 分时图实体接口
 */

public class MinuteLine {

    /**
     * @return 获取均价
     */
    public float avgPrice;

    /**
     * @return 获取成交价
     */
    public float price;

    /**
     * 该指标对应的时间
     */
    public Date time;

    /**
     * 成交量
     */
    public float volume;
}
