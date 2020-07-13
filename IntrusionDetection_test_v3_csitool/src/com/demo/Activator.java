package com.demo;

import com.google.gson.Gson;
import domain.*;
import util.Logger;

import javax.swing.*;
import java.awt.*;
import java.awt.image.ConvolveOp;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 客户端 只用UDP方式连接
 *
 */
public class Activator {

    static JScrollPane jsp;
    public static JTextArea textArea = new JTextArea(15,65);
    static {
        JPanel panel = new JPanel();
        JFrame jf = new JFrame("测试窗口");

        jf.setSize(630,400);
        jf.setLocationRelativeTo(null);

        jsp = new JScrollPane(textArea);
        textArea.setLineWrap(true);
        textArea.setFont(new Font("黑体",Font.BOLD,18));

        panel.add(jsp);

        jf.setContentPane(panel);
        jf.setVisible(true);
    }

    //本地端口号
    private static final int LOCAL_PORT = 8000;
    //Logger的输出前缀
    private static final String PREFIX = "GPSLab-UDPData:";
    //主函数
    public static void main(String[] args) {
        int local_port;
        int data_num;
        int threshold_num;
        int save_txt;

        if (args.length == 0) {
            local_port = 8000;
            data_num = 100;
            threshold_num = 4000;
            save_txt = 1;
        } else {
            local_port = Integer.parseInt(args[0]);
            data_num = Integer.parseInt(args[1]);
            threshold_num = Integer.parseInt(args[2]);
            save_txt = Integer.parseInt(args[3]);
        }

        System.out.println("data_num = " + data_num + " threshold_num = " + threshold_num + " save_txt = " + save_txt + " local_port = " + local_port);
        //初始化日志前缀，方便在路由器上查看debug信息
        Logger.init(PREFIX, "./");

        //创建定时线程池
        ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);

        pool.schedule(new Runnable() {
            public void run() {
                Logger.i("进入线程池");
                //预处理，提取之前保存的阈值
                Processor.preProcess();
                //启用UDP接受字节数组并解析算法处理。此方法为阻塞方法。
                receiveWithUDP(local_port, data_num, threshold_num, save_txt);
            }
        }, 0, TimeUnit.SECONDS);
    }

    /**
     * NIO 用UDP方式读取文件
     */
    private static void receiveWithUDP(int local_port,int data_num, int threshold_num, int save_txt) {
        Logger.i("进入receiveWithUDP()函数");
        InetSocketAddress localAddress = new InetSocketAddress(local_port);//LOCAL_PORT端口

        //try-with-resources语法
        try (DatagramChannel dc = DatagramChannel.open(); Selector selector = Selector.open()) {
            dc.configureBlocking(false);//非阻塞'
            dc.bind(localAddress);//绑定LOCAL_PORT端口

            ByteBuffer receiveBuffer = ByteBuffer.allocate(214);//分配接收数组
            dc.register(selector, SelectionKey.OP_READ);//只监听read事件
            while (selector.select() > 0) {//当有事件
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();//取出所有事件
                while (it.hasNext()) {//遍历事件
                    SelectionKey sk = it.next();//获取事件

                    if (sk.isReadable()) {//如果是可读取事件

                        receiveBuffer.clear();
                        dc.receive(receiveBuffer);
                        receiveBuffer.flip();//准备读

                        byte[] dataArray = new byte[receiveBuffer.remaining()];//根据接收长度，创建一个等长的数组

                        receiveBuffer.get(dataArray);//接结果复制到dataArray中
                        byte[] data = Arrays.copyOfRange(dataArray, 0, dataArray.length);//提取数组的213位，
                        Packet packet= HandlePacket.readPacket(data);//将数据封装成每个数据包
                        HashMap<Integer, ArrayList<Complex>> scalcsi=HandlePacket.GetScaleCSI(packet);//提取数据包中的CSI  有三根天线 每根天线30个复数值
                        CsiInfo csi = new CsiInfo();

                        double [] total_ant_amp = new double[30];

                        //将每个天线的幅值赋值给对应的自定义的CSI数据包
                        for (int i = 1 ;i<scalcsi.size();i++){
                            ArrayList<Complex> a=scalcsi.get(i);
                            Iterator<Complex> itr = a.iterator();
                            int k = 0;
                            while (itr.hasNext()){
                                total_ant_amp[k++] = itr.next().getAmplitude();
                            }
                            switch (i){
                                case 1:csi.set_total_ant1_amp(total_ant_amp);break;
                                case 2:csi.set_total_ant2_amp(total_ant_amp);break;
                                case 3:csi.set_total_ant3_amp(total_ant_amp);break;
                                // case 4:csi.set_total_ant4_amp(total_ant_amp);break;
                                default:break;
                            }
                            int apmac = dataArray[213] - 'a';//为什么最后一位是apmac值
                            csi.set_mac(String.valueOf(apmac));
                            csi.set_timestamp("[2019-05-1917:47:50.352180]");

                        }

                        Processor.processReceiveCsi(csi, data_num, threshold_num, save_txt);//（同步）根据CSI计算入侵结果
                        receiveBuffer.clear();
                    }
                }
                it.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
