package com.demo;

import com.google.gson.Gson;
import domain.Ap;
import domain.CsiInfo;
import util.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 客户端 只用UDP方式连接
 *
 */
public class Activator {
    //本地端口号
    private static final int LOCAL_PORT = 8087;
    //Logger的输出前缀
    private static final String PREFIX = "GPSLab-UDPData:";
    //主函数
    public static void main(String[] args) {
        //初始化日志前缀，方便在路由器上查看debug信息
        Logger.init(PREFIX, "./");

        //设置计算出入侵结果的监听器
        //WifisenseCommand.reportData()函数目前是写的空。
//        Processor.setOnIntrusionListener(new Processor.onIntrusionListener() {
//            /**
//             * @param collector 侦听者
//             * @param isInvaded 侦听者和<aor>的链路是否满足逻辑关系
//             */
//            @Override
//            public void onAlarmed(Ap collector, boolean isInvaded) {
//                Logger.i("侦听者%s管理的区域是否有告警:%b", collector.getMac(), isInvaded);//输出
//                int dateValue = isInvaded ? 1 : 0;
//                //上报数据到云上
////                WifisenseCommand.reportData("motionAlarm", "motionAlarm1", "alarm", dateValue);
//            }
//
//            /**
//             * @param collector 侦听者
//             * @param isSensed 侦听者和<sor><sand>的链路是否满足逻辑关系
//             */
//            @Override
//            public void onSensed(Ap collector, boolean isSensed) {
//                Logger.i("侦听者%s管理的区域是否有区域感知:%b", collector.getMac(), isSensed);
//                int dateValue = isSensed ? 1 : 0;
//                //上报数据到云上
////                WifisenseCommand.reportData("motionSensor", "motionSensor1", "alarm", dateValue);
//            }
//
//            /**
//             * 当侦听者的和<sor><sand>之间的链路已经持续五分钟无任何链路被入侵的时候，此方法会被调用
//             * @param collector 侦听者
//             */
//            @Override
//            public void onNoSensed(Ap collector) {
//                Logger.i("侦听者%s管理的区域已经五分钟没有入侵了");
//                //上报数据到云上
////                WifisenseCommand.reportData("motionSensor", "motionSensor1", "noAlarm", 1);
//            }
//        });

        //创建定时线程池
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

        pool.schedule(new Runnable() {
            public void run() {
                Logger.i("进入线程池");
                //预处理，提取之前保存的阈值
                Processor.preProcess();
                //启用UDP接受字节数组并解析算法处理。此方法为阻塞方法。
                receiveWithUDP();
            }

        }, 0, TimeUnit.SECONDS);
    }

    /**
     * NIO 用UDP方式读取文件
     */
    private static void receiveWithUDP() {
        Logger.i("进入receiveWithUDP()函数");
        InetSocketAddress localAddress = new InetSocketAddress(LOCAL_PORT);//LOCAL_PORT端口

        //try-with-resources语法
        try (DatagramChannel dc = DatagramChannel.open(); Selector selector = Selector.open()) {
            dc.configureBlocking(false);//非阻塞
            dc.bind(localAddress);//绑定LOCAL_PORT端口

            ByteBuffer receiveBuffer = ByteBuffer.allocate(10240);//分配接收数组
            dc.register(selector, SelectionKey.OP_READ);//只监听read事件

            while (selector.select() > 0) {//当有事件
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();//取出所有事件
                while (it.hasNext()) {//遍历事件
                    SelectionKey sk = it.next();//获取事件

                    if (sk.isReadable()) {//如果是可读取事件

                        receiveBuffer.clear();
                        dc.receive(receiveBuffer);
                        receiveBuffer.flip();//准备读

                        String jsonString = new String(receiveBuffer.array(), 0, receiveBuffer.limit());
                        CsiInfo csi = transCSI(jsonString);

                        Processor.processReceiveCsi(csi);//（同步）根据CSI计算入侵结果
                    }
                }
                it.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 将30行json字符串转成CsiInfo对象
     *
     * @param jsonString 传输得到的json数据
     *
     */
    private static CsiInfo transCSI(String jsonString) {
        Gson gson = new Gson();
        CsiInfo csi = new CsiInfo();//存放读取的json格式文件
        String lines[] = jsonString.split("\\r?\\n");

        String[] timestamp = new String [lines.length];
        double[] ant1_amp  = new double [lines.length];
        double[] ant2_amp  = new double [lines.length];
        double[] ant3_amp  = new double [lines.length];
        double[] ant4_amp  = new double [lines.length];
        String[] mac = new String [lines.length];

        for (int i = 0; i < lines.length; i++) {
            csi = gson.fromJson(lines[i], CsiInfo.class);
            timestamp[i] = csi.get_timestamp();
            ant1_amp[i]  = csi.get_ant1_amp();
            ant2_amp[i]  = csi.get_ant2_amp();
            ant3_amp[i]  = csi.get_ant3_amp();
            ant4_amp[i]  = csi.get_ant4_amp();
            mac[i] = csi.get_mac();
        }

        csi.set_total_timestamp(timestamp);
        csi.set_total_ant1_amp(ant1_amp);
        csi.set_total_ant2_amp(ant2_amp);
        csi.set_total_ant3_amp(ant3_amp);
        csi.set_total_ant4_amp(ant4_amp);
        csi.set_total_mac(mac);
        return csi;
    }
}
