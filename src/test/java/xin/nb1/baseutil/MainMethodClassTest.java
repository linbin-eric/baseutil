package xin.nb1.baseutil;

public class MainMethodClassTest
{
    public static void main(String[] args)
    {
        RuntimeJVM.registerMainClass();
        System.out.println(RuntimeJVM.getDirOfMainClass());
    }
}
