package domain;

import java.io.*;
import java.net.*;

public class SendSock {
    public static void main(String[] args) throws IOException {
        FileReader fr = new FileReader("4s_4d_4s_json.txt");
        DatagramSocket ds = new DatagramSocket(8080);

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
                DatagramPacket dp = new DatagramPacket(sendStr.getBytes(),sendStr.getBytes().length);
                dp.setSocketAddress(new InetSocketAddress("127.0.0.1",8087));
                ds.send(dp);
                sendStr = "";
                count=0;
            }
        }
        ds.close();
        fr.close();
    }
}
