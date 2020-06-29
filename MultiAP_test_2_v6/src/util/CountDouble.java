package util;

import Jama.Matrix;
import domain.Ap;
import domain.CsiInfo;

import com.google.gson.Gson;

import java.util.List;

/**
 * 计算阈值和入侵结果的算法类
 */

public class CountDouble {

    /**
     * 根据获得的静态数据计算阈值
     *
     * @param staticDataList 输入列表数据
     *
     * @return 根据静态文件计算得到的阈值
     */
    public static double calUpbound(List<String> staticDataList) {
        long startTime = System.currentTimeMillis();    //获取开始时间

        double[][] subCSI = get_Nor_subCSI(staticDataList, 2);
        double[][] Nor_subCSI = new double[subCSI.length][27];
        for (int j = 0; j < subCSI.length; j++) {
            for (int i = 0; i < 14; i++) {
                Nor_subCSI[j][i] = subCSI[j][i+1];
            }
            for (int i = 14; i < 27; i++) {
                Nor_subCSI[j][i] = subCSI[j][i+2];
            }
        }

        // 根据窗长进行滑窗，计算相关系数矩阵，求每一次得到的矩阵A、C的归一化最大特征值
        int len = 50;//窗长
        double[] normalized_max_eig = get_Max_eig(Nor_subCSI, 50);

        //根据核函数计算阈值
        double res = getUpbound(normalized_max_eig,0.05);

        long endTime = System.currentTimeMillis();    //获取结束时间
        System.out.println("阈值计算运行时间：" + (endTime - startTime) + "ms");    //输出程序运行时间

        return res;
    }

    /**
     * 根据一个窗长的数据计算相关系数矩阵的最大特征值，并与门限判断
     *
     * @param thresholdValue 门限
     *
     * @param dataList 一个窗的数据
     *
     * @return 运算结果, 返回1表示有人，0表示没人，-1表示计算错误
     */
    public static double calDetection(double thresholdValue, List<String> dataList) {
        double[][] Nor_subCSI = get_Nor_subCSI(dataList, 2);
        double fvalue = get_Max_eig_per_win(Nor_subCSI);
        return fvalue;
    }

    public static double cal_1st_win(double thresholdValue, List<String> dataList) {
        double[][] Nor_subCSI = get_Nor_subCSI(dataList, 2);
        double fvalue = get_Max_eig_per_win(Nor_subCSI);
        int res = -1;//结果1代表有人，0代表没人
        if (fvalue < thresholdValue * 0.975) {
            res = 1;
        } else {
            res = 0;
        }
        return res;
    }

    public static double cal_next_win(double[][] Amplitude_CoffValue) {
        Matrix A           = new Matrix(Amplitude_CoffValue);                   //对矩阵进行特征值分解
        Matrix eigenValue1 = A.eig().getD();                                    //取出幅度特征值
        // 归一化
        double W1          = eigenValue1.trace();                               //求和
        Logger.i("W1 = " + W1);
        eigenValue1        = eigenValue1.times(1/W1);                           //归一化
        double fvalue      = get_max_eig(Matrix_to_Array(eigenValue1, 50));//取出归一化之后的最大特征值

        return fvalue;
    }

    public static double[][] get_temp_matrix_next_win(List<String> dataList, double[][] temporary_matrix) {

        double[][] Nor_subCSI = get_Nor_subCSI(dataList, 2);
        int len = Nor_subCSI.length;//窗长，取50
        double[][] newcsi1             = Nor_subCSI;
        double[][] Amplitude_CoffValue = new double[len][len];

        Amplitude_CoffValue = clone_to_array(temporary_matrix, Amplitude_CoffValue, 1,len-1,1,len-1);
        int k = len - 1;
        for (int n = 0; n < len; n++) {
            double[] a = newcsi1[n];
            double[] b = newcsi1[k];
            double csi_cor = getPearsonCorrelationScore(a,b);            //求所有子载波幅值矢量之间的相关系数
            Amplitude_CoffValue[n][k] = csi_cor;
            Amplitude_CoffValue[k][n] = Amplitude_CoffValue[n][k];
        }
        // 返回相关系数矩阵
        return Amplitude_CoffValue;
    }
    /**
     * 根据一个窗长的数据计算相关系数矩阵
     *
     * @param dataList 输入列表数据
     *
     * @return 返回第一个窗的相关系数
     * 矩阵
     */
    public static double[][] get_temp_matrix_1st_win(List<String> dataList) {

        double[][] Nor_subCSI = get_Nor_subCSI(dataList, 2);
        int len = Nor_subCSI.length;//窗长，取50
        double[][] newcsi1             = Nor_subCSI;
        double[][] Amplitude_CoffValue = new double[len][len];

        for (int n = 0; n < len; n++) {
            for (int k = n; k < len; k++) {
                double[] a     = newcsi1[n];
                double[] b     = newcsi1[k];
                double csi_cor = getPearsonCorrelationScore(a,b);

                Amplitude_CoffValue[n][k] = csi_cor;
                Amplitude_CoffValue[k][n] = Amplitude_CoffValue[n][k];
            }
        }
        // 返回最大特征值
        return Amplitude_CoffValue;
    }

