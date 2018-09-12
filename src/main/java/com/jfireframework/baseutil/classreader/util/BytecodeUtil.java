package com.jfireframework.baseutil.classreader.util;

import com.jfireframework.baseutil.reflect.ReflectUtil;

import java.io.IOException;
import java.io.InputStream;

public class BytecodeUtil
{
    /**
     * 通过classloader搜索类名对应的.class文件，返回读取的字节码。如果字节码不存在，则返回null
     *
     * @param loader
     * @param name   格式为aa/bb/cc这种
     * @return
     */
    public static byte[] loadBytecode(ClassLoader loader, String name)
    {
        try
        {
            InputStream resourceAsStream = loader.getResourceAsStream(name + ".class");
            if ( resourceAsStream == null )
            {
                return null;
            }
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
