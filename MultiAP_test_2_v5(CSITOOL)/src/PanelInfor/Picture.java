package PanelInfor;


import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.File;

public class Picture {
    public static void picture() throws Exception {
        File sourceimage = new File("C:\\Users\\YeChenglin\\Desktop\\MultiAP_test_2_v5(CSITOOL)\\war.jpg");

        Image image = ImageIO.read(sourceimage);


        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(image));
//        label.setBounds(0,0,40,40);
//        label.setSize(100,20);
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.setLocation(400,100);
        frame.setSize(50,100);
        frame.pack();
        frame.setVisible(true);
        Thread.sleep(500);
        frame.setVisible(false);
       
        
    }
}
