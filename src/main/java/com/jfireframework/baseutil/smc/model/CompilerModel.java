package com.jfireframework.baseutil.smc.model;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;

public class CompilerModel
{
    private final String             packageName  = "com.jfireframe.smc.output";
    private final String             className;
    private final String             classDefinition;
    private Map<String, FieldModel>  fieldStore   = new HashMap<String, FieldModel>();
    private Map<Method, MethodModel> methodStore  = new HashMap<Method, MethodModel>();
    private Set<MethodModel>         shadowMethod = new HashSet<MethodModel>();
    private Set<String>              constructors = new HashSet<String>();
    private Set<String>              imports      = new HashSet<String>();
    
    public CompilerModel(String className, Class<?> parentClass, Class<?>... interCc)
    {
        this.className = className;
        StringCache cache = new StringCache();
        if (parentClass == Object.class)
        {
            cache.append("public class ").append(className);
        }
        else
        {
            cache.append("public class ").append(className).append(" extends ").append(SmcHelper.getTypeName(parentClass));
        }
        if (interCc != null && interCc.length != 0)
        {
            cache.append(" implements ");//
            for (Class<?> each : interCc)
            {
                cache.append(SmcHelper.getTypeName(each)).appendComma();
            }
            if (cache.isCommaLast())
            {
                cache.deleteLast();
            }
        }
        cache.append(" \r\n{\r\n");
        classDefinition = cache.toString();
    }
    
    public void addImport(Class<?>... ckasses)
    {
        for (Class<?> each : ckasses)
        {
            imports.add("import " + SmcHelper.getTypeName(each) + ";\r\n");
        }
    }
    
    public void addConstructor(String initStr, Class<?>... params)
    {
        StringCache cache = new StringCache();
        cache.append("public ").append(className);
        if (params.length == 0)
        {
            cache.append("()\r\n{");
        }
        else
        {
            cache.append("(");
            for (int i = 0; i < params.length; i++)
            {
                cache.append(SmcHelper.getTypeName(params[i])).append(" ");
                cache.append("$").append(i).append(",");
            }
            cache.deleteLast().append(")\r\n{");
        }
        cache.append(initStr).append("}\r\n");
        constructors.add(cache.toString());
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
    
    public void putMethod(MethodModel methodModel)
    {
        methodStore.put(methodModel.getMethod(), methodModel);
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
        StringCache cache = new StringCache();
        cache.append("package ").append(packageName).append(';').append("\r\n");
        for (String each : imports)
        {
            cache.append(each);
        }
        cache.append(classDefinition);
        for (String constructor : constructors)
        {
            cache.append('\t').append(constructor);
        }
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
    
    public String toStringWithLineNo()
    {
        String source = toString();
        String[] tmp = source.split("\r\n");
        StringCache cache = new StringCache(source.length());
        int no = 1;
        for (String each : tmp)
        {
            cache.append(each).append("     //line:").append(no).append("\r\n");
            no += 1;
        }
        return cache.toString();
    }
    
}
