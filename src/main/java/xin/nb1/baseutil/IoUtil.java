package xin.nb1.baseutil;

import xin.nb1.baseutil.reflect.ReflectUtil;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

public class IoUtil
{
    /**
     * 这段代码拷贝自Hutool
     *
     * @param in
     * @param charset
     * @return
     */
    public static BufferedReader getReader(InputStream in, Charset charset)
    {
        if (null == in)
        {
            return null;
        }
        else
        {
            InputStreamReader reader;
            if (null == charset)
            {
                reader = new InputStreamReader(in);
            }
            else
            {
                reader = new InputStreamReader(in, charset);
            }
            return new BufferedReader(reader);
        }
    }

    public static boolean isFilePathAbsolute(String path)
    {
        if (path == null || path.isEmpty())
        {
            return false;
        }
        return Paths.get(path).isAbsolute();
    }

    public static byte[] readAllBytes(InputStream inputStream) throws IOException
    {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[]                buffer = new byte[1024];
        int                   length;
        while ((length = inputStream.read(buffer)) != -1)
        {
            output.write(buffer, 0, length);
        }
        return output.toByteArray();
    }

    public static String readFile(String path, boolean isFile, Charset charset)
    {
        if (isFile)
        {
            try (FileInputStream inputStream = new FileInputStream(path))
            {
                return new String(readAllBytes(inputStream), charset);
            }
            catch (IOException e)
            {
                ReflectUtil.throwException(e);
                return null;
            }
        }
        else
        {
            try (InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(path))
            {
                return new String(readAllBytes(resourceAsStream), charset);
            }
            catch (IOException e)
            {
                ReflectUtil.throwException(e);
                return null;
            }
        }
    }

    public static String readFile(String path, boolean isFile)
    {
        return readFile(path, isFile);
    }

    /**
     * 删除文件夹
     *
     * @param dir
     */
    public static void deleteDir(String dir)
    {
        try
        {
            Path path = Paths.get(dir);
            if (path.toFile().exists() == false)
            {
                return;
            }
            // 递归遍历文件夹，从最深层开始删除
            Files.walk(path).sorted(Comparator.reverseOrder()) // 倒序排序，确保先删除子文件/子文件夹
                 .forEach(p -> {
                     try
                     {
                         Files.delete(p);
                     }
                     catch (Exception e)
                     {
                         throw new IllegalStateException("无法删除: " + p + ", 原因: " + e.getMessage(), e);
                     }
                 });
        }
        catch (Exception e)
        {
            throw new IllegalStateException("删除文件夹失败", e);
        }
    }

    /**
     * 查找当前启动的JAR文件路径
     * 支持Spring Boot环境和标准Java环境
     *
     * @return JAR文件的绝对路径，如果无法确定则返回null
     */
    public static String findJarPath()
    {
        try
        {
            // 方法1：通过当前类的ProtectionDomain获取JAR路径
            String jarPath = getJarPathFromProtectionDomain();
            if (jarPath != null)
            {
                return jarPath;
            }
            // 方法2：通过Spring Boot的LaunchedURLClassLoader获取
            jarPath = getJarPathFromSpringBootClassLoader();
            if (jarPath != null)
            {
                return jarPath;
            }
            // 方法3：通过系统属性获取
            jarPath = getJarPathFromSystemProperty();
            if (jarPath != null)
            {
                return jarPath;
            }
            return null;
        }
        catch (Exception e)
        {
            // 发生异常时返回null，避免影响应用启动
            return null;
        }
    }

    /**
     * 通过ProtectionDomain获取JAR路径
     */
    private static String getJarPathFromProtectionDomain()
    {
        try
        {
            String location = IoUtil.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            if (location.endsWith(".jar"))
            {
                return new File(location).getAbsolutePath();
            }
        }
        catch (Exception e)
        {
            // 忽略异常，尝试其他方法
        }
        return null;
    }

    /**
     * 通过Spring Boot ClassLoader获取JAR路径
     */
    private static String getJarPathFromSpringBootClassLoader()
    {
        try
        {
            ClassLoader classLoader     = Thread.currentThread().getContextClassLoader();
            String      loaderClassName = classLoader.getClass().getName();
            // 检查是否为Spring Boot环境
            if (loaderClassName.contains("LaunchedURLClassLoader") || loaderClassName.contains("LaunchedClassLoader"))
            {
                // 尝试通过反射获取URLs
                java.net.URL[] urls = getURLsFromClassLoader(classLoader);
                if (urls != null)
                {
                    for (java.net.URL url : urls)
                    {
                        String path = url.getPath();
                        if (path.endsWith(".jar") && !path.contains("BOOT-INF/lib/"))
                        {
                            // 找到主JAR文件（不是依赖库）
                            return new File(path).getAbsolutePath();
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            // 忽略异常，尝试其他方法
        }
        return null;
    }

    /**
     * 通过反射从ClassLoader获取URLs
     */
    private static java.net.URL[] getURLsFromClassLoader(ClassLoader classLoader)
    {
        try
        {
            // 方法1：直接调用getURLs方法
            java.lang.reflect.Method getURLsMethod = classLoader.getClass().getMethod("getURLs");
            return (java.net.URL[]) getURLsMethod.invoke(classLoader);
        }
        catch (Exception e1)
        {
            try
            {
                // 方法2：通过ucp字段反射
                java.lang.reflect.Field ucpField = classLoader.getClass().getDeclaredField("ucp");
                ucpField.setAccessible(true);
                Object                   ucp           = ucpField.get(classLoader);
                java.lang.reflect.Method getURLsMethod = ucp.getClass().getDeclaredMethod("getURLs");
                getURLsMethod.setAccessible(true);
                return (java.net.URL[]) getURLsMethod.invoke(ucp);
            }
            catch (Exception e2)
            {
                // 所有反射方法都失败
                return null;
            }
        }
    }

    /**
     * 通过系统属性获取JAR路径
     */
    private static String getJarPathFromSystemProperty()
    {
        try
        {
            // 检查java.class.path中的JAR文件
            String classPath = System.getProperty("java.class.path");
            if (classPath != null)
            {
                String[] paths = classPath.split(File.pathSeparator);
                for (String path : paths)
                {
                    if (path.endsWith(".jar"))
                    {
                        File jarFile = new File(path);
                        if (jarFile.exists() && jarFile.isFile())
                        {
                            return jarFile.getAbsolutePath();
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            // 忽略异常
        }
        return null;
    }
}
