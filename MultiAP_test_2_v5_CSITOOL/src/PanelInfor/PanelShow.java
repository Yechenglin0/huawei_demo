package PanelInfor;


import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;


/**
 * Created by xielinxiao on 2017/11/12.
 */
public class PanelShow {
    private PrintWriter pw;
    private JFrame frame;
    private JPanel pane_buttom;
    private JSplitPane pane_center;
    private JScrollPane pane_showWindow;
    private JScrollPane pane_inputWindow;
    private JTextArea area_showWindow;
    private JTextArea area_inputWindow;

    private JButton btn_send;

    private Dimension dimension;



    public PanelShow() {
        frame = new JFrame();
        pane_buttom = new JPanel();
        pane_showWindow = new JScrollPane();
        pane_inputWindow = new JScrollPane();
        area_showWindow = new JTextArea();
        area_inputWindow = new JTextArea();
        pane_center = new JSplitPane(JSplitPane.VERTICAL_SPLIT, false, pane_showWindow, pane_inputWindow);
      //  pane_center.setDividerLocation(100);
       // btn_send = new JButton("发送");

        dimension = new Dimension(50, 250);

    }


    public void showFrame()throws Exception{
        initFrame();
        initChatTextArea();
      
        detection();
    }



    public void initFrame(){
        frame.setTitle("入侵结果");
        int width = (int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
        frame.setBounds(width / 2, height / 2, 400, 450);
        frame.setVisible(true);
    }



    private void initChatTextArea(){
        //取得视图焦点
        pane_showWindow.getViewport().add(area_showWindow);
        pane_inputWindow.getViewport().add(area_inputWindow);
        //将显示文本域设置为不可编辑
        area_showWindow.setEditable(false);
        //设置显示文本域可拖拉的大小
        pane_showWindow.setMinimumSize(dimension);
        frame.add(pane_center, BorderLayout.CENTER);
    }



    
    public  static void detection() throws Exception{
    	 Music.music();
         Picture.picture();
    }
}


