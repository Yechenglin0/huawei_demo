package com.demo;

import domain.CsiInfo;
import domain.Ap;
import domain.InvadedList;
import util.Caches;
import util.CountDouble;
import util.DateUtils;
import util.Logger;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static util.FileUtils.*;

public class Processor {
    // 滑窗大小
    private static final int WINDOW_SIZE = 50;
    // 阈值缓存文件名

    // 每秒接收10个数据，这个值关系到会采集多少静默数据（因为采集时间是按照分钟设定的）
    private static final int COUNTS_PER_SECOND = 1;
    private static int count2 = 0;
    public static String ROOT_DIR = "./Data/";//必须以/结尾，否则出错

    /**
     * 预处理：加载之前可能有的阈值文件，并存储进当前阈值；
     * 判断是否有静态数据，如果有则计算阈值，没有则等待数据到600再计算
     */
    public static void preProcess() {
        Logger.i("进入preProcess函数");
    }
    /**
     * 根据RSSI计算出阈值和入侵结果
     *
     * @param csi 得到的Csi的包
     */
    public static void processReceiveCsi(CsiInfo csi, int data_num, int threshold_num, int save_txt) {
        InvadedList invadedList = Caches.getOrCreateInvadedList();

        String mac = csi.get_mac();
        if (1 == save_txt) {//存储原始数据
            String format = csi.toString();
            write2txt( ROOT_DIR + mac + STATIC_DATA_TXT, format);
        }

        Ap collector = Caches.getOrCreateAp(mac);
        if (Ap.getThresholdFromLink(collector) != null && Ap.getThresholdFromLink(collector) != 0) {
            collector.setMinutesOfCollectingSilentData(0);          //采集过了就设置为不需要再采集了
        }
        if (collector.getMinutesOfCollectingSilentData() > 0) {//没有阈值，则添加进集合并保存文件,
            List<String> staticDataList = Ap.getStaticDataListFromLink(collector);//找到这个AP的静默数据list
            staticDataList.add(csi.toJsonString(csi));//将csi添加进list
            Ap.setStaticDataListFromLink(collector, staticDataList);
            Logger.i("静态数据长度:" + staticDataList.size());
            if (staticDataList.size() >= collector.getMinutesOfCollectingSilentData() * threshold_num * COUNTS_PER_SECOND) {//已经采集够了足够的数据

                Activator.textArea.append("AP：" + mac + " 已经收到" + staticDataList.size() + "个包，开始阈值计算" + '\n');
                double upBound = CountDouble.calUpbound(staticDataList);//根据静态数组计算阈值
                Ap.setThresholdFromLink(collector, upBound);
                Activator.textArea.append("AP：" + mac + " 阈值结果为：" + upBound + '\n');
                write2txt(ROOT_DIR + mac + STATIC_THRESHOLD_TXT, String.valueOf(upBound));

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

                if (1 == save_txt) {//记录最大特征值变化情况
                    write2txt(ROOT_DIR + mac + STATIC_FVALUE_TXT, String.valueOf(fvalue));
                }
                count2 = count2 + 1;
                Logger.i("接受到" + count2 + "个包");

                //开始检测是否有人入侵
                List<Integer> list = InvadedList.getInvadedListFromLink(invadedList);//获得当前的结果列表
                list.add(at);//将每个包的结果存入列表中
                InvadedList.setInvadedListFromLink(invadedList,list);
                int ap_num = InvadedList.getApnumFromLink(invadedList);
                if (list.size() >= data_num) {//单AP条件下，每 data_num个包判断一次
                    int sum = 0;
                    int flag = -1;
                    String output;
                    for (int i = 0; i < list.size(); i++) { sum = sum + list.get(i); }
                    if (sum > list.size() * 0.4) {
                        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                        output = "有人入侵！！！" + df2.format(new Date()) + '\n';
                        flag = 1;
                    } else {
                        SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                        output = "安全！        " + df2.format(new Date()) + '\n';
                        flag = 0;
                    }
                    Activator.textArea.append("当前Ap个数：" + ap_num + "\n");
                    Activator.textArea.append(output);
                    Activator.textArea.setSelectionStart(Activator.textArea.getText().length());
                    List<Integer> invadedList2 = new ArrayList<>();

                    InvadedList.setInvadedListFromLink(invadedList, invadedList2);//清空列表
                    write2txt(ROOT_DIR + mac + STATIC_OUTPUT_TXT, String.valueOf(flag) + String.valueOf(count2) );
                }
            }
        } else if (Ap.getThresholdFromLink(collector) == null) {
            Logger.i("请先采集静默数据");//没有阈值，且没有指定要采集静默数据
            collector.setMinutesOfCollectingSilentData(1);//默认设置先采集1分钟静默数据
        } else {
            Logger.i("其他错误");
            Logger.i("阈值为" + Ap.getThresholdFromLink(collector));
        }
    }
}