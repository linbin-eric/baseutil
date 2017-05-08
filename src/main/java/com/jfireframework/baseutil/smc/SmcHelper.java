package com.jfireframework.baseutil.smc;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.StringUtil;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.reflect.ReflectUtil;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.MethodModel;

public class SmcHelper
{
    
    private static final AtomicInteger typeCount = new AtomicInteger(0);
    
    public static CompilerModel createClientClass(Class<?> type, Class<?>... interCc)
    {
        CompilerModel compilerModle = new CompilerModel(type.getSimpleName() + "_Smc_" + typeCount.incrementAndGet(), type, interCc);
        for (Method method : ReflectUtil.getAllMehtods(type))
        {
            if (canHaveChildMethod(method.getModifiers()))
            {
                MethodModel methodModel = new MethodModel(method);
                if (method.getReturnType() == void.class)
                {
                    methodModel.setBody("super." + methodModel.getInvokeInfo() + ";");
                }
                else
                {
                    methodModel.setBody("return super." + methodModel.getInvokeInfo() + ";");
                }
                compilerModle.putMethod(method, methodModel);
            }
        }
        return compilerModle;
    }
    
    public static CompilerModel createImplClass(Class<?> type, Class<?>... interCc)
    {
        CompilerModel compilerModle = new CompilerModel(type.getSimpleName() + "_Smc_" + typeCount.incrementAndGet(), type, interCc);
        return compilerModle;
    }
    
    private static boolean canHaveChildMethod(int moditifer)
    {
        if ((Modifier.isPublic(moditifer) || Modifier.isProtected(moditifer)) //
                && Modifier.isFinal(moditifer) == false //
                && Modifier.isNative(moditifer) == false //
                && Modifier.isStatic(moditifer) == false)
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * 根据表达式生成在java语言下的调用方式。<br/>
     * 比如user.age就会被转化为user.getAge(),或者user.boy 被转化为user.isBoy();
     * 
     * @param content
     * @param paramNames
     * @param types
     * @return
     */
    public static String buildInvoke(String content, String[] paramNames, Class<?>[] types)
    {
        StringCache cache = new StringCache();
        if (content.indexOf('.') == -1)
        {
            int i = 0;
            for (String each : paramNames)
            {
                if (each.equals(content))
                {
                    break;
                }
                else
                {
                    i++;
                }
            }
            cache.append("$").append(i);
        }
        else
        {
            String[] tmp = content.split("\\.");
            content = tmp[0];
            int i = 0;
            for (String each : paramNames)
            {
                if (each.equals(content))
                {
                    break;
                }
                else
                {
                    i++;
                }
            }
            String invokeName;
            try
            {
                invokeName = types[i].getDeclaredMethod("get" + Character.toUpperCase(tmp[1].charAt(0)) + tmp[1].substring(1)).getName() + "()";
            }
            catch (Exception e)
            {
                try
                {
                    invokeName = types[i].getDeclaredMethod("is" + Character.toUpperCase(tmp[1].charAt(0)) + tmp[1].substring(1)).getName() + "()";
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                    throw new JustThrowException(e);
                }
            }
            cache.append("$").append(i).append(".").append(invokeName);
        }
        return cache.toString();
    }
    
    /**
     * 根据表达式解出来表达式的返回值类型<br/>
     * 比如user.age age是一个int的属性，则返回int类型
     * 
     * @param content
     * @param paramNames
     * @param types
     * @return
     */
    public static Class<?> getType(String content, String[] paramNames, Class<?>[] types)
    {
        if (content.indexOf('.') == -1)
        {
            int i = 0;
            for (String each : paramNames)
            {
                if (each.equals(content))
                {
                    break;
                }
                else
                {
                    i++;
                }
            }
            if (i == types.length)
            {
                throw new NullPointerException(StringUtil.format("变量名:{}无法在给定的变量名数组中查询得到。", content));
            }
            return types[i];
        }
        else
        {
            String[] tmp = content.split("\\.");
            content = tmp[0];
            int i = 0;
            for (String each : paramNames)
            {
                if (each.equals(content))
                {
                    break;
                }
                else
                {
                    i++;
                }
            }
            try
            {
                Method method = types[i].getDeclaredMethod("get" + Character.toUpperCase(tmp[1].charAt(0)) + tmp[1].substring(1));
                return method.getReturnType();
            }
            catch (Exception e)
            {
                try
                {
                    Method method = types[i].getDeclaredMethod("is" + Character.toUpperCase(tmp[1].charAt(0)) + tmp[1].substring(1));
                    return method.getReturnType();
                }
                catch (Exception e1)
                {
                    e1.printStackTrace();
                    throw new JustThrowException(e);
                }
            }
        }
    }
    
    public static String getTypeName(Class<?> type)
    {
        if (type.isArray() == false)
        {
            return type.getName().replace('$', '.');
        }
        else
        {
            StringCache cache = new StringCache();
            while (type.isArray())
            {
                cache.append("[]");
                type = type.getComponentType();
            }
            return type.getName().replace('$', '.') + cache.toString();
        }
    }
}
