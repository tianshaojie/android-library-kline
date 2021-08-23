package cn.skyui.library.chart.kline.adapter;

import android.database.DataSetObservable;
import android.database.DataSetObserver;

import cn.skyui.library.chart.kline.base.IAdapter;

/**
 * k线图的数据适配器
 */

public abstract class BaseKLineChartAdapter implements IAdapter {

    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    public void notifyDataSetChanged() {
        if (getCount() > 0) {
            mDataSetObservable.notifyChanged();
        } else {
            mDataSetObservable.notifyInvalidated();
        }
    }


    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }
}
