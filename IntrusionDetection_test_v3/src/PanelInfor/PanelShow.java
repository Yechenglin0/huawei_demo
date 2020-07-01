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

    private JTextField jtf = new JTextField("该文本不可编辑", 30);    // 创建文本行组件, 30 列
    private JPasswordField jpf = new JPasswordField("密码文本", 30);   // 创建密码文本行组件, 30 列
    private JTextArea jta = new JTextArea("您好", 10, 30);               // 创建文本区组件,10行，30列
    private JScrollPane jsp = new JScrollPane(jta);                    // 创建滚动窗格，其显示内容是文本区对象
    public void display() {

        // 布局
        JFrame f = new JFrame("文本编辑功能窗口");
        f.setBounds(200, 150, 350, 227);
        f.setLayout(null);

        jta.setLineWrap(true); // 设置自动换行
        jpf.setBounds(20, 10, 140, 20);
        jtf.setBounds(20, 40, 140, 20);
        jsp.setBounds(20, 70, 160, 100);

        // 把组件添加进窗口f中
        f.add(jpf);
        f.add(jtf);
        f.add(jsp);

        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }


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
//         Picture.picture();
    }
}


