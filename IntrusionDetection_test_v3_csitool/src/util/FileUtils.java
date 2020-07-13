package util;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 文件工具类
 * Created by Nancy on 2017/5/11.
 */
public class FileUtils {

    public static String ROOT_DIR = "./Data/";//必须以/结尾，否则出错
    public static String df = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());//设置日期格式

    public static String STATIC_DATA_TXT = "/" + df +"/data.txt";
    public static String STATIC_FVALUE_TXT = "/" + df +"/fvalue.txt";
    public static String STATIC_OUTPUT_TXT = "/" +df +"/output.txt";
    public static String STATIC_THRESHOLD_TXT = "/" + df +"/threshold.txt";

//      System.out.println(Paths.get(".").toAbsolutePath()); --> /openee/osgi/.
//     	System.out.println(context.getDataFile(null));  --> null
//    	System.out.println(context.getDataFile(""));   --> ./data/runtime/bundle29/data
//    	System.out.println(context.getDataFile("a.txt")); -->    ./data/runtime/bundle29/data/a.txt


    public static void write2txt(String filePath, String insertContent) {

        // 每次写入时，都换行写
        String strContent = insertContent + "\r\n";
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Logger.i("Create the file:" + filePath);
                if (file.getParentFile().mkdirs()) {
                    file.createNewFile();
                }
            }

            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Logger.i("Error on write File:" + e);
        }

    }

    /**
     * @param filePath 写入的文件路径
     * @param bytes    需要写入的字节数组
     */
    public static void write2txt(String filePath, String mac ,byte[] bytes) {

        String startString = String.format("{time:%s,mac:%s,bytes:", DateUtils.getDateAndTime(),mac);
        String endString = "}\r\n";

        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Logger.i("Create the file:" + filePath);
                if (file.getParentFile().mkdirs()) {
                    file.createNewFile();
                }
            }

            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());

            raf.writeChars(startString);
            raf.write(bytes);
            raf.writeChars(endString);

            raf.close();
        } catch (IOException e) {
            Logger.i("Error on write File:" + e);
            e.printStackTrace();
        }
    }
}
