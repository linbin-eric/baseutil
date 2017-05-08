package com.jfireframework.baseutil.smc.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.collection.StringCache;

public class CompilerModel
{
    private final String             packageName  = "com.jfireframe.smc.output";
    private final String             className;
    private final String             headInfo;
    private Map<String, FieldModel>  fieldStore   = new HashMap<String, FieldModel>();
    private Map<Method, MethodModel> methodStore  = new HashMap<Method, MethodModel>();
    private Set<MethodModel>         shadowMethod = new HashSet<MethodModel>();
    
    public CompilerModel(String className, Class<?> parentClass, Class<?>... interCc)
    {
        this.className = className;
        StringCache cache = new StringCache();
        cache.append("package ").append(packageName).append(';').append("\r\n")//
                .append("public class ").append(className).append(" extends ").append(parentClass.getName().replace('$', '.'));
        if (interCc != null && interCc.length != 0)
        {
            cache.append(" implements ");//
            for (Class<?> each : interCc)
            {
                cache.append(each.getName().replace('$', '.')).appendComma();
            }
            if (cache.isCommaLast())
            {
                cache.deleteLast();
            }
        }
        cache.append(" \r\n{\r\n");
        headInfo = cache.toString();
    }
    
    public MethodModel getMethodModel(Method method)
    {
        return methodStore.get(method);
    }
    
    public String fileName()
    {
        return className + ".java";
    }
    
    public String className()
    {
        return className;
    }
    
    public void addField(FieldModel... models)
    {
        for (FieldModel each : models)
        {
            fieldStore.put(each.getName(), each);
        }
    }
    
    public void putMethod(Method key, MethodModel value)
    {
        methodStore.put(key, value);
    }
    
    public Collection<Method> methods()
    {
        return methodStore.keySet();
    }
    
    public void putShadowMethodModel(MethodModel methodModel)
    {
        shadowMethod.add(methodModel);
    }
    
    @Override
    public String toString()
    {
        StringCache cache = new StringCache(headInfo);
        for (FieldModel each : fieldStore.values())
        {
            cache.append('\t').append(each.toString());
        }
        for (MethodModel each : methodStore.values())
        {
            cache.append('\t').append(each.toString());
        }
        for (MethodModel each : shadowMethod)
        {
            cache.append('\t').append(each.toString());
        }
        cache.append("}");
        return cache.toString();
    }
    
}
