package com.demo;

import PanelInfor.PanelShow;
import domain.CsiInfo;
import domain.Ap;
//import domain.ShowTimeApp;
import util.Caches;
import util.CountDouble;
import util.DateUtils;
import util.Logger;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

import static util.FileUtils.ROOT_DIR;
import static util.FileUtils.write2txt;

public class Processor extends JFrame{
    // 滑窗大小
    private static final int WINDOW_SIZE = 50;
    // 阈值缓存文件名
    private static final String STATIC_OUTPUT_VALUE_TXT = ROOT_DIR + "Data/output.txt";
    // 每秒接收10个数据，这个值关系到会采集多少静默数据（因为采集时间是按照分钟设定的）
    private static final int COUNTS_PER_SECOND = 1;
    // 记录RSSI的文件名
    private static String sFileName = "F:/0609代码/MultiAP_test_2/" + "Data/" + DateUtils.getLaunchTime() + ".txt";
    // 回调接口

    /**
     * 预处理：加载之前可能有的阈值文件，并存储进当前阈值；
     * 判断是否有静态数据，如果有则计算阈值，没有则等待数据到600再计算
     */
    public static void preProcess() {
        Logger.i("进入preProcess函数");
    }
15998936499
    /**
     * 根据RSSI计算出阈值和入侵结果
     *
     * @param csi 得到的Csi的包
     */
    public static void processReceiveCsi(CsiInfo csi) {
        Logger.i("进入processReceiveCSI函数");
        Ap collector = Caches.getOrCreateAp(csi.get_mac());
        String pre_timestamp = Ap.getTimestampFromLink(collector);
        String timestamp = csi.get_timestamp();
        Ap.setTimestampFromLink(collector, timestamp);

        Logger.i("上一个时间戳：" + pre_timestamp);
        Logger.i("当前时间戳：" + timestamp);

        if (Ap.getThresholdFromLink(collector) != null && Ap.getThresholdFromLink(collector) != 0) {
            collector.setMinutesOfCollectingSilentData(0);          //采集过了就设置为不需要再采集了
        }
        if (collector.getMinutesOfCollectingSilentData() > 0) {//没有阈值，则添加进集合并保存文件,
            List<String> staticDataList = Ap.getStaticDataListFromLink(collector);//找到这个AP的静默数据list
            staticDataList.add(csi.toJsonString(csi));//将csi添加进list
            Ap.setStaticDataListFromLink(collector, staticDataList);
            Logger.i("静态数据长度:" + staticDataList.size());

            if (staticDataList.size() >= collector.getMinutesOfCollectingSilentData() * 300 * COUNTS_PER_SECOND) {//已经采集够了足够的数据
                Logger.i("开始阈值计算");
                double upBound = CountDouble.calUpbound(staticDataList);//根据静态数组计算阈值
                Ap.setThresholdFromLink(collector, upBound);
                collector.setMinutesOfCollectingSilentData(0);          //采集过了就设置为不需要再采集了
            }
        } else if (Ap.getThresholdFromLink(collector) != null) {
            List<String> apDataList = Ap.getDataListFromLink(collector);
            apDataList.add(csi.toJsonString(csi));
            Ap.setDataListFromLink(collector, apDataList);
            Logger.i("读取数据，数据长度为：" + apDataList.size());
            int count = 0;
            int flag = 0;
            if (apDataList.size() > WINDOW_SIZE) { //够了窗长个就计算并去掉第一个
                apDataList.remove(0);
                Ap.setDataListFromLink(collector, apDataList);

                Double thresholdValue = Ap.getThresholdFromLink(collector); //获得阈值
                int at = CountDouble.calDetection(thresholdValue, apDataList);//计算结果：0代表没人，1代表有人
                List<Integer> invadedList = Ap.getInvadedListFromLink(collector);
                invadedList.add(at);
                Ap.setInvadedListFromLink(collector,invadedList);
                String pre_second = pre_timestamp.substring(17,19);
                String second = timestamp.substring(17,19);
                Logger.i("上一个秒数为：" + pre_second);
                Logger.i("当前秒数为：" + second);
                if (invadedList.size() >= 200) {
                    Logger.i("时间变了");

                    int sum = 0;
                    String output;
                    for (int i = 0; i < invadedList.size(); i++) {
                        sum = sum + invadedList.get(i);
                    }
                    if (sum > invadedList.size() * 0.4) {
                        output = "true";
                        Logger.i("有人入侵!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                        try {
                            PanelShow.detection();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        output = "false";
                        Logger.i("无人————————————————————————————————————————————");
                    }
                    String format = output + " " + timestamp;
                    write2txt(STATIC_OUTPUT_VALUE_TXT, format);
                    Activator.textArea.append(format+"\r\n");

                    List<Integer> invadedList2 = new ArrayList<>();
                    Ap.setInvadedListFromLink(collector,invadedList2);
                }
            }
        } else if (Ap.getThresholdFromLink(collector) == null) {
            //没有阈值，且没有指定要采集静默数据
            Logger.i("请先采集静默数据");
            //默认设置先采集1分钟静默数据
            collector.setMinutesOfCollectingSilentData(1);
        } else {
            Logger.i("其他错误");
            Logger.i("阈值为" + Ap.getThresholdFromLink(collector));
        }
    }
}