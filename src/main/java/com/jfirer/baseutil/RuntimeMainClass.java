package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.ReflectUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

@Slf4j
public class RuntimeMainClass
{
    private static volatile Class  MAIN_CLASS;
    public static final     String SELF_PID = String.valueOf(ManagementFactory.getRuntimeMXBean().getPid());

    /**
     * 将当前执行的 main 方法注册到静态变量，供后续读取需要
     */
    public static void registerMainClass()
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
            MAIN_CLASS = Thread.currentThread().getContextClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException e)
        {
            ReflectUtil.throwException(e);
        }
    }

    public static Class getMainClass()
    {
        if (MAIN_CLASS == null)
        {
            throw new NullPointerException("main方法所在的类还没有注册，请确认先执行了com.jfirer.baseutil.RuntimeMainClass.registerMainClass方法");
        }
        return MAIN_CLASS;
    }

    /**
     * 获取 main 方法所在的类的文件路径。
     * 1. 如果当前是一个 jar 包，则返回的路径是该 jar 文件所在的文件夹路径。
     * 2. 如果当前是在 ide 中运行，则返回的路径是项目文件夹本身的路径。
     *
     * @return
     */
    public static File getFilePathOfMainClass()
    {
        if (MAIN_CLASS == null)
        {
            throw new NullPointerException("main方法所在的类还没有注册，请确认先执行了com.jfirer.baseutil.CodeLocation.registerMainMethodOfClass方法");
        }
        File dirPath = null;
        try
        {
            dirPath = new File(MAIN_CLASS.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            //如果 dirPath 是一个文件夹路径，则意味着在编译输出目录下的 classes 文件夹下；如果 dirPath 是一个文件，则意味着他是一个jar 包
            dirPath = dirPath.isFile() ? dirPath.getParentFile() : dirPath.getParentFile().getParentFile();
        }
        catch (URISyntaxException e)
        {
            ReflectUtil.throwException(e);
        }
        return dirPath;
    }

    /**
     * 检查启动的单体 Jar 的名称。
     * 如果单体 Jar 的名称不包含 copy，则杀死除自己外的同名 Jar 进程，并且复制当前文件到 copy 文件，并且启动该文件。
     * 如果单体 jar 的名称包含 copy，则杀死除自己外的同名 jar 进程。继续后续业务代码。
     *
     * @param prefixName
     */
    public static void checkMainStart(String prefixName, String finalFileName)
    {
        StackTraceElement stackTraceElement = Thread.currentThread().getStackTrace()[2];
        String            className         = stackTraceElement.getClassName();
        String            methodName        = stackTraceElement.getMethodName();
        if (methodName.equals("main") == false)
        {
            throw new IllegalStateException(STR.format("当前方法为 {}#{},不是启动的 main 方法。", className, methodName));
        }
        Class<?> mainMethodInClass = null;
        try
        {
            mainMethodInClass = Thread.currentThread().getContextClassLoader().loadClass(className);
        }
        catch (ClassNotFoundException e)
        {
            ReflectUtil.throwException(e);
        }
        try
        {
            File file = new File(mainMethodInClass.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            if (file.isDirectory())
            {
                //此时意味着在 IDE 中运行，则不需要这个流程
                return;
            }
            if (!file.getName().startsWith(prefixName))
            {
                throw new IllegalArgumentException(STR.format("启动检查流程,检查的文件名前缀为:{}，实际启动的单体 Jar 为:{}，不吻合", prefixName, file.getAbsolutePath()));
            }
            List<String> pidByName = getPidByNameUseJpsWithoutSelf(prefixName);
            log.info("发现同前缀名的非自身进程有:{}", pidByName);
            pidByName.forEach(pid -> killPid(pid));
            if (file.getName().equals(finalFileName))
            {
                log.info("当前单体 Jar 应用程序是:{}，可以继续执行后续业务逻辑", file.getAbsolutePath());
            }
            else
            {
                File copyJarFile = new File(file.getParentFile(), finalFileName);
                if (copyJarFile.exists())
                {
                    copyJarFile.delete();
                }
                Files.copy(file.toPath(), copyJarFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                log.info("当前单体 Jar 应用程序是:{}，将其复制到:{}，启动该复制 Jar", file.getAbsolutePath(), copyJarFile.getAbsolutePath());
                startJar(copyJarFile.getAbsolutePath());
                //启动该 jar 的同时当前进程也需要等待被杀死，因此这里就不需要往下执行了。暂停住当前的线程。
                LockSupport.park();
            }
        }
        catch (URISyntaxException | IOException e)
        {
            log.error("启动检查过程中发生未知异常", e);
        }
    }

    public static List<String> getPidByNameUseJpsWithoutSelf(String prefix)
    {
        boolean window = System.getProperty("os.name").toLowerCase().contains("win");
        ProcessBuilder processBuilder = window ?//
                new ProcessBuilder("cmd.exe", "/c", STR.format("""
                                                                       jps | findstr /C:"{}" | for /f "tokens=1" %i in ('more') do @echo %i""", prefix))//
                : new ProcessBuilder("sh", "-c", STR.format("jps | grep '{}' | grep -v grep | awk '{print $1}'", prefix));
        try
        {
            Process      process = processBuilder.start();
            List<String> list    = new ArrayList<>();
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8")))
            {
                String line;
                while ((line = input.readLine()) != null)
                {
                    if (!line.equals(""))
                    {
                        if (!SELF_PID.equalsIgnoreCase(line))
                        {
                            log.debug("根据名称:{}查找到非自身 pid:{}", prefix, line);
                            list.add(line);
                        }
                    }
                }
            }
            process.destroy();
            return list;
        }
        catch (Throwable e)
        {
            log.error("发生未知异常", e);
            return new ArrayList<>();
        }
    }

    public static List<String> getPidByNameWithoutSelf(String prefix)
    {
        boolean window = System.getProperty("os.name").toLowerCase().contains("win");
        ProcessBuilder processBuilder = window ?//
                new ProcessBuilder("cmd.exe", "/c", "wmic process where \"name='java.exe' and CommandLine like '%%" + prefix + "%%'\" get ProcessId /value | findstr \"=\"")//
                : new ProcessBuilder("sh", "-c", "ps aux | grep '" + prefix + "' | grep -v grep | awk '{print $1,$2}'");
        try
        {
            Process      process = processBuilder.start();
            List<String> list    = new ArrayList<>();
            try (BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream(), "UTF-8")))
            {
                String line;
                while ((line = input.readLine()) != null)
                {
                    if (!line.equals(""))
                    {
                        String pid;
                        if (window)
                        {
                            pid = line.substring(10);
                        }
                        else
                        {
                            String[] split = line.split(" ");
                            if (split[0].matches("-?\\d+"))
                            {
                                pid = split[0].trim();
                            }
                            else
                            {
                                pid = split[1].trim();
                            }
                        }
                        if (!SELF_PID.equalsIgnoreCase(pid))
                        {
                            log.debug("根据名称:{}查找到非自身 pid:{}", prefix, pid);
                            list.add(pid);
                        }
                    }
                }
            }
            process.destroy();
            return list;
        }
        catch (Throwable e)
        {
            log.error("发生未知异常", e);
            return new ArrayList<>();
        }
    }

    private static void killPid(String pid)
    {
        boolean        window  = System.getProperty("os.name").toLowerCase().contains("win");
        ProcessBuilder builder = window ? new ProcessBuilder("cmd.exe", "/C", "taskkill /F /PID " + pid) : new ProcessBuilder("kill", "-9", pid);
        try
        {
            Process process = builder.start();
            process.waitFor();
            process.destroy();
            log.debug("终止进程:{}", pid);
        }
        catch (Throwable e)
        {
            log.error("关闭进程发生未知异常", e);
        }
    }

    public static void startJar(String filePath)
    {
        boolean window = System.getProperty("os.name").toLowerCase().contains("win");
        log.info("准备启动 Jar:{}", filePath);
        try
        {
            ProcessBuilder builder = window ? new ProcessBuilder("cmd.exe", "/c", "java -jar " + filePath) : new ProcessBuilder("nohup", "java", "-jar", filePath, "&");
            new Thread(() -> {
                try
                {
                    Process        process = builder.start();
                    BufferedReader reader  = process.inputReader();
                    while (reader.readLine() != null)
                    {
                        ;
                    }
                    reader.close();
                    process.destroy();
                }
                catch (Throwable e)
                {
                    log.error("发生未知异常", e);
                }
            }).start();
        }
        catch (Throwable e)
        {
            log.error("发生未知异常", e);
        }
    }
}
