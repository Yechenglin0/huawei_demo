package domain;

/**
 * 复数运算
 * 同时可以获得相位和幅值并设置幅值和相位大小
 */
public class Complex{
    // 定义属性
    double RealPart=0;
    double ImagePart=0;

    @Override
    public String toString() {
        return "Complex{" +
                "RealPart=" + RealPart +
                ", ImagePart=" + ImagePart +
                '}';
    }

    // 定义构造函数
    public Complex(){
    }
    public Complex(double R,double I){
        this.RealPart=R;
        this.ImagePart=I;
    }
    // 定义公有方法
    public void setReal(double temp){
        this.RealPart=temp;
    }
    public void setImage(double i){
        this.ImagePart=i;
    }
    public double getReal(){
        return this.RealPart;
    }
    public double getImage(){
        return this.ImagePart;
    }

    public double getPhase(){//相位
        double x=ImagePart/RealPart;
        x = Math.toRadians(x);
        System.out.println("Math.atan(" + x + ")" + Math.atan(x));
        return Math.atan(x);
    }
    public double getAmplitude(){//幅值
        double x=Math.sqrt(ImagePart*ImagePart+RealPart*RealPart);
        return x;
    }

    public Complex times(Complex b) {
        Complex a = this;
        double real = a.RealPart * b.RealPart - a.ImagePart * b.ImagePart;
        double imag = a.RealPart * b.ImagePart + a.ImagePart * b.RealPart;
        return new Complex(real, imag);
    }

    public Complex div(Complex b){ // 复数相除
        Complex a = this;
        Complex com;
        if (b.getReal()==0&&b.getImage()==0)
        {
            com=new Complex(0,0);
            System.out.println("waring: there is zero CSI !!!");
        }else{

            double newReal = (a.RealPart*b.RealPart + a.ImagePart*b.ImagePart)/(b.RealPart*b.RealPart + b.ImagePart*b.ImagePart);
            double newImage = (a.ImagePart*b.RealPart - a.RealPart*b.ImagePart)/(b.RealPart*b.RealPart + b.ImagePart*b.ImagePart);
            com=new Complex(newReal,newImage);
        }

        return com;
    }

    public Complex plus(Complex b){ // 复数相加
        Complex a = this;

        double newReal = a.RealPart + b.RealPart;
        double newImage = a.ImagePart + b.ImagePart;
        return new Complex(newReal,newImage);
    }


}