    /**
     * 根据一个窗长的数据计算相关系数，并计算其最大特征值
     *
     * @param Nor_subCSI 输入列表数据
     *
     * @return 这个窗的，相关系数矩阵的，最大特征值
     */
    public static double get_Max_eig_per_win(double[][] Nor_subCSI) {

        int len = Nor_subCSI.length;//窗长，取50
        double[][] newcsi1             = Nor_subCSI;
        double[][] Amplitude_CoffValue = new double[len][len];
        double[] normalized_max_eig    = new double[len];

        for (int n = 0; n < len; n++) {
            for (int k = n; k < len; k++) {
                double[] a     = newcsi1[n];
                double[] b     = newcsi1[k];
                double csi_cor = getPearsonCorrelationScore(a,b);

                Amplitude_CoffValue[n][k] = csi_cor;
                Amplitude_CoffValue[k][n] = Amplitude_CoffValue[n][k];
            }
        }
        Matrix A           = new Matrix(Amplitude_CoffValue);                   //对矩阵进行特征值分解
        Matrix eigenValue1 = A.eig().getD(); //取出幅度特征值

        // 归一化
        double W1             = eigenValue1.trace();      //求和
        eigenValue1           = eigenValue1.times(1/W1);//归一化
        normalized_max_eig[0] = get_max_eig(Matrix_to_Array(eigenValue1, 50));//取出归一化之后的最大特征值

        // 返回最大特征值
        return normalized_max_eig[0];
    }

    /**
     * 根据获得的静态数据计算相关系数，进行滑窗循环后得到相关系数矩阵，并计算其最大特征值
     *
     * @param Nor_subCSI 输入列表数据
     *
     * @param len 窗长
     *
     * @return 归一化的最大特征值 数组
     */
    public static double[] get_Max_eig(double[][] Nor_subCSI, int len) {
        double[][] Amplitude_CoffValue = new double[len][len];
        double[][] newcsi1             = new double[len][Nor_subCSI[0].length];
        double[][] temporary_matrix    = new double[len - 1][len - 1];
        double[] normalized_max_eig    = new double[Nor_subCSI.length - len + 1];
        for (int i = 0; i < Nor_subCSI.length - len + 1; i++) { //从第1个数据之后开始跑完

            newcsi1 = empty_arr(newcsi1);
            newcsi1 = clone_to_array(Nor_subCSI, newcsi1 ,i ,i + len - 1,0, Nor_subCSI[0].length - 1);
            if (0 == i) {
                for (int n = 0; n < len; n++) {
                    for (int k = n; k < len; k++) {
                        double[] a     = newcsi1[n];
                        double[] b     = newcsi1[k];
                        double csi_cor = getPearsonCorrelationScore(a,b);
                        Amplitude_CoffValue[n][k] = csi_cor;
                        Amplitude_CoffValue[k][n] = Amplitude_CoffValue[n][k];
                    }
                }
                temporary_matrix = empty_arr(temporary_matrix);
                temporary_matrix = clone_to_array(Amplitude_CoffValue, temporary_matrix, 1,len-1,1,len-1);
            }

            if (0 != i) {
                Amplitude_CoffValue = empty_arr(Amplitude_CoffValue);
                Amplitude_CoffValue = clone_to_array(temporary_matrix, Amplitude_CoffValue, 0,len-2,0,len-2);

                int k = len - 1;
                for (int n = 0; n < len; n++) {
                    double[] a = newcsi1[n];
                    double[] b = newcsi1[k];
                    double csi_cor = getPearsonCorrelationScore(a,b);            //求所有子载波幅值矢量之间的相关系数
                    Amplitude_CoffValue[n][k] = csi_cor;
                    Amplitude_CoffValue[k][n] = Amplitude_CoffValue[n][k];
                }
                temporary_matrix = empty_arr(temporary_matrix);
                temporary_matrix = clone_to_array(Amplitude_CoffValue, temporary_matrix, 1,len-1,1,len-1);
            }

            Matrix A = new Matrix(Amplitude_CoffValue);                         //对矩阵进行特征值分解
            Matrix eigenValue1 = A.eig().getD();                                //取出幅度特征值

            // 归一化
            double W1 = eigenValue1.trace();                                    //求和
            eigenValue1 = eigenValue1.times(1/W1);                              //归一化
            double[][] eig_arr = Matrix_to_Array(eigenValue1, len);
            normalized_max_eig[i] = get_max_eig(eig_arr);                       //取出归一化之后的最大特征值
            Amplitude_CoffValue = empty_arr(Amplitude_CoffValue);               //每次循环后置零

        }
        return normalized_max_eig;
    }

