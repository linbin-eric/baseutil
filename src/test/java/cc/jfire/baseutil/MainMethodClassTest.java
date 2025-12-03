package cc.jfire.baseutil;

public class MainMethodClassTest
{
    public static void main(String[] args)
    {
        RuntimeJVM.registerMainClass();
        System.out.println(RuntimeJVM.getDirOfMainClass());
    }
}
