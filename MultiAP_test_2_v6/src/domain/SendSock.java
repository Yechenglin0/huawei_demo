package domain;

import java.io.*;
import java.net.*;

public class SendSock {
    static int count2 = 0;
    public static void main(String[] args) throws IOException {


        FileReader fr = new FileReader("4s_4d_4s_json.txt");
        DatagramSocket ds = new DatagramSocket(8080);

        int len;
        int count = 0;
        String sendStr = "";
        long startTime = System.currentTimeMillis();    //获取开始时间
        while ((len = fr.read())!=-1){

            sendStr+=(char)len;
            if((char)len=='\n'){
                count++;
            }
            if(count==30){
                System.out.println(++count2);
//                System.out.println(sendStr);
                DatagramPacket dp = new DatagramPacket(sendStr.getBytes(),sendStr.getBytes().length);
                dp.setSocketAddress(new InetSocketAddress("127.0.0.1",8000));
                ds.send(dp);
                sendStr = "";
                count=0;
            }
            if (count2 == 2000) {
                long endTime = System.currentTimeMillis();    //获取结束时间
                System.out.println("发包时间：" + (endTime - startTime) + "ms");    //输出程序运行时间
            }

        }
        ds.close();
        fr.close();


    }


}

class SendTcp {
    public static void main(String[] args) throws IOException {
        Socket socket  = new Socket("127.0.0.1",8000);
        FileReader fr = new FileReader("F:\\0609代码\\huawei_demo\\MultiAP_test_2_v6\\4s_4d_4s_json.txt");
        OutputStream send = socket.getOutputStream();
//        InputStream readData = new FileInputStream("G:\\项目组\\南京华为项目（wifi感知技术部分）\\第一阶段\\测试\\20191226\\1226_1000_silence1.dat");
//        byte[] store = new byte[214];
//        OutputStream send2 = new FileOutputStream("C:\\Users\\cx\\Desktop\\ceshi.dat");
        int len;
        int count = 0;
        String sendStr = "";
        while ((len = fr.read())!=-1){
            sendStr+=(char)len;
            if((char)len=='\n'){
                count++;
            }
            if(count==30){
                System.out.println(sendStr);
                send.write(sendStr.getBytes());
                sendStr = "";
                count=0;
                send.flush();
            }
        }
        send.close();
    }
}