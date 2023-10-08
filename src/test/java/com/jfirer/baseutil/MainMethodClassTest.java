package com.jfirer.baseutil;

public class MainMethodClassTest
{
    public static void main(String[] args)
    {
        CodeLocation.registerMainMethodOfClass();
        System.out.println(CodeLocation.getFilePathOfMainMethodClass());
    }
}