    /**
     * 根据获得的静态数据进行数据转换和天线选择
     *
     * @param staticDataList 输入列表数据
     *
     * @param ant_num 选择的天线数
     *
     * @return 选择的天线对应的幅值数据
     */
    public static double[][] get_Nor_subCSI(List<String> staticDataList, int ant_num) {

        Logger.i("长度为：" + staticDataList.size());

        // 提取每个包的CSI幅值数据,进行天线选择，最后得到一个60*30的二维数组
        Gson gson = new Gson();
        String timestamp = new String();
        double[][] ant1_amp = new double[staticDataList.size()][30];
        double[][] ant2_amp = new double[staticDataList.size()][30];
        double[][] ant3_amp = new double[staticDataList.size()][30];
        double[][] ant4_amp = new double[staticDataList.size()][30];
        String mac = new String();
        for(int i = 0; i < staticDataList.size(); i++) {
            CsiInfo packet = gson.fromJson(staticDataList.get(i), CsiInfo.class);
//            Logger.i("数据为："+staticDataList.get(i));
            timestamp   = packet.get_timestamp();
            ant1_amp[i] = packet.get_total_ant1_amp();
            ant2_amp[i] = packet.get_total_ant2_amp();
            ant3_amp[i] = packet.get_total_ant3_amp();
            ant4_amp[i] = packet.get_total_ant4_amp();
            mac         = packet.get_mac();

        }
        double[][] Nor_subCSI = new double[staticDataList.size()][30];
        switch(ant_num)
        {
            case 1 :
                Nor_subCSI = ant1_amp;
                Logger.i("天线选择1");
                break;
            case 2 :
                Nor_subCSI = ant2_amp;
                Logger.i("天线选择2");
                break;
            case 3 :
                Nor_subCSI = ant3_amp;
                Logger.i("天线选择3");
                break;
            case 4 :
                Nor_subCSI = ant4_amp;
                Logger.i("天线选择4");
                break;
            default :
                Nor_subCSI = ant2_amp;
                Logger.i("未定义天线，默认选择2");
        }
        return Nor_subCSI;
    }

