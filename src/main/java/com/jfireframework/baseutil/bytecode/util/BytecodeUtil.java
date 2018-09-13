package com.jfireframework.baseutil.bytecode.util;

import com.jfireframework.baseutil.bytecode.ClassFile;
import com.jfireframework.baseutil.bytecode.ClassFileParser;
import com.jfireframework.baseutil.bytecode.structure.Attribute.AttributeInfo;
import com.jfireframework.baseutil.bytecode.structure.Attribute.CodeAttriInfo;
import com.jfireframework.baseutil.bytecode.structure.Attribute.LocalVariableTableAttriInfo;
import com.jfireframework.baseutil.bytecode.structure.MethodInfo;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.reflect.ReflectUtil;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;

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
            if (resourceAsStream == null)
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

    public static String[] parseMethodParamNames(Method method)
    {
        String name = method.getDeclaringClass().getName().replace('.', '/');
        byte[] bytes = loadBytecode(method.getDeclaringClass().getClassLoader(), name);
        ClassFile classFile = new ClassFileParser(bytes).parse();
        StringCache cache = new StringCache();
        cache.append('(');
        Class<?>[] parameterTypes = method.getParameterTypes();
        for (Class<?> parameterType : parameterTypes)
        {
            cache.append(getName(parameterType));
        }
        cache.append(')').append(getName(method.getReturnType()));
        String methodName = method.getName();
        String descriptor = cache.toString().replace('.', '/');
        for (MethodInfo methodInfo : classFile.getMethodInfos())
        {
            if (methodInfo.getName().equals(methodName))
            {
                if (methodInfo.getDescriptor().equals(descriptor))
                {
                    for (AttributeInfo attributeInfo : methodInfo.getAttributeInfos())
                    {
                        if (attributeInfo instanceof CodeAttriInfo)
                        {
                            for (AttributeInfo info : ((CodeAttriInfo) attributeInfo).getAttributeInfos())
                            {
                                if (info instanceof LocalVariableTableAttriInfo)
                                {
                                    LocalVariableTableAttriInfo localVariableTableAttriInfo = (LocalVariableTableAttriInfo) info;
                                    LocalVariableTableAttriInfo.LocalVariableTableEntry[] entries = localVariableTableAttriInfo.getEntries();
                                    String[] names = new String[method.getParameterTypes().length];
                                    for (int i = 0; i < names.length; i++)
                                    {
                                        names[i] = entries[i + 1].getName();
                                    }
                                    return names;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static String getName(Class<?> parameterType)
    {
        if (parameterType.isPrimitive())
        {
            if (parameterType == int.class) return "I";
            else if (parameterType == short.class) return "S";
            else if (parameterType == long.class) return "J";
            else if (parameterType == float.class) return "F";
            else if (parameterType == double.class) return "D";
            else if (parameterType == char.class) return "C";
            else if (parameterType == byte.class) return "B";
            else if (parameterType == boolean.class) return "Z";
            else if (parameterType == void.class) return "V";
            else throw new IllegalArgumentException();
        }
        else if (parameterType.isArray())
        {
            return "[" + getName(parameterType);
        }
        else
        {
            return "L" + parameterType.getName() + ";";
        }
    }

}