package com.jfireframework.baseutil.classreader.util;

import com.jfireframework.baseutil.reflect.ReflectUtil;

import java.io.IOException;
import java.io.InputStream;

public class BytecodeUtil
{
    /**
     * 通过classloader搜索类名对应的.class文件，读取其字节码
     * @param loader
     * @param name 格式为aa/bb/cc这种
     * @return
     */
    public static byte[] loadBytecode(ClassLoader loader, String name)
    {
        try
        {
            InputStream resourceAsStream = loader.getResourceAsStream(name + ".class");
            byte[] content = new byte[resourceAsStream.available()];
            resourceAsStream.read(content);
            resourceAsStream.close();
            return content;
        } catch (IOException e1)
        {
            ReflectUtil.throwException(e1);
            return null;
        }
    }
}