    /**
     * 获取二维数组中的最大值
     *
     * @param arr 输入二维数组
     *
     * @return 某个二维数组中的最大值
     */
    public static double get_max_eig(double[][] arr) {
        double max = arr[0][0];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                if (max < arr[i][j]) {
                    max = arr[i][j];
                }
            }
        }

        return max;

    }

    /**
     * 将50*50的矩阵转成二维数组
     *
     * @param m 输入矩阵，一定要是50*50的矩阵
     *
     * @return 一个50*50的矩阵
     */
    public static double[][] Matrix_to_Array(Matrix m, int len) {
        double[][] res = new double[len][len];
        for (int i = 0; i < len; i++) {
            for (int j = 0; j < len; j++) {
                res[i][j] = m.get(i,j);
            }
        }
        return res;
    }

    /**
     * 清空二维数组
     *
     * @param arr 输入数据数组
     *
     * @return 一个所有值都是0的二维数组
     */
    public static double[][] empty_arr(double[][] arr) {
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[0].length; j++) {
                arr[i][j] = 0.0;
            }
        }
        return arr;
    }

    /**
     * 返回给定位置的二维数组
     *
     * @param arr 输入数据数组
     * @param des_arr 输出数据数组
     * @param col_s 开始行
     * @param col_e 结束行
     * @param row_s 开始列
     * @param row_e 结束列
     *
     * @return 目标二维数组
     */
    public static double[][] clone_to_array(double[][] arr, double[][] des_arr, int col_s, int col_e, int row_s, int row_e) {
        double[][] res = des_arr;
        if (col_s > col_e || row_s > row_e || arr.length < col_e || arr[0].length < row_e) {
            Logger.i("输入参数错误！col_s = " + col_s + " col_e = " + col_e + " row_s = " + row_s + " row_e" + row_e);
        } else {
            for (int i = 0; i < col_e - col_s + 1; i++) {
                for (int j = 0; j < row_e - row_s + 1; j++) {
                    res[i][j] = arr[i + col_s][j + row_s];
                }
            }
        }
        return res;
    }

    /**
     * 计算x和y的相关系数
     *
     * @param xData 输入1
     *
     * @param yData 输入2
     *
     * @return 他们的相关系数
     */
    public static double getPearsonCorrelationScore(double[] xData, double[] yData) {
        if (xData.length != yData.length)
            throw new RuntimeException("数据不正确！");
        double xMeans;
        double yMeans;
        double numerator = 0.0;// 求解皮尔逊的分子
        double denominator = 0.0;// 求解皮尔逊系数的分母

        double result = 0.0;
        // 拿到两个数据的平均值
        xMeans = getMeans(xData);
        yMeans = getMeans(yData);
        // 计算皮尔逊系数的分子
        numerator = generateNumerator(xData, xMeans, yData, yMeans);
        // 计算皮尔逊系数的分母
        denominator = generateDenomiator(xData, xMeans, yData, yMeans);
        // 计算皮尔逊系数
        result = numerator / denominator;
        return result;
    }

    /**
     * 计算相关系数分子
     *
     * @param xData
     *
     * @param xMeans
     *
     * @param yData
     *
     * @param yMeans
     *
     * @return 相关系数分子
     */
    private static double generateNumerator(double[] xData, double xMeans, double[] yData, double yMeans) {
        double numerator = 0.0;
        for (int i = 0; i < xData.length; i++) {
            numerator += (xData[i] - xMeans) * (yData[i] - yMeans);
        }
        return numerator;
    }

    /**
     * 生成相关系数分母
     *
     * @param yMeans
     *
     * @param yData
     *
     * @param xMeans
     *
     * @param xData
     *
     * @return 相关系数分母
     */
    private static double generateDenomiator(double[] xData, double xMeans, double[] yData, double yMeans) {
        double xSum = 0.0;
        for (int i = 0; i < xData.length; i++) {
            xSum += (xData[i] - xMeans) * (xData[i] - xMeans);
        }
        double ySum = 0.0;
        for (int i = 0; i < yData.length; i++) {
            ySum += (yData[i] - yMeans) * (yData[i] - yMeans);
        }
        return Math.sqrt(xSum) * Math.sqrt(ySum);
    }

    /**
     * 根据给定的数据集进行平均值计算
     *
     * @param datas
     *
     * @return 给定数据集的平均值
     */
    private static double getMeans(double[] datas) {
        double sum = 0.0;
        for (int i = 0; i < datas.length; i++) {
            sum += datas[i];
        }
        return sum / datas.length;
    }

    /**
     * 求给定双精度数组的门限
     *
     * @param inputData 输入数据数组
     *
     * @param alf       ？？？？？
     *
     * @return
     *
     */
    public static double getUpbound(double[] inputData, double alf) {

        double step    = 0.001;
        double max     = getMax(inputData);
        double min     = getMin(inputData);
        double upbound = 0;
        double std     = getStandardDiviation(inputData);
        double h       = 2.3450 * std * Math.pow(inputData.length, -0.2);//计算宽度因子
        double Fx      = 0;//计算概率密度以及概率密度分布函数

        double[] low_bound = new double[]{min - 0.001, 0};
        int index_len      = (int)  ((max + 0.001 - getMax(low_bound)) / step);
        Logger.i("index_len = " + index_len + " max = " + max + " min = " + min);
        double[] index     = new double[index_len];
        double[] fx        = new double[1000];
        double[] storeOfFX = new double[1000];
        Logger.i(index_len);
        System.out.println("-----------------------------2.2-----------------------------------");

        for (int i = 0; i < index_len; i++) {
            index[i] = getMax(low_bound) + i * step;
        }

        for (int i = 0; i < index.length; i++) {
            fx[i] = density(index[i], h, inputData);

            Fx = Fx + fx[i] * step;
            storeOfFX[i] = Fx;

            if (Fx > alf && upbound == 0) {
                upbound = index[i];
                break;
            }
        }
        System.out.println("-----------------------------2.2-----------------------------------");

        if (upbound == 0) {
            upbound = index[index_len];
        }
        System.out.println("-----------------------------2.3-----------------------------------");

        Logger.i("门限" + upbound);
        return upbound;
    }

    public static double density(double index, double h, double[] inputData) {
        double fx = 0;
        int n     = inputData.length;

        for (int i = 0; i < n; i++) {
            fx = fx + kernel((index-inputData[i])/h);//概率密度
        }

        fx = fx / (n * h);
        return fx;
    }

    public static double kernel(double q) {

        double vq;

        if (Math.abs(q) <= 1) {
            vq = 0.75*(1 - Math.pow(q,2));
        } else {
            vq = 0;
        }

        return vq;
    }

    /**
     * 求给定双精度数组中值的最大值
     *
     * @param inputData 输入数据数组
     * @return 运算结果, 如果输入值不合法，返回为-1
     */
    public static double getMax(double[] inputData) {
        if (inputData == null || inputData.length == 0)
            return -1;
        int len = inputData.length;
        double max = inputData[0];
        for (int i = 0; i < len; i++) {
            if (max < inputData[i])
                max = inputData[i];
        }
        return max;
    }

    /**
     * 求求给定双精度数组中值的最小值
     *
     * @param inputData 输入数据数组
     * @return 运算结果, 如果输入值不合法，返回为-1
     */
    public static double getMin(double[] inputData) {
        if (inputData == null || inputData.length == 0)
            return -1;
        int len = inputData.length;
        double min = inputData[0];
        for (int i = 0; i < len; i++) {
            if (min > inputData[i])
                min = inputData[i];
        }
        return min;
    }

    /**
     * 求给定双精度数组中值的和
     *
     * @param inputData 输入数据数组
     * @return 运算结果
     */
    public static double getSum(double[] inputData) {
        if (inputData == null || inputData.length == 0)
            return -1;
        int len = inputData.length;
        double sum = 0;

        try {
            for (int i = 0; i < len; i++) {
                sum = sum + inputData[i];
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return sum;

    }

    /**
     * 求给定双精度数组中值的数目
     *
     * @param inputData 输入数据数组
     * @return 运算结果
     */
    public static int getCount(double[] inputData) {
        if (inputData == null)
            return -1;

        return inputData.length;
    }

    /**
     * 求给定双精度数组中值的平均值
     *
     * @param inputData 输入数据数组
     * @return 运算结果
     */
    public static double getAverage(double[] inputData) {
        if (inputData == null || inputData.length == 0)
            return -1;
        int len = inputData.length;
        double result;
        result = getSum(inputData) / len;

        return result;
    }

    /**
     * 求给定双精度数组中值的平方和
     *
     * @param inputData 输入数据数组
     * @return 运算结果
     */
    public static double getSquareSum(double[] inputData) {
        if (inputData == null || inputData.length == 0)
            return -1;
        int len = inputData.length;
        double sqrsum = 0.0;
        for (int i = 0; i < len; i++) {
            sqrsum = sqrsum + inputData[i] * inputData[i];
        }


        return sqrsum;
    }

    /**
     * 求给定双精度数组中值的方差
     *
     * @param inputData 输入数据数组
     * @return 运算结果
     */
    public static double getVariance(double[] inputData) {
        int count = getCount(inputData);
        //double sqrsum = getSquareSum(inputData);
        double average = getAverage(inputData);
        double result = 0;
        for (int i = 0; i < inputData.length; i++) {
            result = result + Math.pow((inputData[i] - average), 2) / (inputData.length - 1);
        }
        //result = (sqrsum - count * average * average) / count;

        return result;
    }

    /**
     * 求给定双精度数组中值的标准差
     *
     * @param inputData 输入数据数组
     * @return 运算结果
     */
    public static double getStandardDiviation(double[] inputData) {
        double result;
        //绝对值化很重要
        result = Math.sqrt(Math.abs(getVariance(inputData)));

        return result;

    }
}
