package com.jfireframework.baseutil.smc.model;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;

public class MethodModel
{
    private final String modifier;
    private String       methodName;
    private String       body;
    private final String returnInfo;
    private final String argsInfo;
    private final int    sumOfarg;
    private final String throwableInfo;
    
    @Override
    public String toString()
    {
        StringCache cache = new StringCache();
        cache.append(modifier).append(' ').append(returnInfo).append(' ')//
                .append(methodName).append("(").append(argsInfo).append(") ").append(throwableInfo).append(" \r\n\t{\r\n");//
        String[] tmp = body.split("\r\n");
        for (String each : tmp)
        {
            cache.append("\t\t").append(each).append("\r\n");
        }
        cache.append("\r\n\t}\r\n");
        return cache.toString();
    }
    
    public MethodModel(Method method)
    {
        sumOfarg = method.getParameterTypes().length;
        if (Modifier.isPublic(method.getModifiers()))
        {
            modifier = "public";
        }
        else if (Modifier.isProtected(method.getModifiers()))
        {
            modifier = "protected";
        }
        else
        {
            throw new UnsupportedOperationException();
        }
        if (method.getReturnType() == void.class)
        {
            returnInfo = "void";
        }
        else
        {
            returnInfo = SmcHelper.getTypeName(method.getReturnType());
        }
        methodName = method.getName();
        StringCache cache = new StringCache();
        int index = 0;
        for (Class<?> each : method.getParameterTypes())
        {
            cache.append(SmcHelper.getTypeName(each)).append(" $").append(index).append(",");
            index += 1;
        }
        if (cache.isCommaLast())
        {
            cache.deleteLast();
        }
        argsInfo = cache.toString();
        cache.clear();
        for (Class<?> each : method.getExceptionTypes())
        {
            cache.append(each.getName()).appendComma();
        }
        if (cache.isCommaLast())
        {
            cache.deleteLast();
            throwableInfo = "throws " + cache.toString();
        }
        else
        {
            throwableInfo = "";
        }
    }
    
    public String getInvokeInfo()
    {
        StringCache cache = new StringCache();
        cache.clear().append(methodName).append('(');
        for (int i = 0; i < sumOfarg; i++)
        {
            cache.append('$').append(i).append(',');
        }
        if (cache.isCommaLast())
        {
            cache.deleteLast();
        }
        cache.append(')');
        return cache.toString();
    }
    
    public String getMethodName()
    {
        return methodName;
    }
    
    public void setMethodName(String methodName)
    {
        this.methodName = methodName;
    }
    
    public String getBody()
    {
        return body;
    }
    
    public void setBody(String body)
    {
        this.body = body;
    }
    
    public String getReturnInfo()
    {
        return returnInfo;
    }
    
    public String getArgsInfo()
    {
        return argsInfo;
    }
    
    public int getSumOfarg()
    {
        return sumOfarg;
    }
    
    public String getModifier()
    {
        return modifier;
    }
    
}
