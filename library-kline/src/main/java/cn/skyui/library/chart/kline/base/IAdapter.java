package cn.skyui.library.chart.kline.base;

import android.database.DataSetObserver;

import cn.skyui.library.chart.kline.data.model.KLine;

import java.util.Date;
import java.util.List;


/**
 * 数据适配器
 */

public interface IAdapter {
    /**
     * 获取点的数目
     *
     * @return
     */
    int getCount();

    /**
     * 通过序号获取item
     *
     * @param position 对应的序号
     * @return 数据实体
     */
    KLine getItem(int position);

    List<KLine> getItems();

    /**
     * 通过序号获取时间
     *
     * @param position
     * @return
     */
    Date getDate(int position);

    /**
     * 注册一个数据观察者
     *
     * @param observer 数据观察者
     */
    void registerDataSetObserver(DataSetObserver observer);

    /**
     * 移除一个数据观察者
     *
     * @param observer 数据观察者
     */
    void unregisterDataSetObserver(DataSetObserver observer);

    /**
     * 当数据发生变化时调用
     */
    void notifyDataSetChanged();
}
