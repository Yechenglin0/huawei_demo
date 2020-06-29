package PanelInfor;


import java.applet.Applet;
import java.applet.AudioClip;
import java.io.File;
import java.net.URI;
import java.net.URL;


public class Music {
    public static void main(String[] args) {
        Music.music();
        try {
            Picture.picture();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void music() {               //注意，java只能播放无损音质，如.wav这种格式
        try {
            File f = new File("C:\\Users\\cx\\Desktop\\warning.wav"); //相对路径
            URI uri = f.toURI();
            URL url = uri.toURL(); //解析路径
            AudioClip aau;
            aau = Applet.newAudioClip(url);
            aau.play();
            System.out.println("sdad");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

