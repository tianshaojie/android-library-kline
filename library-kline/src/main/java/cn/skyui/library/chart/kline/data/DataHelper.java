package cn.skyui.library.chart.kline.data;

import cn.skyui.library.chart.kline.data.model.KLine;

import java.util.List;

/**
 * 数据辅助类 计算macd rsi等
 */

public class DataHelper {

    /**
     * 计算RSI
     *
     * @param datas
     */
    static void calculateRSI(List<KLine> datas) {
        float rsi1 = 0;
        float rsi2 = 0;
        float rsi3 = 0;
        float rsi1ABSEma = 0;
        float rsi2ABSEma = 0;
        float rsi3ABSEma = 0;
        float rsi1MaxEma = 0;
        float rsi2MaxEma = 0;
        float rsi3MaxEma = 0;
        for (int i = 0; i < datas.size(); i++) {
            KLine kline = datas.get(i);
            final float closePrice = kline.close;
            if (i == 0) {
                rsi1 = 0;
                rsi2 = 0;
                rsi3 = 0;
                rsi1ABSEma = 0;
                rsi2ABSEma = 0;
                rsi3ABSEma = 0;
                rsi1MaxEma = 0;
                rsi2MaxEma = 0;
                rsi3MaxEma = 0;
            } else {
                float Rmax = Math.max(0, closePrice - datas.get(i - 1).close);
                float RAbs = Math.abs(closePrice - datas.get(i - 1).close);
                rsi1MaxEma = (Rmax + (6f - 1) * rsi1MaxEma) / 6f;
                rsi1ABSEma = (RAbs + (6f - 1) * rsi1ABSEma) / 6f;

                rsi2MaxEma = (Rmax + (12f - 1) * rsi2MaxEma) / 12f;
                rsi2ABSEma = (RAbs + (12f - 1) * rsi2ABSEma) / 12f;

                rsi3MaxEma = (Rmax + (24f - 1) * rsi3MaxEma) / 24f;
                rsi3ABSEma = (RAbs + (24f - 1) * rsi3ABSEma) / 24f;

                rsi1 = (rsi1MaxEma / rsi1ABSEma) * 100;
                rsi2 = (rsi2MaxEma / rsi2ABSEma) * 100;
                rsi3 = (rsi3MaxEma / rsi3ABSEma) * 100;
            }
            kline.rsi.rsi1 = rsi1;
            kline.rsi.rsi2 = rsi2;
            kline.rsi.rsi3 = rsi3;
        }
    }

    /**
     * 计算kdj
     *
     * @param datas
     */
    static void calculateKDJ(List<KLine> datas) {
        float k = 0;
        float d = 0;

        for (int i = 0; i < datas.size(); i++) {
            KLine kline = datas.get(i);
            final float closePrice = kline.close;
            int startIndex = i - 8;
            if (startIndex < 0) {
                startIndex = 0;
            }
            float max9 = Float.MIN_VALUE;
            float min9 = Float.MAX_VALUE;
            for (int index = startIndex; index <= i; index++) {
                max9 = Math.max(max9, datas.get(index).high);
                min9 = Math.min(min9, datas.get(index).low);

            }
            float rsv = 100f * (closePrice - min9) / (max9 - min9);
            if (i == 0) {
                k = rsv;
                d = rsv;
            } else {
                k = (rsv + 2f * k) / 3f;
                d = (k + 2f * d) / 3f;
            }
            kline.kdj.k = k;
            kline.kdj.d = d;
            kline.kdj.j = 3f * k - 2 * d;
        }

    }

