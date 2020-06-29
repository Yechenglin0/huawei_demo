package util;

import com.demo.Activator;
import domain.Ap;
import java.util.HashMap;
import java.util.Map;

public class Caches {
    public static Map<String, Ap> apCaches = new HashMap<>();

    //从缓存中找到mac对应的ap，如果没有找到，就new一个并添加进去。
    public static Ap getOrCreateAp(String mac) {
        Ap ap = apCaches.get(mac);

        if (ap == null) {
            ap = addAp(mac);
            Logger.i("新建了一个ap");
        }
        return ap;
    }

    public static Ap addAp(String mac) {
        Ap ap = new Ap();
        ap.setMac(mac);
        ap.setMinutesOfCollectingSilentData(1);
        Activator.textArea.append("开始收集静态数据\n");
        apCaches.put(mac, ap);
        return ap;
    }

    public static void addAp(Ap ap) {
        if (ap.getMac() == null) throw new RuntimeException("添加的ap的mac为空，无法添加！");
        apCaches.put(ap.getMac(), ap);
    }

    public static void removeAp(Ap ap) {
        if (ap.getMac() == null) throw new RuntimeException("需要移除的ap的mac为空，无法移除！");
        apCaches.remove(ap.getMac());
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




