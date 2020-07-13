package domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class InvadedList {
    private int ap_num;//ap个数
    private List<Integer> invadedList;//结果队列

    public List<Integer> getInvadedList() {
        return invadedList;
    }
    public int getAp_num() {
        return ap_num;
    }

    public void setAp_num(int ap_num) {
        this.ap_num = ap_num;
    }
    public void setInvadedList(List<Integer> invadedList) {
        this.invadedList = invadedList;
    }

    public static void setInvadedListFromLink(InvadedList list, List<Integer> invadedQueue) {
        list.setInvadedList(invadedQueue);
    }
    public static List<Integer> getInvadedListFromLink(InvadedList list) {
        List<Integer> invadedQueue = list.getInvadedList();
        if (invadedQueue == null) {
            invadedQueue = new ArrayList<>();
        }

        return invadedQueue;
    }

    public static void setApnumFromLink(InvadedList list, int ap_num) {
        list.setAp_num(ap_num);
    }
    public static int getApnumFromLink(InvadedList list) {
        return list.getAp_num();
    }

}
