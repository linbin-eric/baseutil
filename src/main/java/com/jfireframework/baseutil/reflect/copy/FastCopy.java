package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.ParameterizedType;

public abstract class FastCopy<S, D> implements Copy<S, D>
{
    private Copy<S, D> instance;
    
    @SuppressWarnings("unchecked")
    public FastCopy()
    {
        ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
        Class<S> source = (Class<S>) tmp.getActualTypeArguments()[0];
        Class<D> destination = (Class<D>) tmp.getActualTypeArguments()[1];
        instance = new CopyInstance<S, D>(source, destination, ReflectPropertyCopyDescriptorFactory.instance);
    }
    
    @SuppressWarnings("unchecked")
    public FastCopy(PropertyCopyDescriptorFactory factory)
    {
        ParameterizedType tmp = (ParameterizedType) (this.getClass().getGenericSuperclass());
        Class<S> source = (Class<S>) tmp.getActualTypeArguments()[0];
        Class<D> destination = (Class<D>) tmp.getActualTypeArguments()[1];
        instance = new CopyInstance<S, D>(source, destination, factory);
    }
    
    @Override
    public D copy(S src, D desc)
    {
        return instance.copy(src, desc);
    }
    
}
