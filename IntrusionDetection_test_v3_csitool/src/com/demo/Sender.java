package com.demo;

import java.io.*;
import java.net.*;

public class Sender {
    public static void main(String[] args) throws IOException {
        Socket socket  = new Socket("127.0.0.1",8000);
        OutputStream send = socket.getOutputStream();
        InputStream readData = new FileInputStream("G:\\项目组\\南京华为项目（wifi感知技术部分）\\第一阶段\\测试\\20191226\\1226_1000_silence1.dat");
        byte[] store = new byte[214];
        OutputStream send2 = new FileOutputStream("C:\\Users\\cx\\Desktop\\ceshi.dat");
        int len;
        while ((len= readData.read(store))!=-1){
            send.write(store);
        }
        send.close();
    }
}
class SenderUDP{
    public static void main(String[] args) throws IOException {
        InputStream readData = new FileInputStream("F:\\0609代码\\huawei_demo\\MultiAP_test_2_v5(CSITOOL)\\1226_1000_silence1.dat");
        DatagramSocket ds = new DatagramSocket(8800);
        byte[] store = new byte[214];
        int len;
        while ((len = readData.read(store))!=-1){
            DatagramPacket dp = new DatagramPacket(store,214);
            dp.setSocketAddress(new InetSocketAddress("127.0.0.1",8000));
            ds.send(dp);
        }
        ds.close();
        readData.close();
    }
}
