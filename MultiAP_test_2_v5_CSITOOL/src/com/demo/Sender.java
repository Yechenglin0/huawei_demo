package com.demo;

import java.io.*;
import java.net.Socket;

public class Sender {
    public static void main(String[] args) throws IOException {
        Socket socket  = new Socket("127.0.0.1",8000);
        OutputStream send = socket.getOutputStream();
        InputStream readData = new FileInputStream("E:\\项目组\\南京华为项目（wifi感知技术部分）\\第一阶段\\测试\\20191226\\1226_1000_silence1.dat");
        byte[] store = new byte[214];
        OutputStream send2 = new FileOutputStream("C:\\Users\\cx\\Desktop\\ceshi.dat");
        int len;
        while ((len= readData.read(store))!=-1){
            send.write(store);
            System.out.println(store.length);
            send2.write(store);
        }
        send.close();

    }
}
