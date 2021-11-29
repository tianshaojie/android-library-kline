package cn.skyui.library.chart.kline.adapter;

import cn.skyui.library.chart.kline.data.model.KLine;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 数据适配器
 */

public class KLineChartAdapter extends BaseKLineChartAdapter {

    private List<KLine> datas = new ArrayList<>();

    public KLineChartAdapter() {

    }

    @Override
    public int getCount() {
        return datas.size();
    }

    @Override
    public KLine getItem(int position) {
        return datas.get(position);
    }

    @Override
    public List<KLine> getItems() {
        return datas;
    }

    @Override
    public Date getDate(int position) {
        try {
            String s = datas.get(position).date;
            String[] split = s.split("/");
            Date date = new Date();
            date.setYear(Integer.parseInt(split[0]) - 1900);
            date.setMonth(Integer.parseInt(split[1]) - 1);
            date.setDate(Integer.parseInt(split[2]));
            return date;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 向头部添加数据
     */
    public void addHeaderData(List<KLine> data) {
        if (data != null && !data.isEmpty()) {
            datas.addAll(data);
            notifyDataSetChanged();
        }
    }

    /**
     * 向尾部添加数据
     */
    public void addFooterData(List<KLine> data) {
        if (data != null && !data.isEmpty()) {
            datas.addAll(0, data);
            notifyDataSetChanged();
        }
    }

    /**
     * 改变某个点的值
     * @param position 索引值
     */
    public void changeItem(int position,KLine data)
    {
        datas.set(position,data);
        notifyDataSetChanged();
    }

}
