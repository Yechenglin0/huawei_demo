package util;

import com.demo.Activator;
import domain.Ap;
import domain.InvadedList;
import java.util.HashMap;
import java.util.Map;

public class Caches {
    public static Map<String, Ap> apCaches = new HashMap<>();
    public static InvadedList invadedListCaches;

    public static InvadedList getOrCreateInvadedList() {
        Logger.i("获得入侵队列");
        InvadedList invadedList = invadedListCaches;
        if (invadedList == null) {
            Logger.i("创建一个入侵队列");
            invadedListCaches = new InvadedList();
        }
        return invadedListCaches;
    }
    //从缓存中找到mac对应的ap，如果没有找到，就new一个并添加进去。
    public static Ap getOrCreateAp(String mac) {
        Ap ap = apCaches.get(mac);

        if (ap == null) {
            ap = addAp(mac);
            Activator.textArea.append("新建连接AP：" + mac + "\n");
            if (ap.getMinutesOfCollectingSilentData() > 0) {//没有阈值，则添加进集合并保存文件,
                Activator.textArea.append("AP：" + mac +" 开始收集静态数据\n");
            }
            Logger.i("新建了一个ap");
        }
        return ap;
    }

    public static Ap addAp(String mac) {
        Ap ap = new Ap();
        ap.setMac(mac);
        ap.setMinutesOfCollectingSilentData(1);
        apCaches.put(mac, ap);
        int ap_num = InvadedList.getApnumFromLink(invadedListCaches);
        InvadedList.setApnumFromLink(invadedListCaches,ap_num + 1);
        return ap;
    }

    public static void addAp(Ap ap) {
        if (ap.getMac() == null) throw new RuntimeException("添加的ap的mac为空，无法添加！");
        apCaches.put(ap.getMac(), ap);
    }

    public static void removeAp(Ap ap) {
        int ap_num = InvadedList.getApnumFromLink(invadedListCaches);
        if (ap_num > 0) {
            InvadedList.setApnumFromLink(invadedListCaches,ap_num - 1);
        }
    }

    /**
     * 在Sense_del和Alarm_del中用到了此方法
     *
     * @param mac 从缓存中移除此ap
     */
    public static void removeAp(String mac) {
        apCaches.remove(mac);
    }

    /**
     * @param mac 缓存中是否包含此ap
     * @return 包含就返回true，否则fasle
     */
    public static boolean contains(String mac) {
        return apCaches.containsKey(mac);
    }

    public static boolean contains(Ap ap) {
        return apCaches.containsValue(ap);
    }

    public static void clearCaches() {
        apCaches.clear();
    }
}




