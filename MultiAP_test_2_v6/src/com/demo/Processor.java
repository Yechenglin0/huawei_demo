package com.demo;

import PanelInfor.PanelShow;
import domain.CsiInfo;
import domain.Ap;
import util.Caches;
import util.CountDouble;
import util.DateUtils;
import util.Logger;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static util.FileUtils.ROOT_DIR;
import static util.FileUtils.write2txt;

public class Processor {
    // 滑窗大小
    private static final int WINDOW_SIZE = 50;
    // 阈值缓存文件名
    private static final String STATIC_OUTPUT_TXT = ROOT_DIR + "Data/output.txt";
    private static final String STATIC_FVALUE_TXT = ROOT_DIR + "Data/fvalue.txt";
    private static final String STATIC_TIME_TXT = ROOT_DIR + "Data/time.txt";

    // 每秒接收10个数据，这个值关系到会采集多少静默数据（因为采集时间是按照分钟设定的）
    private static final int COUNTS_PER_SECOND = 1;
    private static int count2 = 0;
    /**
     * 预处理：加载之前可能有的阈值文件，并存储进当前阈值；
     * 判断是否有静态数据，如果有则计算阈值，没有则等待数据到600再计算
     */
    public static void preProcess() {
        Logger.i("进入preProcess函数");
    }
    public static void scrollAndSetCursor(JTextArea ta){
        ta.setSelectionStart(ta.getText().length());
    }
    /**
     * 根据RSSI计算出阈值和入侵结果
     *
     * @param csi 得到的Csi的包
     */
    public static void processReceiveCsi(CsiInfo csi) {
        count2 = count2 + 1;
        Logger.i("接受到" + count2 + "个包");
        String format = csi.toString();
        write2txt(STATIC_OUTPUT_TXT, format);
        Logger.i("进入processReceiveCSI函数");
        long startTime = System.currentTimeMillis();    //获取开始时间

        Ap collector = Caches.getOrCreateAp(csi.get_mac());
        String pre_timestamp = Ap.getTimestampFromLink(collector);
        String timestamp = csi.get_timestamp();
        Ap.setTimestampFromLink(collector, timestamp);

        if (Ap.getThresholdFromLink(collector) != null && Ap.getThresholdFromLink(collector) != 0) {
            collector.setMinutesOfCollectingSilentData(0);          //采集过了就设置为不需要再采集了
        }
        if (collector.getMinutesOfCollectingSilentData() > 0) {//没有阈值，则添加进集合并保存文件,
            List<String> staticDataList = Ap.getStaticDataListFromLink(collector);//找到这个AP的静默数据list
            staticDataList.add(csi.toJsonString(csi));//将csi添加进list
            Ap.setStaticDataListFromLink(collector, staticDataList);
            Logger.i("静态数据长度:" + staticDataList.size());
            if (staticDataList.size() >= collector.getMinutesOfCollectingSilentData() * 300 * COUNTS_PER_SECOND) {//已经采集够了足够的数据
                Activator.textArea.append("已经收到" + staticDataList.size() + "个包，开始阈值计算" + '\n');

                double upBound = CountDouble.calUpbound(staticDataList);//根据静态数组计算阈值
                Ap.setThresholdFromLink(collector, upBound);
                Activator.textArea.append("阈值结果为：" + upBound + '\n');
                collector.setMinutesOfCollectingSilentData(0);          //采集过了就设置为不需要再采集了
            }
        } else if (Ap.getThresholdFromLink(collector) != null) {
            List<String> apDataList = Ap.getDataListFromLink(collector);
            Double thresholdValue   = Ap.getThresholdFromLink(collector); //获得阈值

            apDataList.add(csi.toJsonString(csi));//将csi添加进list
            Ap.setDataListFromLink(collector, apDataList);
            Logger.i("读取数据，数据长度为：" + apDataList.size());

            if (apDataList.size() == WINDOW_SIZE) { //第一个窗
                Ap.setDataListFromLink(collector, apDataList);
                Logger.i("第一个窗");
                double[][] tempMatrix = CountDouble.get_temp_matrix_1st_win(apDataList);//计算结果：0代表没人，1代表有人
                Ap.setTempMatrixFromLink(collector, tempMatrix);
            }
            if (apDataList.size() > WINDOW_SIZE) { //够了窗长个就计算并去掉第一个
                apDataList.remove(0);
                Ap.setDataListFromLink(collector, apDataList);

                double[][] tempMatrix = Ap.getTempMatrixFromLink(collector);
                tempMatrix = CountDouble.get_temp_matrix_next_win(apDataList, tempMatrix);
                Ap.setTempMatrixFromLink(collector, tempMatrix);
                double fvalue = CountDouble.cal_next_win(tempMatrix);//简便计算
                int at = fvalue < thresholdValue * 0.975 ? 1 : 0;//结果1代表有人，0代表没人

                String format3 = String.valueOf(fvalue);
                write2txt(STATIC_FVALUE_TXT, format3);

                List<Integer> invadedList = Ap.getInvadedListFromLink(collector);
                invadedList.add(at);
                Ap.setInvadedListFromLink(collector,invadedList);
                if (invadedList.size() >= 100) {

                    int sum = 0;
                    String output;
                    for (int i = 0; i < invadedList.size(); i++) {
                        sum = sum + invadedList.get(i);
                    }
                    if (sum > invadedList.size() * 0.4) {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                        output = "有人入侵！！！" + df.format(new Date()) + '\n';
                        try {
                            PanelShow.detection();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                        output = "安全！        " + df.format(new Date()) + '\n';
                    }
                    Activator.textArea.append(output);
                    Activator.textArea.setSelectionStart(Activator.textArea.getText().length());
                    List<Integer> invadedList2 = new ArrayList<>();
                    Ap.setInvadedListFromLink(collector,invadedList2);
                }
            }
        } else if (Ap.getThresholdFromLink(collector) == null) {
            Logger.i("请先采集静默数据");//没有阈值，且没有指定要采集静默数据
            collector.setMinutesOfCollectingSilentData(1);//默认设置先采集1分钟静默数据
        } else {
            Logger.i("其他错误");
            Logger.i("阈值为" + Ap.getThresholdFromLink(collector));
        }


        long endTime = System.currentTimeMillis();    //获取结束时间
        System.out.println("程序计算运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
        String format3 = String.valueOf(endTime - startTime);
        write2txt(STATIC_TIME_TXT, format3);
    }
}