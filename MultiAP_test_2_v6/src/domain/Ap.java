package domain;

import util.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class Ap {
    private String mac;//此ap的mac地址
    private String timestamp;//此ap的mac地址
    private double threshold;//此ap对应的阈值
    //<从侦听者到aor/sor/sand中所有不同ap的链路的链路的入侵结果队列>
    private List<String> staticDataList;//静态数据
    private List<String> dataList;//非静态数据（只有在计算出阈值之后才会开始存储非静态数据）
    private List<Integer> invadedList;//结果队列

    private int minutesOfCollectingSilentData;//需要采集的静态数据时间 0代表不需要，1代表需要采集1分钟，2代表两分钟，以此类推
    private  double[][] tempMatrix;


    //getter和setter
    public String getMac() {
        return mac;
    }
    public String getTimestamp() {
        return timestamp;
    }
    public double getThreshold() {
        return threshold;
    }
    public List<String> getStaticDataList() {
        return staticDataList;
    }
    public List<String> getDataList() {
        return dataList;
    }
    public List<Integer> getInvadedList() {
        return invadedList;
    }
    public int getMinutesOfCollectingSilentData() {
        return minutesOfCollectingSilentData;
    }
    public double[][] getTempMatrix() {
        return tempMatrix;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }
    public void setStaticDataList(List<String> staticDataListMap) {
        this.staticDataList = staticDataListMap;
    }
    public void setDataList(List<String> dataList) {
        this.dataList = dataList;
    }
    public void setInvadedList(List<Integer> invadedQueue) {
        this.invadedList = invadedQueue;
    }
    public void setMinutesOfCollectingSilentData(int minutesOfCollectingSilentData) {
        this.minutesOfCollectingSilentData = minutesOfCollectingSilentData;
    }
    public void setTempMatrix(double[][] tempMatrix) {
        this.tempMatrix = tempMatrix;
    }

    //静态方法------------------------------------------------------------------------------------------------------------------
    /**
     * 获得此从侦听者到被侦听者之间的链路阈值
     * @param collector   侦听者
     */
    public static void setThresholdFromLink(Ap collector, double upbound) {
        collector.setThreshold(upbound);
    }
    public static Double getThresholdFromLink(Ap collector) {
        return collector.getThreshold();
    }

    /**
     * 获得此从侦听者到被侦听者之间的链路阈值
     * @param collector   侦听者
     */
    public static void setStaticDataListFromLink(Ap collector, List<String> staticDataList) {
        collector.setStaticDataList(staticDataList);
    }
    public static List<String> getStaticDataListFromLink(Ap collector) {
        List<String> list = collector.getStaticDataList();
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public static void setDataListFromLink(Ap collector, List<String> dataList) {
        collector.setDataList(dataList);
    }
    public static List<String> getDataListFromLink(Ap collector) {
        List<String> list = collector.getDataList();
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    /**
     * 获得此从侦听者到被侦听者之间的链路阈值
     * @param collector   侦听者
     */
    public static void setInvadedListFromLink(Ap collector, List<Integer> invadedQueue) {
        collector.setInvadedList(invadedQueue);
    }
    public static List<Integer> getInvadedListFromLink(Ap collector) {
        List<Integer> invadedQueue = collector.getInvadedList();
        if (invadedQueue == null) {
            invadedQueue = new ArrayList<>();
        }

        return invadedQueue;
    }

    public static void setTimestampFromLink(Ap collector, String timestamp) {
        collector.setTimestamp(timestamp);
    }
    public static String getTimestampFromLink(Ap collector) {
        return collector.getTimestamp();
    }

    public static void setTempMatrixFromLink(Ap collector, double[][] tempMatrix) {
        collector.setTempMatrix(tempMatrix);
    }
    public static double[][] getTempMatrixFromLink(Ap collector) {
        return collector.getTempMatrix();
    }
}
