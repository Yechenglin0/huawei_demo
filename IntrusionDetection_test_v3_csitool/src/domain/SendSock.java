package domain;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

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
                dp.setSocketAddress(new InetSocketAddress("127.0.0.1",8000));
                ds.send(dp);
                sendStr = "";
                count=0;
            }
        }
        ds.close();
        fr.close();
    }
}
class Test{

    static JTextArea textArea = new JTextArea(5,10);
    static {
        Timer timer = new Timer();
        timer.schedule(new Timerc(),10,1000);
        JFrame jf = new JFrame("测试窗口");
        jf.setSize(250,250);
        jf.setLocationRelativeTo(null);
        JPanel panel = new JPanel();
        textArea.setLineWrap(true);
        panel.add(textArea);
        jf.setContentPane(panel);
        jf.setVisible(true);
    }
    public static void main(String[] args) {

    }
    static class Timerc extends TimerTask{
        @Override
        public void run() {
            textArea.append(""+"\r\n");
        }
    }
}