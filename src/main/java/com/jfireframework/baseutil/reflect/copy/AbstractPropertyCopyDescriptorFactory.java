package com.jfireframework.baseutil.reflect.copy;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import com.jfireframework.baseutil.reflect.ReflectUtil;

public abstract class AbstractPropertyCopyDescriptorFactory implements PropertyCopyDescriptorFactory
{
    
    @Override
    public <S, D> PropertyCopyDescriptor<S, D> getInstance(Class<S> s, Class<D> d, String fromProperty, String toProperty)
    {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public <S, D> PropertyCopyDescriptor<S, D> getInstance(Class<S> s, Class<D> d, final Field fromProperty, final Field toProperty)
    {
        boolean hasTransfer = hasTransfer(fromProperty, toProperty);
        boolean isEnum = canEnumCopy(fromProperty.getType(), toProperty.getType());
        if (fromProperty.getType() != toProperty.getType() && hasTransfer == false && isEnum == false)
        {
            return new PropertyCopyDescriptor<S, D>() {
                
                @Override
                public String fromProperty()
                {
                    return fromProperty.getName();
                }
                
                @Override
                public String toProperty()
                {
                    return toProperty.getName();
                }
                
                @Override
                public void process(S source, D des) throws Exception
                {
                }
            };
        }
        fromProperty.setAccessible(true);
        toProperty.setAccessible(true);
        if (isEnum)
        {
            return generateEnumCopyPropertyCopyDescriptor(s, d, fromProperty, toProperty);
        }
        else
        {
            return generateDefaultCopyPropertyDescriptor(s, d, fromProperty, toProperty);
        }
    }
    
    /**
     * 是否是基本类到包装类的复制
     * 
     * @param fromProperty
     * @param toProperty
     * @return
     */
    private boolean hasTransfer(final Field fromProperty, final Field toProperty)
    {
        boolean hasTransfer = false;
        if (fromProperty.getType().isPrimitive())
        {
            if (fromProperty.getType() == int.class && toProperty.getType() == Integer.class)
            {
                hasTransfer = true;
            }
            else if (fromProperty.getType() == boolean.class && toProperty.getType() == Boolean.class)
            {
                hasTransfer = true;
            }
            else if (fromProperty.getType() == char.class && toProperty.getType() == Character.class)
            {
                hasTransfer = true;
            }
            else if (fromProperty.getType() == byte.class && toProperty.getType() == Byte.class)
            {
                hasTransfer = true;
            }
            else if (fromProperty.getType() == short.class && toProperty.getType() == Short.class)
            {
                hasTransfer = true;
            }
            else if (fromProperty.getType() == long.class && toProperty.getType() == Long.class)
            {
                hasTransfer = true;
            }
            else if (fromProperty.getType() == float.class && toProperty.getType() == Float.class)
            {
                hasTransfer = true;
            }
            else if (fromProperty.getType() == double.class && toProperty.getType() == Double.class)
            {
                hasTransfer = true;
            }
        }
        return hasTransfer;
    }
    
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private boolean canEnumCopy(Class<?> srcType, Class<?> desType)
    {
        if (Enum.class.isAssignableFrom(srcType) == false || Enum.class.isAssignableFrom(desType) == false)
        {
            return false;
        }
        Map<String, ? extends Enum<?>> allEnumInstances = ReflectUtil.getAllEnumInstances((Class<? extends Enum<?>>) srcType);
        boolean miss = false;
        for (Entry<String, ? extends Enum<?>> entry : allEnumInstances.entrySet())
        {
            try
            {
                Enum.valueOf((Class<Enum>) desType, entry.getKey());
            }
            catch (Exception e)
            {
                miss = true;
                break;
            }
        }
        return !miss;
    }
    
    protected abstract <S, D> PropertyCopyDescriptor<S, D> generateEnumCopyPropertyCopyDescriptor(Class<S> s, Class<D> d, final Field fromProperty, final Field toProperty);
    
    protected abstract <S, D> PropertyCopyDescriptor<S, D> generateDefaultCopyPropertyDescriptor(Class<S> s, Class<D> d, Field fromProperty, Field toProperty);
    
}
