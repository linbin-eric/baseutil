package com.jfireframework.baseutil.smc.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.jfireframework.baseutil.collection.StringCache;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.model.MethodModel.MethodModelKey;

public class ClassModel
{
    private final String                     packageName  = "com.jfireframe.smc.output";
    private final String                     className;
    private Map<String, FieldModel>          fieldStore   = new HashMap<String, FieldModel>();
    private Map<MethodModelKey, MethodModel> methodStore  = new HashMap<MethodModelKey, MethodModel>();
    private Set<String>                      constructors = new HashSet<String>();
    private Set<Class<?>>                    imports      = new HashSet<Class<?>>();
    private Set<Class<?>>                    interfaces   = new HashSet<Class<?>>();
    private Class<?>                         parentClass;
    
    public ClassModel(String className, Class<?> parentClass, Class<?>... interCc)
    {
        this.className = className;
        this.parentClass = parentClass;
        for (Class<?> each : interCc)
        {
            interfaces.add(each);
        }
    }
    
    private String buildClassDefinition()
    {
        StringCache cache = new StringCache();
        if (parentClass == Object.class)
        {
            cache.append("public class ").append(className);
        }
        else
        {
            cache.append("public class ").append(className).append(" extends ").append(SmcHelper.getTypeName(parentClass));
        }
        if (interfaces.isEmpty() == false)
        {
            cache.append(" implements ");//
            for (Class<?> each : interfaces)
            {
                cache.append(SmcHelper.getTypeName(each)).appendComma();
            }
            if (cache.isCommaLast())
            {
                cache.deleteLast();
            }
        }
        cache.append(" \r\n{\r\n");
        return cache.toString();
    }
    
    public void addInterface(Class<?> intercc)
    {
        interfaces.add(intercc);
    }
    
    public void addImport(Class<?>... ckasses)
    {
        for (Class<?> each : ckasses)
        {
            imports.add(each);
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
    
    public void putMethodModel(MethodModel methodModel)
    {
        methodStore.put(methodModel.generateKey(), methodModel);
    }
    
    public MethodModel getMethodModel(MethodModelKey key)
    {
        return methodStore.get(key);
    }
    
    public MethodModel removeMethodModel(MethodModelKey key)
    {
        return methodStore.remove(key);
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
    
    public Collection<MethodModelKey> methods()
    {
        return methodStore.keySet();
    }
    
    @Override
    public String toString()
    {
        StringCache cache = new StringCache();
        cache.append("package ").append(packageName).append(';').append("\r\n");
        for (Class<?> each : imports)
        {
            cache.append("import ").append(SmcHelper.getTypeName(each)).append(";\r\n");
        }
        cache.append(buildClassDefinition());
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
            cache.append("/*").append(no).append("*/").append(each).append("\r\n");
            no += 1;
        }
        return cache.toString();
    }
    
}
