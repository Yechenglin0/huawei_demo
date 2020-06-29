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
    private static final int LOCAL_PORT = 8000;
    //Logger的输出前缀
    private static final String PREFIX = "GPSLab-UDPData:";
    //主函数
    public static void main(String[] args) {
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
     * NIO 方式TCP
     */
    public static void receiveWithTCP() {
        //1. 获取通道
        try (ServerSocketChannel ssChannel = ServerSocketChannel.open(); Selector selector = Selector.open()) {

            //2. 切换非阻塞模式
            ssChannel.configureBlocking(false);

            //3. 绑定连接
            ssChannel.bind(new InetSocketAddress(LOCAL_PORT));

            //4. 获取选择器

            //5. 将通道注册到选择器上, 并且指定“监听接收事件”
            ssChannel.register(selector, SelectionKey.OP_ACCEPT);

            //6. 轮询式的获取选择器上已经“准备就绪”的事件
            while (selector.select() > 0) {
                System.out.println("-------------------1----------------------");
                //7. 获取当前选择器中所有注册的“选择键(已就绪的监听事件)”
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    //8. 获取准备“就绪”的是事件
                    SelectionKey sk = it.next();
                    System.out.println("-------------------2----------------------");

                    //9. 判断具体是什么事件准备就绪
                    if (sk.isAcceptable()) {
                        //10. 若“接收就绪”，获取客户端连接
                        SocketChannel sChannel = ssChannel.accept();

                        //11. 切换非阻塞模式
                        sChannel.configureBlocking(false);

                        //12. 将该通道注册到选择器上
                        sChannel.register(selector, SelectionKey.OP_READ);
                    } else if (sk.isReadable()) {
                        //13. 获取当前选择器上“读就绪”状态的通道
                        SocketChannel sChannel = (SocketChannel) sk.channel();

                        //14. 读取数据
                        ByteBuffer buf = ByteBuffer.allocate(10240);
                        System.out.println("-------------------3----------------------");

                        while ((sChannel.read(buf)) > 0) {
                            System.out.println("-------------------4----------------------");
                            //数据读取到receiveBuffer中,SocketAddress本身为抽象类且只有唯一子类，所以转型不会出错
                            InetSocketAddress socketAddress = (InetSocketAddress) sChannel.getRemoteAddress();
                            buf.flip();//准备读
                            byte[] dataArray = new byte[buf.remaining()];//根据接收长度，创建一个等长的数组
                            buf.get(dataArray);//接结果复制到dataArray中
                            System.out.println("-------------------5----------------------");

                            String jsonString = new String(buf.array(), 0, buf.limit());
                            System.out.println("-------------------5.5----------------------");
                            System.out.println("Json : " + jsonString);
                            CsiInfo csi = transCSI(jsonString);
                            System.out.println("-------------------6----------------------");

                            Processor.processReceiveCsi(csi);//（同步）根据CSI计算入侵结果
                            buf.clear();
                            System.out.println("------------------7----------------------");

                        }
                    }

                    //15. 取消选择键 SelectionKey
                    it.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void receiveWithTCP2() {
        //1. 获取通道
        try (ServerSocketChannel ssChannel = ServerSocketChannel.open(); Selector selector = Selector.open()) {

            //2. 切换非阻塞模式
            ssChannel.configureBlocking(false);

            //3. 绑定连接
            ssChannel.bind(new InetSocketAddress(LOCAL_PORT));

            //4. 获取选择器

            //5. 将通道注册到选择器上, 并且指定“监听接收事件”
            ssChannel.register(selector, SelectionKey.OP_ACCEPT);

            //6. 轮询式的获取选择器上已经“准备就绪”的事件
            while (selector.select() > 0) {

                //7. 获取当前选择器中所有注册的“选择键(已就绪的监听事件)”
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();

                while (it.hasNext()) {
                    //8. 获取准备“就绪”的是事件
                    SelectionKey sk = it.next();

                    //9. 判断具体是什么事件准备就绪
                    if (sk.isAcceptable()) {
                        //10. 若“接收就绪”，获取客户端连接
                        SocketChannel sChannel = ssChannel.accept();

                        //11. 切换非阻塞模式
                        sChannel.configureBlocking(false);

                        //12. 将该通道注册到选择器上
                        sChannel.register(selector, SelectionKey.OP_READ);
                    } else if (sk.isReadable()) {
                        //13. 获取当前选择器上“读就绪”状态的通道
                        SocketChannel sChannel = (SocketChannel) sk.channel();

                        //14. 读取数据
                        ByteBuffer buf = ByteBuffer.allocate(10240);

                        while ((sChannel.read(buf)) > 0) {
                            //数据读取到receiveBuffer中,SocketAddress本身为抽象类且只有唯一子类，所以转型不会出错
                            InetSocketAddress socketAddress = (InetSocketAddress) sChannel.getRemoteAddress();
                            buf.flip();//准备读

                            byte[] dataArray = new byte[buf.remaining()];//根据接收长度，创建一个等长的数组

                            buf.get(dataArray);//接结果复制到dataArray中
                            System.out.println(buf.remaining());
                            System.out.println(buf.limit());
                            String jsonString = new String(buf.array(), 0, buf.limit());
                            System.out.println("-------------------5.5----------------------");
                            System.out.println("Json : " + jsonString);
                            CsiInfo csi = transCSI(jsonString);
                            System.out.println("-------------------6----------------------");

                            Processor.processReceiveCsi(csi);//（同步）根据CSI计算入侵结果
                            buf.clear();

                        }
                    }

                    //15. 取消选择键 SelectionKey
                    it.remove();
                }
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
