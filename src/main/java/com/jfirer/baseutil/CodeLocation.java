package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.ReflectUtil;

import java.io.File;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CodeLocation
{
    /**
     * 调用该方法所在的行数信息
     *
     * @return
     */
    public static String getCodeLocation()
    {
        return getCodeLocation(3);
    }

    /**
     * 获取方法调用的信息.1代表调用这个方法所在的行,2代表再上一层
     *
     * @param deep
     * @return
     */
    public static String getCodeLocation(int deep)
    {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[deep];
        int               index             = stackTraceElement.getClassName().lastIndexOf(".") + 1;
        return stackTraceElement.getClassName() + "." + stackTraceElement.getMethodName() + "(" + stackTraceElement.getClassName().substring(index) + ".java:" + stackTraceElement.getLineNumber() + ")";
    }

    private static volatile Class mainMethodInClass;

    /**
     * 将当前执行的 main 方法注册到静态变量，供后续读取需要
     */
    public static void registerMainMethodOfClass()
    {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String            className         = stackTraceElement.getClassName();
        String            methodName        = stackTraceElement.getMethodName();
        if (methodName.equals("main") == false)
        {
            throw new IllegalStateException("当前方法为" + className + "#" + methodName + ",不是启动的 main 方法。");
        }
        try
        {
            mainMethodInClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public static Class getMainMethodInClass()
    {
        if (mainMethodInClass == null)
        {
            throw new NullPointerException("main方法所在的类还没有注册，请确认先执行了com.jfirer.baseutil.CodeLocation.registerMainMethodOfClass方法");
        }
        return mainMethodInClass;
    }

    /**
     * 获取 main 方法所在的类的文件路径。
     * 1. 如果当前是一个 jar 包，则返回的路径是该 jar 文件所在的文件夹路径。
     * 2. 如果当前是在 ide 中运行，则返回的路径是项目文件夹本身的路径。
     * @return
     */
    public static File getFilePathOfMainMethodClass()
    {
        if (mainMethodInClass == null)
        {
            throw new NullPointerException("main方法所在的类还没有注册，请确认先执行了com.jfirer.baseutil.CodeLocation.registerMainMethodOfClass方法");
        }
        File dirPath = null;
        try
        {
            dirPath = new File(mainMethodInClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            //如果 dirPath 是一个文件夹路径，则意味着在编译输出目录下的 classes 文件夹下；如果 dirPath 是一个文件，则意味着他是一个jar 包
            dirPath = dirPath.isFile() ? dirPath.getParentFile() : dirPath.getParentFile().getParentFile();
        }
        catch (URISyntaxException e)
        {
            ReflectUtil.throwException(e);
        }
        return dirPath;
    }

    public static void main(String[] args) throws InterruptedException
    {
        System.out.println(CodeLocation.getCodeLocation());
        ExecutorService poo = Executors.newCachedThreadPool();
        poo.submit(new Runnable()
        {
            @Override
            public void run()
            {
                System.out.println(CodeLocation.getCodeLocation());
            }
        });
        poo.shutdown();
        poo.awaitTermination(50, TimeUnit.SECONDS);
    }
}
