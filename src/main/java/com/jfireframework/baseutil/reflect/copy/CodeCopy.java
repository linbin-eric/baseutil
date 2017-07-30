package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import com.jfireframework.baseutil.exception.JustThrowException;
import com.jfireframework.baseutil.smc.SmcHelper;
import com.jfireframework.baseutil.smc.compiler.JavaStringCompiler;
import com.jfireframework.baseutil.smc.model.CompilerModel;
import com.jfireframework.baseutil.smc.model.MethodModel;

public abstract class CodeCopy<S, D> implements Copy<S, D>
{
    static final AtomicInteger count = new AtomicInteger(1);
    private Class<S>           source;
    private Class<D>           destination;
    private final Copy<S, D>   util;
    
    @SuppressWarnings("unchecked")
    public CodeCopy()
    {
        ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
        source = (Class<S>) tmp.getActualTypeArguments()[0];
        destination = (Class<D>) tmp.getActualTypeArguments()[1];
        util = new CodeCopyUtilImpl(source, destination);
    }
    
    @SuppressWarnings("unchecked")
    public CodeCopy(Map<String, String> nameMap)
    {
        ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
        source = (Class<S>) tmp.getActualTypeArguments()[0];
        destination = (Class<D>) tmp.getActualTypeArguments()[1];
        util = new CodeCopyUtilImpl(source, destination, nameMap);
    }
    
    @SuppressWarnings("unchecked")
    public CodeCopy(String[] pairs)
    {
        ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
        source = (Class<S>) tmp.getActualTypeArguments()[0];
        destination = (Class<D>) tmp.getActualTypeArguments()[1];
        Map<String, String> map = new HashMap<String, String>();
        for (String pair : pairs)
        {
            String[] kv = pair.split(":");
            map.put(kv[0], kv[1]);
        }
        util = new CodeCopyUtilImpl(source, destination, map);
    }
    
    @Override
    public D copy(S src, D desc)
    {
        return util.copy(src, desc);
    }
    
    class CodeCopyUtilImpl implements Copy<S, D>
    {
        private final Processor<S, D> processor;
        
