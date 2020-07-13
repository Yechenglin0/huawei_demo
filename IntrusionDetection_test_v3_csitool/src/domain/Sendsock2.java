package domain;

import java.io.*;
import java.net.*;

public class Sendsock2 {
    static int count2 = 0;
    public static void main(String[] args) throws IOException {
        FileReader fr = new FileReader("4s_4d_4s_json_2.txt");
        DatagramSocket ds = new DatagramSocket(8081);

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
                DatagramPacket dp = new DatagramPacket(sendStr.getBytes(),sendStr.getBytes().length);
                dp.setSocketAddress(new InetSocketAddress("127.0.0.1",8000));
                ds.send(dp);
                sendStr = "";
                count = 0;
            }
            if (count2 == 2000) {
                long endTime = System.currentTimeMillis();    //获取结束时间
            }
        }
        ds.close();
        fr.close();
    }


}
