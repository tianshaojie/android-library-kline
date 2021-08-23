package cn.skyui.library.chart.kline.demo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import cn.skyui.library.chart.kline.data.DataHelper;
import cn.skyui.library.chart.kline.data.model.KLine;
import cn.skyui.library.chart.kline.data.model.MinuteLine;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * 模拟网络请求
 */
public class DataRequest {
    private static List<KLine> datas = null;
    private static Random random = new Random();

    public static String getStringFromAssert(Context context, String fileName) {
        try {
            InputStream in = context.getResources().getAssets().open(fileName);
            int length = in.available();
            byte[] buffer = new byte[length];
            in.read(buffer);
            return new String(buffer, 0, buffer.length, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static List<KLine> getALL(Context context) {
        if (datas == null) {
            final List<KLine> data = new Gson().fromJson(getStringFromAssert(context, "ibm-format.json"), new TypeToken<List<KLine>>() {}.getType());
            DataHelper.calculate(data);
            datas = data;
        }
        return datas;
    }

    /**
     * 分页查询
     *
     * @param context
     * @param offset  开始的索引
     * @param size    每次查询的条数
     */
    public static List<KLine> getData(Context context, int offset, int size) {
        List<KLine> all = getALL(context);
        List<KLine> data = new ArrayList<>();
        int start = Math.max(0, all.size() - 1 - offset - size);
        int stop = Math.min(all.size(), all.size() - offset);
        for (int i = start; i < stop; i++) {
            data.add(all.get(i));
        }
        return data;
    }

    /**
     * 随机生成分时数据
     */
    public static List<MinuteLine>
    getMinuteData(@NonNull Date startTime,
                  @NonNull Date endTime,
                  @Nullable Date firstEndTime,
                  @Nullable Date secondStartTime) {
        List<MinuteLine> list = new ArrayList<>();
        long startDate=startTime.getTime();
        if(firstEndTime==null&&secondStartTime==null) {
            while (startDate <= endTime.getTime()) {
                MinuteLine data = new MinuteLine();
                data.time = new Date(startDate);
                startDate += 60000;
                list.add(data);
            }
        }
        else {
            while (startDate <= firstEndTime.getTime()) {
                MinuteLine data = new MinuteLine();
                data.time = new Date(startDate);
                startDate += 60000;
                list.add(data);
            }
            startDate=secondStartTime.getTime();
            while (startDate <= endTime.getTime()) {
                MinuteLine data = new MinuteLine();
                data.time = new Date(startDate);
                startDate += 60000;
                list.add(data);
            }
        }
        randomLine(list);
        randomVolume(list);
        float sum = 0;
        for (int i = 0; i < list.size(); i++) {
            MinuteLine data = list.get(i);
            sum += data.price;
            data.avgPrice = 1f * sum / (i + 1);
        }
        return list;
    }

    private static void randomVolume(List<MinuteLine> list){
        for(MinuteLine data:list){
            data.volume= (int) (Math.random()*Math.random()*Math.random()*Math.random()*10000000);
        }
    }

    /**
     * 生成随机曲线
     */
    private static void randomLine(List<MinuteLine> list) {
        float STEP_MAX = 0.9f;
        float STEP_CHANGE = 1f;
        float HEIGHT_MAX = 200;

        float height = (float) (Math.random() * HEIGHT_MAX);
        float slope = (float) ((Math.random() * STEP_MAX) * 2 - STEP_MAX);

        for (int x = 0; x < list.size(); x++) {
            height += slope;
            slope += (Math.random() * STEP_CHANGE) * 2 - STEP_CHANGE;

            if (slope > STEP_MAX) {
                slope = STEP_MAX;
            }
            if (slope < -STEP_MAX) {
                slope = -STEP_MAX;
            }

            if (height > HEIGHT_MAX) {
                height = HEIGHT_MAX;
                slope *= -1;
            }
            if (height < 0) {
                height = 0;
                slope *= -1;
            }

            list.get(x).price = height + 1000;
        }
    }
}