        @SuppressWarnings("unchecked")
        public CodeCopyUtilImpl(Class<S> src, Class<D> des)
        {
            class Entry
            {
                Method src;
                Method des;
            }
            List<Entry> list = new ArrayList<Entry>();
            for (Method getMethod : src.getMethods())
            {
                if (getMethod.isAnnotationPresent(CopyIgnore.class))
                {
                    continue;
                }
                String name = getMethod.getName();
                if ((name.startsWith("get") || name.startsWith("is"))//
                        && getMethod.getReturnType() != void.class && getMethod.getParameterTypes().length == 0)
                {
                    String srcName;
                    if (name.startsWith("get"))
                    {
                        srcName = name.substring(3);
                    }
                    else
                    {
                        srcName = name.substring(2);
                    }
                    Field field = findField(srcName.substring(0, 1).toLowerCase() + srcName.substring(1), src);
                    if (field != null && field.isAnnotationPresent(CopyIgnore.class))
                    {
                        continue;
                    }
                    String desName = "set" + srcName;
                    try
                    {
                        Method method = des.getMethod(desName, getMethod.getReturnType());
                        Entry entry = new Entry();
                        entry.src = getMethod;
                        entry.des = method;
                        list.add(entry);
                    }
                    catch (NoSuchMethodException e)
                    {
                        continue;
                    }
                    catch (Exception e)
                    {
                        throw new JustThrowException(e);
                    }
                }
            }
            CompilerModel compilerModel = new CompilerModel("Copy$" + count.getAndIncrement(), Object.class, Processor.class);
            try
            {
                MethodModel methodModel = new MethodModel(Processor.class.getMethod("exec", Object.class, Object.class));
                String body = "{\r\n"//
                        + SmcHelper.getTypeName(src) + " src = (" + SmcHelper.getTypeName(src) + ")$0;\r\n"//
                        + SmcHelper.getTypeName(des) + " des = (" + SmcHelper.getTypeName(des) + ")$1;\r\n";
                for (Entry each : list)
                {
                    body += "des." + each.des.getName() + "(src." + each.src.getName() + "());\r\n";
                }
                body += "}";
                methodModel.setBody(body);
                compilerModel.putMethod(methodModel);
                JavaStringCompiler compiler = new JavaStringCompiler();
                Class<? extends Processor<?, ?>> type = (Class<? extends Processor<?, ?>>) compiler.compile(compilerModel, src.getClassLoader());
                processor = (Processor<S, D>) type.newInstance();
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
            
        }
        
        private Field findField(String name, Class<?> ckass)
        {
            do
            {
                
                if (ckass != Object.class)
                {
                    try
                    {
                        Field field = ckass.getDeclaredField(name);
                        return field;
                    }
                    catch (Exception e)
                    {
                        ckass = ckass.getSuperclass();
                    }
                }
                else
                {
                    break;
                }
            } while (true);
            return null;
        }
        
        @SuppressWarnings("unchecked")
        public CodeCopyUtilImpl(Class<S> src, Class<D> des, Map<String, String> nameMap)
        {
            class Entry
            {
                Method src;
                Method des;
            }
            List<Entry> list = new ArrayList<Entry>();
            for (Method getMethod : src.getMethods())
            {
                String name = getMethod.getName();
                if ((name.startsWith("get") || name.startsWith("is"))//
                        && getMethod.getReturnType() != void.class && getMethod.getParameterTypes().length == 0)
                {
                    String origin;
                    if (name.startsWith("get"))
                    {
                        origin = name.substring(3);
                    }
                    else
                    {
                        origin = name.substring(2);
                    }
                    String desName;
                    String key = origin.substring(0, 1).toLowerCase() + origin.substring(1);
                    if (nameMap.containsKey(key))
                    {
                        
                        String mapName = nameMap.get(key);
                        mapName = mapName.substring(0, 1).toUpperCase() + mapName.substring(1);
                        desName = "set" + mapName;
                    }
                    else
                    {
                        desName = "set" + origin;
                    }
                    try
                    {
                        Method method = des.getMethod(desName, getMethod.getReturnType());
                        Entry entry = new Entry();
                        entry.src = getMethod;
                        entry.des = method;
                        list.add(entry);
                    }
                    catch (NoSuchMethodException e)
                    {
                        continue;
                    }
                    catch (Exception e)
                    {
                        throw new JustThrowException(e);
                    }
                }
            }
            CompilerModel compilerModel = new CompilerModel("Copy$" + count.getAndIncrement(), Object.class, Processor.class);
            try
            {
                MethodModel methodModel = new MethodModel(Processor.class.getMethod("exec", Object.class, Object.class));
                String body = "{\r\n"//
                        + SmcHelper.getTypeName(src) + " src = (" + SmcHelper.getTypeName(src) + ")$0;\r\n"//
                        + SmcHelper.getTypeName(des) + " des = (" + SmcHelper.getTypeName(des) + ")$1;\r\n";
                for (Entry each : list)
                {
                    body += "des." + each.des.getName() + "(src." + each.src.getName() + "());\r\n";
                }
                body += "}";
                methodModel.setBody(body);
                compilerModel.putMethod(methodModel);
                JavaStringCompiler compiler = new JavaStringCompiler();
                Class<? extends Processor<?, ?>> type = (Class<? extends Processor<?, ?>>) compiler.compile(compilerModel, src.getClassLoader());
                processor = (Processor<S, D>) type.newInstance();
            }
            catch (Exception e)
            {
                throw new JustThrowException(e);
            }
            
        }
        
        @Override
        public D copy(S src, D desc)
        {
            if (src == null)
            {
                return desc;
            }
            processor.exec(src, desc);
            return desc;
        }
        
    }
    
    public static interface Processor<S, D>
    {
        void exec(S s, D d);
    }
    
}