    /**
     * 计算macd
     *
     * @param datas
     */
    static void calculateMACD(List<KLine> datas) {
        float ema12 = 0;
        float ema26 = 0;
        float dif = 0;
        float dea = 0;
        float macd = 0;

        for (int i = 0; i < datas.size(); i++) {
            KLine kline = datas.get(i);
            final float closePrice = kline.close;
            if (i == 0) {
                ema12 = closePrice;
                ema26 = closePrice;
            } else {
//                EMA（12） = 前一日EMA（12） X 11/13 + 今日收盘价 X 2/13
//                EMA（26） = 前一日EMA（26） X 25/27 + 今日收盘价 X 2/27
                ema12 = ema12 * 11f / 13f + closePrice * 2f / 13f;
                ema26 = ema26 * 25f / 27f + closePrice * 2f / 27f;
            }
//            DIF = EMA（12） - EMA（26） 。
//            今日DEA = （前一日DEA X 8/10 + 今日DIF X 2/10）
//            用（DIF-DEA）*2即为MACD柱状图。
            dif = ema12 - ema26;
            dea = dea * 8f / 10f + dif * 2f / 10f;
            macd = (dif - dea) * 2f;
            kline.macd.dif = dif;
            kline.macd.dea = dea;
            kline.macd.macd = macd;
        }

    }

    /**
     * 计算 BOLL 需要在计算ma之后进行
     *
     * @param datas
     */
    static void calculateBOLL(List<KLine> datas) {
        for (int i = 0; i < datas.size(); i++) {
            KLine kline = datas.get(i);
            final float closePrice = kline.close;
            if (i == 0) {
                kline.boll.mb = closePrice;
                kline.boll.up = Float.NaN;
                kline.boll.dn = Float.NaN;
            } else {
                int n = 20;
                if (i < 20) {
                    n = i + 1;
                }
                float md = 0;
                for (int j = i - n + 1; j <= i; j++) {
                    float c = datas.get(j).close;
                    float m = kline.ma20Price;
                    float value = c - m;
                    md += value * value;
                }
                md = md / (n - 1);
                md = (float) Math.sqrt(md);
                kline.boll.mb = kline.ma20Price;
                kline.boll.up = kline.boll.mb + 2f * md;
                kline.boll.dn = kline.boll.mb - 2f * md;
            }
        }

    }

    /**
     * 计算ma
     *
     * @param datas
     */
    static void calculateMA(List<KLine> datas) {
        float ma5 = 0;
        float ma10 = 0;
        float ma20 = 0;

        for (int i = 0; i < datas.size(); i++) {
            KLine point = datas.get(i);
            final float closePrice = point.close;

            ma5 += closePrice;
            ma10 += closePrice;
            ma20 += closePrice;
            if (i >= 5) {
                ma5 -= datas.get(i - 5).close;
                point.ma5Price = ma5 / 5f;
            } else {
                point.ma5Price = ma5 / (i + 1f);
            }
            if (i >= 10) {
                ma10 -= datas.get(i - 10).close;
                point.ma10Price = ma10 / 10f;
            } else {
                point.ma10Price = ma10 / (i + 1f);
            }
            if (i >= 20) {
                ma20 -= datas.get(i - 20).close;
                point.ma20Price = ma20 / 20f;
            } else {
                point.ma20Price = ma20 / (i + 1f);
            }
        }
    }

    /**
     * 计算MA BOLL RSI KDJ MACD
     *
     * @param datas
     */
    public static void calculate(List<KLine> datas) {
        calculateMA(datas);
        calculateMACD(datas);
        calculateBOLL(datas);
        calculateRSI(datas);
        calculateKDJ(datas);
        calculateVolumeMA(datas);
    }

    private static void calculateVolumeMA(List<KLine> entries) {
        float volumeMa5 = 0;
        float volumeMa10 = 0;

        for (int i = 0; i < entries.size(); i++) {
            KLine kline = entries.get(i);

            volumeMa5 += kline.volume;
            volumeMa10 += kline.volume;

            // 冗余3个字段
            kline.vol.openPrice = kline.open;
            kline.vol.closePrice = kline.close;
            kline.vol.volume = kline.volume;

            if (i >= 5) {

                volumeMa5 -= entries.get(i - 5).volume;
                kline.vol.ma5Volume = (volumeMa5 / 5f);
            } else {

                kline.vol.ma5Volume = (volumeMa5 / (i + 1f));
            }

            if (i >= 10) {
                volumeMa10 -= entries.get(i - 10).volume;
                kline.vol.ma10Volume = (volumeMa10 / 5f);
            } else {
                kline.vol.ma10Volume = (volumeMa10 / (i + 1f));
            }
        }
    }
}
