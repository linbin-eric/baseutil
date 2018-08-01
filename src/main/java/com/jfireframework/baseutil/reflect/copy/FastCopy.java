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
        instance = generateInstace(source, destination, ReflectPropertyCopyDescriptorFactory.instance);
    }
    
    public FastCopy(Class<S> source, Class<D> destination)
    {
        instance = generateInstace(source, destination, ReflectPropertyCopyDescriptorFactory.instance);
    }
    
    public FastCopy(Class<S> source, Class<D> destination, PropertyCopyDescriptorFactory factory)
    {
        instance = generateInstace(source, destination, factory);
    }
    
    private CopyInstance<S, D> generateInstace(Class<S> source, Class<D> destination, PropertyCopyDescriptorFactory factory)
    {
        return new CopyInstance<S, D>(source, destination, factory);
    }
    
    @Override
    public D copy(S src, D desc)
    {
        return instance.copy(src, desc);
    }
    
}
