package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

public abstract class Copy<S, D> implements CopyUtil<S, D>
{
    private Class<S>             source;
    private Class<D>             destination;
    private final CopyUtil<S, D> util;
    
    @SuppressWarnings("unchecked")
    public Copy()
    {
        ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
        source = (Class<S>) tmp.getActualTypeArguments()[0];
        destination = (Class<D>) tmp.getActualTypeArguments()[1];
        util = new CopyUtilImpl<S, D>(source, destination);
    }
    
    @SuppressWarnings("unchecked")
    public Copy(Map<String, String> nameMap)
    {
        ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
        source = (Class<S>) tmp.getActualTypeArguments()[0];
        destination = (Class<D>) tmp.getActualTypeArguments()[1];
        util = new CopyUtilImpl<S, D>(source, destination, nameMap);
    }
    
    @SuppressWarnings("unchecked")
    public Copy(String[] pairs)
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
        util = new CopyUtilImpl<S, D>(source, destination, map);
    }
    
    @Override
    public D copy(S src, D desc)
    {
        return util.copy(src, desc);
    }
    
}
