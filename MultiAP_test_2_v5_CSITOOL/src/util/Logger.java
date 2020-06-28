package util;


import java.io.PrintStream;


/**
 * 日志类;
 * 重要：日志类初始化必须最先执行，否则后面会无法获得文件存储路径而报空指针。
 */
public class Logger {
    public static String prefix = "";
    private static PrintStream out = System.out;

    /**
     * @param prefix  前缀
     * @param rootDir 文件存储的根目录
     */
    public static void init(String prefix, String rootDir) {
        Logger.prefix = prefix;
        FileUtils.ROOT_DIR = rootDir;
        i("Logger and FileRootDir are initialized");
    }

    public static void i(String format, Object... args) {
        out.println(prefix + String.format(format, args));
    }

    public static void i(Object content) {
        out.println(prefix + content.toString());
    }
}
