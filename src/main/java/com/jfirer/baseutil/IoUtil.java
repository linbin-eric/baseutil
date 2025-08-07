package com.jfirer.baseutil;

import com.jfirer.baseutil.reflect.ReflectUtil;

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
}
