package util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具类
 * Created by Nancy on 2017/5/11.
 */
public class DateUtils {
    private static Date date = new Date();
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    //本次的软件启动时间，用来确定文件
    private static String launchTime = sdf.format(date);

    public static String getLaunchTime() {
        return launchTime;
    }

    public static SimpleDateFormat getSdf() {
        return sdf;
    }

    public static String getDateAndTime(){
        return sdf.format(new Date());
    }

}
